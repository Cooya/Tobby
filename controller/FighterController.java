package controller;

import gamedata.character.Elements;
import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.informations.FightContext;
import main.FatalError;
import main.Instance;
import messages.EmptyMessage;
import messages.character.SpellUpgradeRequestMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;
import messages.exchanges.ExchangePlayerRequestMessage;
import messages.exchanges.ExchangeReadyMessage;
import messages.fights.GameActionFightCastRequestMessage;
import messages.fights.GameFightReadyMessage;
import messages.fights.GameFightTurnFinishMessage;

public abstract class FighterController extends CharacterController {
	protected int areaId;
	protected int monsterGroupMaxSize;
	private MuleController mule;
	public FightContext fightContext;

	public FighterController(Instance instance, String login, String password, int serverId) {
		super(instance, login, password, serverId);
		this.fightContext = new FightContext(this);
	}
	
	public void setMule(MuleController mule) {
		this.mule = mule;
	}
	
	@SuppressWarnings("unused")
	private void regenerateLife() {
		waitState(CharacterState.IS_LOADED);
		
		int missingLife = this.infos.missingLife();
		this.instance.log.p("Missing life : " + missingLife + " life points.");
		if(missingLife > 0) {
			this.instance.log.p("Break for life regeneration.");
			try {
				sleep(this.infos.regenRate * 100 * missingLife); // on attend de récupérer toute sa vie
			} catch(Exception e) {
				interrupt();
				return;
			}
			this.infos.stats.lifePoints = this.infos.stats.maxLifePoints;
			this.instance.log.graphicalFrame.setLifeLabel(this.infos.stats.lifePoints, this.infos.stats.maxLifePoints);
		}
	}
	
	
	
	
	//
	
