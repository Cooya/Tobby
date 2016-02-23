package controller;

import gamedata.character.Elements;
import gamedata.character.PlayerStatusEnum;
import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;

import java.util.Vector;

import controller.informations.CharacterInformations;
import controller.informations.RoleplayContext;
import controller.pathfinding.CellsPathfinder;
import controller.pathfinding.Path;
import controller.pathfinding.Pathfinder;
import main.FatalError;
import main.Instance;
import messages.EmptyMessage;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;

public abstract class CharacterController extends Thread {
	private static final boolean DEBUG = true;
	protected Instance instance;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	public String currentPathName;
	public CellsPathfinder pathfinder;
	
	protected class State {
		protected boolean state;
		
		public State(boolean state) {
			this.state = state;
		}
	}

	protected State isLoaded; // entrée en jeu et changement de map
	protected State inMovement;
	protected State inFight;
	protected State inGameTurn;
	protected State inRegeneration;
	protected State needToEmptyInventory;
	protected State levelUp;
	protected State inExchange;
	protected State exchangeValidated;
	protected State newActorOnMap;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.infos = new CharacterInformations(login, password, serverId, Elements.intelligence);
		this.roleplayContext = new RoleplayContext(this);
		
		// états du personnage
		this.isLoaded = new State(false); // début du jeu et à chaque changement de map
		this.inMovement = new State(false); 
		this.inFight = new State(false); 
		this.inGameTurn = new State(false); 
		this.inRegeneration = new State(false); 
		this.needToEmptyInventory = new State(false);
		this.levelUp = new State(false);
		this.inExchange = new State(false);
		this.exchangeValidated = new State(false);
		this.newActorOnMap = new State(false);
	}

	public void setCurrentMap(int mapId) {
		this.infos.currentMap = MapsCache.loadMap(mapId);
		this.pathfinder = new CellsPathfinder(this.infos.currentMap);
	}
	
	private boolean isFree() {
		return this.isLoaded.state && !this.inMovement.state && !this.inFight.state && !this.inRegeneration.state && !this.inExchange.state;
	}
	
	// seul le thread principal entre ici
	public synchronized void emit(Event event) {
		if(DEBUG)
			this.instance.log.p("Event emitted : " + event);
		switchEvent(event);
		notify();
	}
	
	private void switchEvent(Event event) {
		switch(event) {
			case CHARACTER_LOADED : this.isLoaded.state = true; break;
			case FIGHT_START : this.inFight.state = true; break;
			case FIGHT_END : this.inFight.state = false; break;
			case GAME_TURN_START : this.inGameTurn.state = true; break;
			case WEIGHT_MAX : this.needToEmptyInventory.state = true; break;
			case LEVEL_UP : this.levelUp.state = true; break;
			case EXCHANGE_DEMAND : this.inExchange.state = true; break;
			case EXCHANGE_VALIDATION : this.exchangeValidated.state = true; break;
			case EXCHANGE_LEAVE : this.inExchange.state = false; break;
			case EXCHANGE_START : this.inExchange.state = true; break;
			case NEW_ACTOR : this.newActorOnMap.state = true; break;
			default : new FatalError("Unexpected event caught : " + event); break;
		}
	}
	
	// seul le contrôleur entre ici
	protected synchronized boolean waitSpecificState(State condition, boolean expectedState, int timeout) {
		long startTime = System.currentTimeMillis();
		long currentTime;
		while(condition.state != expectedState && (currentTime = System.currentTimeMillis() - startTime) < timeout) {
			try {
				wait(timeout - currentTime);
			} catch (Exception e) {
				interrupt();
				return false;
			}
		}
		if(condition.state == expectedState)
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
	protected void waitState(int stateId) {
		switch(stateId) {
			case 0 : // free
				if(DEBUG)
					this.instance.log.p("Waiting for character to be free.");
				while(!isInterrupted() && !isFree())
					waitAnyEvent();
				return;
			case 1 : // start fight
				if(DEBUG)
					this.instance.log.p("Waiting for fight beginning.");
				while(!isInterrupted() && !this.inFight.state)
					if(!waitSpecificState(this.inFight, true, 2000))
						return;
				return;
			case 2 : // start game turn
				if(DEBUG)
					this.instance.log.p("Waiting for my game turn.");
				while(!isInterrupted() && !this.inGameTurn.state && this.inFight.state)
					waitAnyEvent();	
				return;
			case 3 : // exchange demand
				if(DEBUG)
					this.instance.log.p("Waiting for exchange demand.");
				while(!isInterrupted() && !this.inExchange.state)
					waitAnyEvent();
				return;
			case 4 : // exchange acceptation
				if(DEBUG)
					this.instance.log.p("Waiting for exchange acceptance.");
				while(!isInterrupted() && !this.inExchange.state)
					waitAnyEvent();
				return;
			case 5 : // new actor on the map
				if(DEBUG)
					this.instance.log.p("Waiting for new actor on the map.");
				while(!isInterrupted() && !this.newActorOnMap.state)
					waitAnyEvent();
				return;
			case 6 : // leave exchange
				if(DEBUG)
					this.instance.log.p("Waiting for leaving exchange.");
				while(!isInterrupted() && this.inExchange.state)
					waitAnyEvent();
				return;
		}
	}

	public void moveTo(int cellId, boolean changeMap) {
		waitState(0);
		if(isInterrupted())
			return;

		if(this.infos.currentCellId == cellId) { // déjà sur la cellule cible
			this.instance.log.p("Already on the target cell id.");
			return;
		}
		
		this.instance.log.p("Moving from " + this.infos.currentCellId + " to " + cellId + ".");

		pathfinder = new CellsPathfinder(this.infos.currentMap);
		Path path = pathfinder.compute(this.infos.currentCellId, cellId);
		
		this.instance.log.p(path.toString());
		
		MovementPath mvPath = CellsPathfinder.movementPathFromArray(path.toVector());
		mvPath.setStart(MapPoint.fromCellId(this.infos.currentCellId));
		mvPath.setEnd(MapPoint.fromCellId(cellId));
		
		this.instance.log.p("Sending movement request.");
	
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(mvPath.getServerMovement(), this.infos.currentMap.id, instance.id);
		instance.outPush(GMMRM);
		this.inMovement.state = true;
		
		int duration = path.getCrossingDuration();
		this.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			sleep((long) (duration + (duration * 0.3))); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			interrupt();
			return;
		}
		
		this.instance.log.p("Target cell reached.");

		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		instance.outPush(EM);
		this.inMovement.state = false;
	}

	public void moveTo(int x, int y, boolean changeMap) {
		Vector<Integer> mapIds = MapPosition.getMapIdByCoord(x, y);
		if(mapIds.size() == 0)
			throw new Error("Invalid map coords.");
		moveTo(mapIds.get(0), changeMap); // pas terrible ça
	}

	public void changeMap(int direction) {
		waitState(0);
		if(isInterrupted())
			return;

		this.instance.log.p("Moving to " + Pathfinder.directionToString(direction) + " map.");

		moveTo(pathfinder.getChangementMapCell(direction), true);
		
		if(isInterrupted())
			return;
		
		this.instance.log.p("Sending map changement request.");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(this.infos.currentMap.getNeighbourMapFromDirection(direction));
		instance.outPush(CMM);

		this.isLoaded.state = false; // on attend la fin du changement de map
	}
	
	protected void changePlayerStatus() {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.instance.outPush(PSURM);
		this.instance.log.p("Passing in away mode.");
	}
	
	protected boolean isInIncarnam() {
		if(this.infos.currentMap.subareaId >= 422 && this.infos.currentMap.subareaId <= 450)
			return true;
		return false;
	}
}