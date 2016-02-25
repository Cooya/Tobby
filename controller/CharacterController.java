package controller;

import java.util.Hashtable;

import gamedata.character.Elements;
import gamedata.character.PlayerStatusEnum;
import gamedata.d2p.MapsCache;
import controller.informations.CharacterInformations;
import controller.informations.RoleplayContext;
import controller.pathfinding.CellsPathfinder;
import main.Instance;
import messages.character.PlayerStatusUpdateRequestMessage;

public abstract class CharacterController extends Thread {
	private static final boolean DEBUG = true;
	protected Instance instance;
	protected Hashtable<CharacterState, Boolean> states;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	public String currentPathName;
	public CellsPathfinder pathfinder;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.states = new Hashtable<CharacterState, Boolean>();
		this.infos = new CharacterInformations(login, password, serverId, Elements.intelligence);
		this.roleplayContext = new RoleplayContext(this);
		
		for(CharacterState state : CharacterState.values())
			this.states.put(state, false);
	}

	public void setCurrentMap(int mapId) {
		this.infos.currentMap = MapsCache.loadMap(mapId);
		this.pathfinder = new CellsPathfinder(this.infos.currentMap);
	}
	
	private boolean isFree() {
		return this.states.get(CharacterState.IS_LOADED)
			&& !this.states.get(CharacterState.IN_MOVEMENT)
			&& !this.states.get(CharacterState.IN_FIGHT)
			&& !this.states.get(CharacterState.IN_REGENERATION)
			&& !this.states.get(CharacterState.IN_EXCHANGE);
	}
	
	// seul le thread principal entre ici
	public synchronized void updateState(CharacterState state, boolean newState) {
		if(DEBUG)
			this.instance.log.p("State updated : " + state + " = " + newState + ".");
		this.states.put(state, newState);
		notify();
	}
	
	protected synchronized boolean waitSpecificState(CharacterState state, int timeout) {
		long startTime = System.currentTimeMillis();
		long currentTime;
		while(!this.states.get(state) && (currentTime = System.currentTimeMillis() - startTime) < timeout) {
			try {
				wait(timeout - currentTime);
			} catch (Exception e) {
				interrupt();
				return false;
			}
		}
		if(this.states.get(state))
			return true;
		this.instance.log.p("TIMEOUT");
		return false; // si on ne l'a pas reçu à temps
	}
	
	private synchronized void waitAnyEvent() {
		try {
			wait();
		} catch(Exception e) {
			interrupt();
		}
	}
	
	// seul le contrôleur entre ici
	protected void waitState(CharacterState state) {
		switch(state) {
			case IS_FREE:
				if(DEBUG)
					this.instance.log.p("Waiting for character to be free.");
				while(!isInterrupted() && !isFree())
					waitAnyEvent();
				return;
			case IS_LOADED:
				if(DEBUG)
					this.instance.log.p("Waiting for character to be loaded.");
				while(!isInterrupted() && !this.states.get(CharacterState.IS_LOADED))
					waitAnyEvent();
				return;
			case IN_FIGHT :
				if(DEBUG)
					this.instance.log.p("Waiting for fight beginning.");
				while(!isInterrupted() && !this.states.get(CharacterState.IN_FIGHT))
					if(!waitSpecificState(CharacterState.IN_FIGHT, 2000))
						return;
				return;
			case IN_GAME_TURN :
				if(DEBUG)
					this.instance.log.p("Waiting for my game turn.");
				while(!isInterrupted() && this.states.get(CharacterState.IN_FIGHT) && !this.states.get(CharacterState.IN_GAME_TURN))
					waitAnyEvent();	
				return;
			case PENDING_DEMAND :
				if(DEBUG)
					this.instance.log.p("Waiting for exchange demand.");
				while(!isInterrupted() && !this.states.get(CharacterState.PENDING_DEMAND))
					if(!waitSpecificState(CharacterState.PENDING_DEMAND, 1000 * 60 * 5)) // 5 minutes
						return;
				return;
			case IN_EXCHANGE :
				if(DEBUG)
					this.instance.log.p("Waiting for exchange acceptance.");
				while(!isInterrupted() && !this.states.get(CharacterState.IN_EXCHANGE))
					waitAnyEvent();
				return;
			case EXCHANGE_VALIDATED :
				if(DEBUG)
					this.instance.log.p("Waiting for exchange validation.");
				while(!isInterrupted() && !this.states.get(CharacterState.EXCHANGE_VALIDATED))
					waitAnyEvent();
				return;
			case NEW_ACTOR_ON_MAP :
				if(DEBUG)
					this.instance.log.p("Waiting for new actor on the map.");
				while(!isInterrupted() && !this.states.get(CharacterState.NEW_ACTOR_ON_MAP))
					waitAnyEvent();
				return;
			default : new Error("Unexpected state waiting : " + state + "."); break;
		}
	}
	
	protected void changePlayerStatus() {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.instance.outPush(PSURM);
		this.instance.log.p("Passing in away mode.");
	}
}