	protected void upgradeStatsAndSpell() {
		if(!inState(CharacterState.LEVEL_UP))
			return;
		waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.serialize(this.infos.element, calculateMaxStatsPoints());
		instance.outPush(SURM);
		updateState(CharacterState.LEVEL_UP, false);
		this.instance.log.p("Increase stat : " + Elements.intelligence + " of " + this.infos.stats.statsPoints + " points.");
	}
	
	
	private void upgradeSpell() {
		int spellId = infos.spellToUpgrade;
		if(infos.spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			infos.spellList.get(spellId).spellLevel++;
			SpellUpgradeRequestMessage SURM = new SpellUpgradeRequestMessage();
			SURM.serialize(spellId, infos.spellList.get(spellId).spellLevel);
			instance.outPush(SURM);
			this.instance.log.p("Increase spell \"Flèche Magique\" to level " + infos.spellList.get(161).spellLevel);
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = infos.spellList.get(spellId).spellLevel;
		if(level < 5)
			return infos.stats.spellsPoints >= level;
		return false;
	}
	
	private int calculateMaxStatsPoints() {
		int stage = (getElementInfoById() / 100) + 1;
		return infos.stats.statsPoints - (infos.stats.statsPoints % stage);
	}

	private int getElementInfoById() {
		switch(infos.element) {
			case 10 : return infos.stats.strength.base;
			case 13 : return infos.stats.chance.base;
			case 14 : return infos.stats.agility.base;
			case 15 : return infos.stats.intelligence.base;
		}
		return 0;
	}
	
	
	
	//
	
	
	
	protected boolean lookForAndLaunchFight() {
		waitState(CharacterState.IS_LOADED);
		
		this.instance.log.p("Searching for monster group to fight.");
		int monsterGroupSize;
		for(GameRolePlayGroupMonsterInformations monsterGroup : this.roleplayContext.getMonsterGroups()) {
			monsterGroupSize = getMonsterGroupSize(monsterGroup);
			if(monsterGroupSize <= this.monsterGroupMaxSize) {
				this.instance.log.p("Going to take a monster group of size " + monsterGroupSize + " on cell id " + monsterGroup.disposition.cellId + ".");
				if(!this.mvt.moveTo(monsterGroup.disposition.cellId, false)) // groupe de monstres inatteignable
					continue;
				this.instance.log.p("Sending attack request.");
				GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
				GRPAMRM.serialize(monsterGroup.contextualId);
				instance.outPush(GRPAMRM);
				return true;
			}
		}
		this.instance.log.p("None monster group available or attackable on the map.");
		return false;
	}
	
	private int getMonsterGroupSize(GameRolePlayGroupMonsterInformations monsterGroup) {
		return 1 + monsterGroup.staticInfos.underlings.size();
	}
	
	protected void fight(boolean fightRecovery) {
		if(!fightRecovery) { // si c'est un combat tout frais
			GameFightReadyMessage GFRM = new GameFightReadyMessage();
			GFRM.serialize();
			this.instance.outPush(GFRM);
		}
		while(!isInterrupted() && inState(CharacterState.IN_FIGHT)) {
			waitState(CharacterState.IN_GAME_TURN); // attente du début du prochain tour ou de la fin du combat
			if(!inState(CharacterState.IN_FIGHT))
				break;
			launchSpell();

			if(inState(CharacterState.IN_FIGHT)) {
				GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
				GFTFM.serialize();
				this.instance.outPush(GFTFM);
				updateState(CharacterState.IN_GAME_TURN, false);
			}
		}
		if(this.roleplayContext.lastFightOutcome) { // si on a gagné le combat
			this.infos.fightsWonCounter++;
			this.instance.log.graphicalFrame.setFightsWonLabel(this.infos.fightsWonCounter);
		}
		else {
			this.infos.fightsLostCounter++;
			this.instance.log.graphicalFrame.setFightsLostLabel(this.infos.fightsLostCounter);
		 	this.instance.log.p("Fight lost.");
		}
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
	
	protected void goToExchangeWithMule(boolean giveKamas) {
		waitState(CharacterState.IS_LOADED);
		
		if(this.infos.currentMap.id != this.mule.waitingMapId)
			this.mvt.goTo(this.mule.waitingMapId);
		
		waitState(CharacterState.IS_LOADED);
		
		while(!isInterrupted() && !inState(CharacterState.IN_EXCHANGE)) {
			if(!this.roleplayContext.actorIsOnMap(this.mule.infos.characterId)) { // si la mule n'est pas sur la map
				waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
				updateState(CharacterState.NEW_ACTOR_ON_MAP, false);
			}
			ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage(); // demande d'échange
			EPRM.serialize(this.mule.infos.characterId, 1, this.instance.id);
			this.instance.outPush(EPRM);
			this.instance.log.p("Sending exchange demand.");
			waitState(CharacterState.IN_EXCHANGE); // attendre l'acceptation de l'échange (avec timeout)
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage"); // on transfère tous les objets
		this.instance.outPush(EM);
		this.instance.log.p("Transfering all objects.");
		ExchangeObjectMoveKamaMessage EOMKM = new ExchangeObjectMoveKamaMessage(); // et les kamas
		EOMKM.serialize(this.infos.stats.kamas);
		this.instance.outPush(EOMKM);
		
		try {
			sleep(5000); // on attend de pouvoir valider l'échange
		} catch(InterruptedException e) {
			interrupt();
			return;
		}
		
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'échange
		this.instance.outPush(ERM); // on valide de notre côté
		this.instance.log.p("Exchange validated from my side.");
		
		waitState(CharacterState.IS_FREE); // pour obtenir le résultat de l'échange
		if(this.roleplayContext.lastExchangeOutcome) {
			updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			this.instance.log.p("Exchange with mule terminated successfully.");
		}
		else
			throw new FatalError("Exchange with mule has failed.");	
	}
	
	@SuppressWarnings("unused")
	private void updateFightArea() {
		if(this.infos.level < 8) {
			this.areaId = 450; // route des âmes d'Incarnam
			this.monsterGroupMaxSize = 3;
		}
		else if(this.infos.level < 25) {
			this.areaId = 445; // pâturages d'Incarnam
			this.monsterGroupMaxSize = 3;
		}
		else if(this.infos.level < 30) {
			this.areaId = 92; // contour d'Astrub
			this.monsterGroupMaxSize = 1;
		}
		else {
			this.areaId = 92; // contour d'Astrub
			this.monsterGroupMaxSize = 2;
		}
		this.mvt.setArea(this.areaId);
		
		// 95 -> pious d'Astrub
		// 442 -> lac d'Incarnam
	}
	
	/*
	public void run() {
		waitState(CharacterState.IS_FREE);
		changePlayerStatus();
		updateFightArea();
		 
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			fight(true);
		}
		
		while(!isInterrupted()) {
			waitState(CharacterState.IS_FREE); // important
			
			// besoin d'aller voir la mule
			while(!isInterrupted() && inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
				if(waitState(CharacterState.MULE_AVAILABLE))
					goToExchangeWithMule(true);
				else
					sendPingRequest(); // pour rester connecté
			}
			
			// besoin de mettre à jour ses caractéristiques, ses sorts et son aire de combat
			if(inState(CharacterState.LEVEL_UP)) {
				upgradeSpell();
				upgradeStats();
				updateFightArea();
			}
			
			// besoin d'aller dans l'aire de combat
			this.mvt.goToArea(this.areaId);
			
			// besoin de récupérer sa vie
			regenerateLife();
			
			// combats
			if(lookForAndLaunchFight()) {
				if(waitState(CharacterState.IN_FIGHT)) // on vérifie si le combat a bien été lancé (avec timeout)
					fight(false);
			}
			else
				if(!isInterrupted())
					this.mvt.changeMap();
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
	*/
}