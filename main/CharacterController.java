package main;

import game.character.Elements;
import game.character.PlayerStatusEnum;
import game.currentmap.GameRolePlayGroupMonsterInformations;
import game.d2o.modules.MapPosition;
import game.d2p.MapsCache;
import game.d2p.ankama.MapPoint;
import game.d2p.ankama.MovementPath;
import game.fight.GameFightMonsterInformations;
import game.pathfinding.AreaRover;
import game.pathfinding.CellsPathfinder;
import game.pathfinding.Path;
import game.pathfinding.Pathfinder;
import informations.CharacterInformations;
import informations.FightContext;
import informations.RoleplayContext;

import java.util.Vector;

import utilities.Log;
import messages.EmptyMessage;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.fight.GameActionFightCastRequestMessage;
import messages.fight.GameFightReadyMessage;
import messages.fight.GameFightTurnFinishMessage;

public class CharacterController extends Thread {
	private static final boolean DEBUG = true;
	private Instance instance;
	private int fightsCounter;
	private AreaRover areaRover;
	public CharacterInformations infos;
	public RoleplayContext roleplayContext;
	public FightContext fightContext;
	public String currentPathName;
	public CellsPathfinder pathfinder;

	private class State {
		private boolean state;

		public State(boolean state) {
			this.state = state;
		}
	}

	private State isLoaded; // entrée en jeu et changement de map
	private State inMovement;
	private State inFight;
	private State inGameTurn;
	private State inRegeneration;
	private State needToEmptyInventory;
	private State levelUp;

	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.infos = new CharacterInformations(login, password, serverId, Elements.intelligence);
		this.roleplayContext = new RoleplayContext(this);
		this.fightContext = new FightContext(this);

