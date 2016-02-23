package controller;

import gamedata.character.Elements;
import gamedata.currentmap.GameRolePlayGroupMonsterInformations;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.informations.FightContext;
import controller.pathfinding.AreaRover;
import controller.pathfinding.PathsCache;
import main.Instance;
import main.Log;
import messages.EmptyMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.exchange.ExchangePlayerRequestMessage;
import messages.exchange.ExchangeReadyMessage;
import messages.fight.GameActionFightCastRequestMessage;
import messages.fight.GameFightReadyMessage;
import messages.fight.GameFightTurnFinishMessage;

public class FighterController extends CharacterController {
	private int fightsCounter;
	private AreaRover areaRover;
	private MuleController mule;
	public FightContext fightContext;

	public FighterController(Instance instance, String login, String password, int serverId, MuleController mule) {
		super(instance, login, password, serverId);
		this.fightContext = new FightContext(this);
		this.mule = mule;
	}
	
	public void regenerateLife() {
		waitState(CharacterState.IS_FREE);
		
		int missingLife = this.infos.missingLife();
		this.instance.log.p("Missing life : " + missingLife + " life points.");
		if(missingLife > 0) {
			this.states.put(CharacterState.IN_REGENERATION, true);
			this.instance.log.p("Break for life regeneration.");
			try {
				sleep(this.infos.regenRate * 100 * missingLife); // on attend de récupérer toute sa vie
			} catch(Exception e) {
				interrupt();
				return;
			}
			this.states.put(CharacterState.IN_REGENERATION, false);
		}
	}
	
	private void upgradeStats() {
		waitState(CharacterState.IS_FREE);
		
		if(this.states.get(CharacterState.LEVEL_UP)) {
			StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
			SURM.serialize(this.infos.element, calculateMaxStatsPoints());
			instance.outPush(SURM);
			this.states.put(CharacterState.LEVEL_UP, false);
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
	
	private boolean lookForFight() {
		waitState(CharacterState.IS_FREE);
		
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
	
	private boolean launchFight(GameRolePlayGroupMonsterInformations monsterGroup) {
		waitState(CharacterState.IS_FREE);
		
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
	
	private void fight(boolean fightRecovery) {
		this.instance.startFight(); // lancement de la FightFrame (à mettre en premier)
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
		while(!isInterrupted() && this.states.get(CharacterState.IN_FIGHT)) {
			waitState(CharacterState.IN_GAME_TURN); // attente du début du prochain tour ou de la fin du combat
			if(!this.states.get(CharacterState.IN_FIGHT))
				break;
			launchSpell();
			
			if(isInterrupted())
				return;
			
			concludeGameTurn();
		}
		this.instance.log.p("Number of fights done : " + ++this.fightsCounter);
	}

	private void launchSpell() {
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
	
	private void concludeGameTurn() {
		GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
		GFTFM.serialize();
		this.instance.outPush(GFTFM);
		this.states.put(CharacterState.IN_GAME_TURN, false);
	}
	
	private void selectAreaRoverDependingOnLevel() { // à terminer
		//this.areaRover = new AreaRover(95, this); // pious d'Astrub
		this.areaRover = new AreaRover(445, this); // bouftous d'Incarnam
	}
	
	private void goToExchangeWithMule(boolean giveKamas) {
		if(this.infos.currentMap.id != this.mule.waitingMapId)
			PathsCache.moveTo(this.mule.waitingMapId, this);
		while(!isInterrupted() && !this.roleplayContext.actorIsOnMap(this.mule.infos.characterId))
			waitState(CharacterState.NEW_ACTOR_ON_MAP); // attendre que la mule revienne sur la map
		if(isInterrupted())
			return;
		
		ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage(); // demande d'échange
		EPRM.serialize(this.mule.infos.characterId, 1);
		this.instance.outPush(EPRM);
		waitState(CharacterState.IN_EXCHANGE); // attendre l'acceptation de l'échange
		if(isInterrupted())
			return;
		
		try {
			sleep(2000); // on attend un peu que la fenêtre d'échange apparaisse
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage"); // on transfère tout
		this.instance.outPush(EM);
		
		// donner aussi les kamas (à faire)
		
		try {
			sleep(5000); // on attend de pouvoir valider l'échange
		} catch (InterruptedException e) {
			interrupt();
			return;
		}
		
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 1);
		this.instance.outPush(ERM); // on valide de notre côté
		
		waitState(CharacterState.IS_FREE);
	}
	
	public void run() {
		waitState(CharacterState.IS_FREE);
		
		changePlayerStatus();
		
		if(this.states.get(CharacterState.IN_FIGHT)) // reprise de combat à la reconnexion
			fight(true);
		
		if(!isInterrupted())
			selectAreaRoverDependingOnLevel(); // se rend à l'aire de combat
		
		while(!isInterrupted()) { // boucle principale 
			while(!isInterrupted() && !this.states.get(CharacterState.NEED_TO_EMPTY_INVENTORY)) { // boucle recherche & combat
				upgradeStats();
				if(isInterrupted())
					break;

				regenerateLife();
				if(isInterrupted())
					break;
				
				if(lookForFight()) {
					waitState(CharacterState.IN_FIGHT); // avec timeout
					
					if(isInterrupted())
						break;
					
					if(this.states.get(CharacterState.IN_FIGHT)) // on vérifie si le combat a bien été lancé
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
			//if(!isInterrupted())
				//goToExchangeWithMule(true);
		}
		this.instance.log.p(Log.Status.CONSOLE, "Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}