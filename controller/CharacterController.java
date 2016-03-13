package controller;

import java.util.Hashtable;

import gamedata.character.Elements;
import gamedata.character.PlayerStatusEnum;
import gamedata.d2p.ankama.Map;
import controller.informations.CharacterInformations;
import controller.informations.RoleplayContext;
import main.FatalError;
import main.Instance;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.parties.PartyLeaveRequestMessage;
import messages.synchronisation.BasicPingMessage;

public abstract class CharacterController extends Thread {
	private Hashtable<CharacterState, Boolean> states;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	protected MovementController mvt;
	protected Instance instance;
	protected int partyId;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		super(login + "/controller");
		this.instance = instance;
		this.states = new Hashtable<CharacterState, Boolean>();
		this.infos = new CharacterInformations(login, password, serverId, Elements.intelligence);
		this.roleplayContext = new RoleplayContext(this);
		this.mvt = new MovementController(this);
		
		for(CharacterState state : CharacterState.values())
			this.states.put(state, false);
	}
	
	public void updatePosition(Map map, int cellId) {
		this.mvt.updatePosition(map, cellId);
	}

	public void updatePosition(int cellId) {
		this.mvt.updatePosition(cellId);
	}
	
	public void setPartyId(int partyId) {
		this.partyId = partyId;
	}
	
	protected void changePlayerStatus() {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.instance.outPush(PSURM);
		this.instance.log.p("Passing in away mode.");
	}
	
	protected void sendPingRequest() {
		BasicPingMessage BPM = new BasicPingMessage();
		BPM.serialize(false);
		this.instance.outPush(BPM);
		this.instance.log.p("Sending a ping request to server for stay connected.");
	}
	
	protected void useInteractive(int besideCellId, int elemId, int skillInstanceUid) {
		if(!this.mvt.moveTo(besideCellId, false))
			return;
		waitState(CharacterState.IS_LOADED);
		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(elemId, skillInstanceUid, this.instance.id);
		this.instance.outPush(IURM);
		this.instance.log.p("Interactive used.");
		updateState(CharacterState.IS_LOADED, false);
	}
	
	protected void leaveGroup() {
		PartyLeaveRequestMessage PLRM = new PartyLeaveRequestMessage();
		PLRM.partyId = this.partyId;
		PLRM.serialize();
		this.instance.outPush(PLRM);
		this.instance.log.p("Leaving group request sent.");
		waitState(CharacterState.NOT_IN_PARTY);
	}
	
	// seul le thread de traitement entre ici
	public synchronized void updateState(CharacterState state, boolean newState) {
		this.instance.log.p("State updated : " + state + " = " + newState + ".");
		this.states.put(state, newState);
		notify();
	}
	
	public boolean inState(CharacterState state) {
		return this.states.get(state);
	}
	
	// seul le contrôleur entre ici
	protected boolean waitState(CharacterState state) {
		Condition condition = null;
		boolean isEvent = false;
		switch(state) {
			case IS_FREE : // état composé
				this.instance.log.p("Waiting for character to be free.");
				condition = new Condition(CharacterState.IS_LOADED);
				condition.addSecondState(CharacterState.IN_EXCHANGE, false);
				break;
			case IN_GAME_TURN : // état avec contrainte
				this.instance.log.p("Waiting for my game turn.");
				condition = new Condition(state);
				condition.addConstraint(CharacterState.IN_FIGHT, false);
				break;
			case IS_LOADED : // état simple
				this.instance.log.p("Waiting for character to be loaded.");
				condition = new Condition(state);
				break;
			case PENDING_DEMAND : // état simple
				this.instance.log.p("Waiting for exchange demand.");
				condition = new Condition(state);
				break;
			case EXCHANGE_VALIDATED : // état simple
				this.instance.log.p("Waiting for exchange validation.");
				condition = new Condition(state);
				break;
			case MULE_AVAILABLE : // état simple
				this.instance.log.p("Waiting for mule available.");
				condition = new Condition(state);
				break;
			case CAN_MOVE : // état simple
				this.instance.log.p("Waiting for movement acceptation by server.");
				condition = new Condition(state);
				break;
			case IN_PARTY : // état simple
				this.instance.log.p("Waiting for joining party.");
				condition = new Condition(state);
				break;
			case NOT_IN_PARTY : // état abstrait inverse
				this.instance.log.p("Waiting for leaving party.");
				condition = new Condition(CharacterState.IN_PARTY, false);
				break;
			case IN_FIGHT : // attente avec timeout
				this.instance.log.p("Waiting for fight beginning.");
				condition = new Condition(state, 5000);
				break;
			case IN_EXCHANGE : // attente avec timeout
				this.instance.log.p("Waiting for exchange acceptance.");
				condition = new Condition(state, 5000);
				break;
			case NEW_ACTOR_ON_MAP : // event
				this.instance.log.p("Waiting for new actor on the map.");
				condition = new Condition(state);
				isEvent = true;
				break;
			case CAPTAIN_ACT : // event
				this.instance.log.p("Waiting for captain act.");
				condition = new Condition(state);
				isEvent = true;
				break;
			case NEW_PARTY_MEMBER : // event
				this.instance.log.p("Waiting for party invitation acceptation.");
				condition = new Condition(state);
				isEvent = true;
				break;
			case FIGHT_LAUNCHED : // event
				this.instance.log.p("Waiting for fight be launched by captain.");
				condition = new Condition(state);
				isEvent = true;
				break;
			case NEW_ACTOR_IN_FIGHT : // event avec contrainte
				this.instance.log.p("Waiting for soldier join fight.");
				condition = new Condition(state);
				condition.addConstraint(CharacterState.IN_GAME_TURN, true);
				isEvent = true;
				break;
			case LEVEL_UP : // état non attendu
				throw new FatalError("Unexpected waiting state : " + state + ".");
			case NEED_TO_EMPTY_INVENTORY : // état non attendu
				throw new FatalError("Unexpected waiting state : " + state + ".");
		}
		boolean result = waitFor(condition);
		if(isEvent)
			this.states.put(condition.expectedState, false);
		return result;
	}
	
	private synchronized boolean waitFor(Condition condition) {
		boolean infiniteWaiting = condition.timeout == 0;
		if(infiniteWaiting)
			condition.timeout = 1000 * 60 * 5; // 5 minutes
		long startTime = System.currentTimeMillis();
		long currentTime;
		while(true) {
			while((currentTime = System.currentTimeMillis() - startTime) < condition.timeout) {
				if(!condition.type) {
					if(this.states.get(condition.expectedState) == condition.expectedValueForExpectedState)
						return true;
					if(condition.otherState != null && this.states.get(condition.otherState) == condition.expectedValueForOtherState)
						return false;
				}
				else
					if(this.states.get(condition.expectedState) == condition.expectedValueForExpectedState && this.states.get(condition.otherState) == condition.expectedValueForOtherState)
						return true;
				try {
					wait(condition.timeout - currentTime);
				} catch(Exception e) {
					interrupt();
					return false;
				}
			}
			if(infiniteWaiting) {
				sendPingRequest();
				startTime = System.currentTimeMillis();
			}
			else {
				this.instance.log.p("TIMEOUT");
				return false; // si on ne l'a pas reçu à temps
			}
		}
	}

	private static class Condition {
		private CharacterState expectedState;
		private boolean expectedValueForExpectedState;
		private CharacterState otherState;
		private boolean expectedValueForOtherState;
		private int timeout;
		private boolean type; // true = second state, false = simple state or constraint state
		
		private Condition(CharacterState state) {
			this.expectedState = state;
			this.expectedValueForExpectedState = true;
			this.timeout = 0;
			this.otherState = null;
			this.type = false;
		}
		
		private Condition(CharacterState state, boolean expectedValue) {
			this.expectedState = state;
			this.expectedValueForExpectedState = expectedValue;
			this.timeout = 0;
			this.otherState = null;
			this.type = false;
		}
		
		private Condition(CharacterState state, int timeout) {
			this.expectedState = state;
			this.expectedValueForExpectedState = true;
			this.timeout = timeout;
			this.otherState = null;
			this.type = false;
		}
		
		
		private void addSecondState(CharacterState state, boolean expectedValue) {
			this.otherState = state;
			this.expectedValueForOtherState = expectedValue;
			this.type = true;
		}
		
		private void addConstraint(CharacterState state, boolean expectedValue) {
			this.otherState = state;
			this.expectedValueForOtherState = expectedValue;
			this.type = false;
		}
	}
}