		this.isLoaded = new State(false); // début du jeu et à chaque changement de map
		this.inMovement = new State(false); 
		this.inFight = new State(false); 
		this.inGameTurn = new State(false); 
		this.inRegeneration = new State(false); 
		this.needToEmptyInventory = new State(false);
		this.levelUp = new State(false);
	}

	public void setCurrentMap(int mapId) {
		this.infos.currentMap = MapsCache.loadMap(mapId);
		this.pathfinder = new CellsPathfinder(this.infos.currentMap);
	}

	private boolean isFree() {
		return this.isLoaded.state && !this.inMovement.state && !this.inFight.state && !this.inRegeneration.state;
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
		case MONSTER_GROUP_RESPAWN : break; // juste une stimulation
		case LEVEL_UP: this.levelUp.state = true; break;
		default : new FatalError("Unexpected event caught : " + event); break;
		}
	}

	// seul le CC entre ici
	private synchronized boolean waitSpecificState(State condition, boolean expectedState, int timeout) {
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

	// seul le CC entre ici
	private synchronized void waitAnyEvent() {
		try {
			wait();
		} catch(Exception e) {
			interrupt();
		}
	}

	private void waitState(int stateId) {
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
			/*
			case 3 : // monster group respawn
				if(DEBUG)
					this.instance.log.p("Waiting for monster group respawn.");
				waitAnyEvent();	
				return;
			 */
		}
	}

	public void moveTo(int cellId, boolean changeMap) {
		waitState(0);

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
		moveTo(mapIds.get(0), changeMap);
	}


	public void changeMap(int direction) {
		waitState(0);

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

	public void regenerateLife() {
		waitState(0);

		int missingLife = this.infos.missingLife();
		this.instance.log.p("Missing life : " + missingLife + " life points.");
		if(missingLife > 0) {
			this.inRegeneration.state = true;
			this.instance.log.p("Break for life regeneration.");
			try {
				sleep(this.infos.regenRate * 100 * missingLife); // on attend de récupérer toute sa vie
			} catch(Exception e) {
				interrupt();
				return;
			}
			this.inRegeneration.state = false;
		}
	}

	private void upgradeStats() {
		waitState(0);
		if(this.levelUp.state) {
			StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
			SURM.serialize(this.infos.element, calculateMaxStatsPoints());
			instance.outPush(SURM);
			levelUp.state = false;
			this.instance.log.p("Increase stat : " + Elements.intelligence + " of " + this.infos.stats.statsPoints + " points.");
		}
	}



	private int calculateMaxStatsPoints() {
		int stage=(getElementInfoById()/100)+1;
		return infos.stats.statsPoints-(infos.stats.statsPoints%stage);
	}

	private int getElementInfoById() {
		switch(infos.element){
		case 10: return infos.stats.strength.base;
		case 13: return infos.stats.chance.base ;
		case 14: return infos.stats.agility.base;
		case 15: return infos.stats.intelligence.base;
		}
		return 0;
	}

	public boolean lookForFight() {
		waitState(0);

		this.instance.log.p("Searching for monster group to fight.");
		Vector<GameRolePlayGroupMonsterInformations> monsterGroups;
		int monsterGroupsSize;
		while(true) {
			monsterGroups = this.roleplayContext.getMonsterGroups();
			monsterGroupsSize = monsterGroups.size();
			if(monsterGroupsSize > 0) {
				GameRolePlayGroupMonsterInformations monsterGroup = this.roleplayContext.getMonsterGroups().get((int) Math.random() * monsterGroupsSize);
				this.instance.log.p("Monster group on cell id " + monsterGroup.disposition.cellId + ".");
				if(launchFight(monsterGroup))
					return true;

				if(isInterrupted())
					return false;
			}
			else {
				this.instance.log.p("None monster group available on the map.");
				return false;
			}
		}
	}

	public boolean launchFight(GameRolePlayGroupMonsterInformations monsterGroup) {
		waitState(0);

		this.instance.log.p("Trying to take this monster group.");
		moveTo(monsterGroup.disposition.cellId, false);

		if(isInterrupted())
			return false;

		if(this.roleplayContext.getMonsterGroupCellId(monsterGroup) == this.infos.currentCellId) {
			this.instance.log.p("Monster group taken.");
			GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
			GRPAMRM.serialize(monsterGroup.contextualId);
			instance.outPush(GRPAMRM);
			return true;
		}
		return false;
	}

	public void fight(boolean fightRecovery) {
		this.instance.startFight(); // lancement de la FightFrame (à mettre en premier)
		waitState(1);
		if(!fightRecovery) { // si c'est un combat tout frais
			try {
				sleep(1000); // pour paraître plus naturel lors du lancement du combat
			} catch(Exception e) {
				interrupt();
				return;
			}
			GameFightReadyMessage GFRM = new GameFightReadyMessage();
			GFRM.serialize();
			this.instance.outPush(GFRM);
		}
		while(!isInterrupted() && this.inFight.state) {
			waitState(2); // attente du début du prochain tour ou de la fin du combat
			if(!this.inFight.state)
				break;
			launchSpell();

			if(isInterrupted())
				return;

			concludeGameTurn();
		}
		this.instance.log.p("Number of fights done : " + ++this.fightsCounter);
	}

	public void launchSpell() {
		Vector<GameFightMonsterInformations> aliveMonsters = this.fightContext.getAliveMonsters();
		for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
			if(this.fightContext.self.stats.actionPoints >= 4) {
				this.instance.log.p("Launching a spell.");
				GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
				GAFCRM.serialize(161, (short) aliveMonster.disposition.cellId, this.instance.id);
				instance.outPush(GAFCRM);	
			}
			else
				break;
			try {
				sleep(1000); // important pour le moment sinon bug
			} catch(InterruptedException e) {
				interrupt();
				return;
			}
		}
	}

	public void concludeGameTurn() {
		GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
		GFTFM.serialize();
		this.instance.outPush(GFTFM);
		this.inGameTurn.state = false;
	}

	private void changePlayerStatus() {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(PlayerStatusEnum.PLAYER_STATUS_AFK);
		this.instance.outPush(PSURM);
		this.instance.log.p("Passing in away mode.");
	}

	private void selectAreaRoverDependingOnLevel() { // à terminer
		//this.areaRover = new AreaRover(95, this); // pious d'Astrub
		this.areaRover = new AreaRover(445, this); // bouftous d'Incarnam
	}

	public void run() {
		waitState(0);

		changePlayerStatus();

		if(this.inFight.state) // reprise de combat à la reconnexion
			fight(true);

		while(!isInterrupted()) { // boucle principale 
			while(!isInterrupted() && !this.needToEmptyInventory.state) { // boucle recherche & combat
				selectAreaRoverDependingOnLevel();
				if(isInterrupted())
					break;
				upgradeStats();
				if(isInterrupted())
					break;
				regenerateLife();
				if(isInterrupted())
					break;

				if(lookForFight()) {
					waitState(1);

					if(isInterrupted())
						break;

					if(this.inFight.state) // on vérifie si le combat a bien été lancé
						fight(false);
					else
						changeMap(this.areaRover.nextMap(this));
				}
				else {
					if(isInterrupted())
						break;

					changeMap(this.areaRover.nextMap(this));
				}
			}

			// go to empty inventory
		}

		this.instance.log.p(Log.Status.CONSOLE, "Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}