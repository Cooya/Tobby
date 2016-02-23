package controller;

import java.util.Hashtable;

import gamedata.character.Elements;
import gamedata.character.PlayerStatusEnum;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.MapPoint;
import gamedata.d2p.ankama.MovementPath;
import controller.informations.CharacterInformations;
import controller.informations.RoleplayContext;
import controller.pathfinding.CellsPathfinder;
import controller.pathfinding.Path;
import controller.pathfinding.Pathfinder;
import controller.pathfinding.PathsCache;
import main.Instance;
import messages.EmptyMessage;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public abstract class CharacterController extends Thread {
	private static final boolean DEBUG = true;
	protected Instance instance;
	protected Hashtable<CharacterState, Boolean> states;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	public String currentPathName;
	public CellsPathfinder pathfinder;
	
	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.states = new Hashtable<CharacterState, Boolean>();
		this.instance = instance;
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
	
	protected synchronized boolean waitSpecificState(CharacterState condition, boolean expectedState, int timeout) {
		long startTime = System.currentTimeMillis();
		long currentTime;
		while(this.states.get(condition) != expectedState && (currentTime = System.currentTimeMillis() - startTime) < timeout) {
			try {
				wait(timeout - currentTime);
			} catch (Exception e) {
				interrupt();
				return false;
			}
		}
		if(this.states.get(condition) == expectedState)
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
			case IN_FIGHT :
				if(DEBUG)
					this.instance.log.p("Waiting for fight beginning.");
				while(!isInterrupted() && !this.states.get(CharacterState.IN_FIGHT))
					if(!waitSpecificState(CharacterState.IN_FIGHT, true, 2000))
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
				while(!isInterrupted() && !this.states.get(CharacterState.IN_EXCHANGE))
					waitAnyEvent();
				return;
			case IN_EXCHANGE :
				if(DEBUG)
					this.instance.log.p("Waiting for exchange acceptance.");
				while(!isInterrupted() && !this.states.get(CharacterState.IN_EXCHANGE))
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

	public void moveTo(int cellId, boolean changeMap) {
		waitState(CharacterState.IS_FREE);
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
		this.states.put(CharacterState.IN_MOVEMENT, true);
		
		int duration = path.getCrossingDuration();
		this.instance.log.p("Movement duration : " + duration + " ms.");

		try {
			sleep((long) (duration * 1.1)); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			interrupt();
			return;
		}
		
		this.instance.log.p("Target cell reached.");

		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		instance.outPush(EM);
		this.states.put(CharacterState.IN_MOVEMENT, false);
	}

	public void changeMap(int direction) {
		waitState(CharacterState.IS_FREE);
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
			
		this.states.put(CharacterState.IS_LOADED, false); // on attend la fin du changement de map
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
	
	protected void goDownToAstrub() {
		PathsCache.moveTo(153880835, this); // map où se situe le pnj
		if(interrupted())
			return;
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.infos.currentMap.id, this.instance.id); // on parle au pnj
		this.instance.outPush(NGARM);
		
		try {
			sleep(1000); // on attend la première question
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25209); // on sélectionne la première réponse
		this.instance.outPush(NDRM);
		
		try {
			sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(25207); // on sélectionne la seconde réponse
		this.instance.outPush(NDRM);
		
		this.states.put(CharacterState.IS_LOADED, false);
	}
	
	protected void goToAstrubBank() {
		PathsCache.moveTo(84674566, this); // map où se situe la banque
		if(interrupted())
			return;
		
		moveTo(317, false); // entrée de la banque
		if(interrupted())
			return;
		
		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(465440, 140242, this.instance.id); // porte de la banque
		this.instance.outPush(IURM);
		
		this.states.put(CharacterState.IS_FREE, false);
		waitState(CharacterState.IS_FREE);
		if(interrupted())
			return;
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.infos.currentMap.id, this.instance.id); // on parle au banquier
		this.instance.outPush(NGARM);
		
		try {
			sleep(1000); // on attend la question
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(259); // on sélectionne la réponse
		this.instance.outPush(NDRM);
		
		try {
			sleep(2000); // on attend l'affichage de l'inventaire
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.instance.outPush(EM); // on transfère tous les objets de l'inventaire
		
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EM = new EmptyMessage("LeaveDialogRequestMessage");
		this.instance.outPush(EM); // on ferme l'inventaire
		
		try {
			sleep(1000); // on attend que l'inventaire se ferme
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		moveTo(396, false);
		if(interrupted())
			return;
		
		// à finir
	}
}