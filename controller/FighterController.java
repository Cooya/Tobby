package controller;

import gamedata.character.PlayerLifeStatusEnum;
import gamedata.character.PlayerStatusEnum;
import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.d2o.modules.SubArea;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.informations.FightContext;
import main.FatalError;
import main.Instance;
import main.Main;
import messages.EmptyMessage;
import messages.character.SpellUpgradeRequestMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.GameContextReadyMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;
import messages.exchanges.ExchangeReadyMessage;
import messages.fights.GameActionFightCastOnTargetRequestMessage;
import messages.fights.GameFightReadyMessage;
import messages.fights.GameFightTurnFinishMessage;

public class FighterController extends CharacterController {
	public FightContext fightContext;
	protected int areaId;
	protected int monsterGroupMinSize;
	protected int monsterGroupMaxSize;
	private boolean areaIsFixed;
	private MuleController mule;

	public FighterController(Instance instance, String login, String password, int serverId, int areaId) {
		super(instance, login, password, serverId);
		this.fightContext = new FightContext(this);
		this.monsterGroupMinSize = 1;
		this.areaIsFixed = areaId != 0;
		if(this.areaIsFixed) {
			this.areaId = areaId;
			this.monsterGroupMaxSize = 8;
		}
		else
			this.monsterGroupMaxSize = 3;
	}
	
	public void setMule(MuleController mule) {
		this.mule = mule;
	}

	protected void levelUpManager() {
		if(!waitState(CharacterState.LEVEL_UP))
			return;
		waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		increaseStats();
		setFightArea(this.infos.level);
	}
	
	protected void upgradeSpell() {
		int spellId = infos.attackSpell;
		if(infos.spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			infos.spellList.get(spellId).spellLevel++;
			SpellUpgradeRequestMessage SURM = new SpellUpgradeRequestMessage();
			SURM.serialize(spellId, infos.spellList.get(spellId).spellLevel);
			instance.outPush(SURM);
			this.instance.log.p("Increasing attack spell to level " + infos.spellList.get(spellId).spellLevel + ".");
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = infos.spellList.get(spellId).spellLevel;
		if(level < 5)
			return infos.stats.spellsPoints >= level;
		return false;
	}
	
	protected void increaseStats() {
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.serialize(this.infos.element, calculateMaxStatsPoints());
		instance.outPush(SURM);
		this.instance.log.p("Increase stat : " + this.infos.element + ".");
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
	
	protected boolean lookForAndLaunchFight() {
		waitState(CharacterState.IS_LOADED);
		
		this.instance.log.p("Searching for monster group to fight.");
		int monsterGroupSize;
		for(GameRolePlayGroupMonsterInformations monsterGroup : this.roleplayContext.getMonsterGroups()) {
			monsterGroupSize = monsterGroup.staticInfos.underlings.size() + 1;
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
			Vector<GameFightMonsterInformations> aliveMonsters = this.fightContext.getAliveMonsters();
			this.instance.log.p(aliveMonsters.size() + " alive monster(s) remaining.");
			for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
				if(this.fightContext.self.stats.actionPoints >= this.infos.attackSpellActionPoints)
					castSpellOverMonster(aliveMonster);
				else
					break;
			}
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

	private void castSpellOverMonster(GameFightMonsterInformations monster) {
		this.instance.log.p("Trying to cast a spell over a monster.");
		GameActionFightCastOnTargetRequestMessage GAFCOTRM = new GameActionFightCastOnTargetRequestMessage();
		GAFCOTRM.spellId = this.infos.attackSpell;
		GAFCOTRM.targetId = monster.contextualId;
		GAFCOTRM.serialize();
		instance.outPush(GAFCOTRM);
		/*
		GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
		GAFCRM.serialize(this.infos.spellToUpgrade, monster.disposition.cellId, this.instance.id);
		instance.outPush(GAFCRM);
		*/
		waitState(CharacterState.SPELL_CASTED); // on attend le résultat du sort lancé
		try {
			Thread.sleep(1000); // nécessaire sinon kick par le serveur
		} catch(Exception e) {
			interrupt();
			return;
		}
	}
	
	protected void regenerateLife() {
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
	
	protected void emptyInventoryIfNecessary() {
		if(inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.instance.log.p("Need to empty inventory.");
			goToExchangeWithMule();
		}
	}
	
	// fonction à améliorer
	protected void goToExchangeWithMule() {
		waitState(CharacterState.MULE_AVAILABLE);
		waitState(CharacterState.IS_LOADED);
		
		if(!isInterrupted() && this.infos.currentMap.id != this.mule.waitingMapId)
			this.mvt.goTo(this.mule.waitingMapId);
		
		waitState(CharacterState.IS_LOADED);
		
		while(!isInterrupted() && !inState(CharacterState.IN_EXCHANGE)) {
			if(!this.roleplayContext.actorIsOnMap(this.mule.infos.characterId)) // si la mule n'est pas sur la map
				waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
			exchangeDemand(this.mule.infos.characterId);
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
	
	protected void setFightArea(int level) {
		if(this.areaIsFixed)
			return;
		
		if(level < 5) {
			this.areaId = 450;
			this.monsterGroupMaxSize = 2;
		}
		else if(level < 10) {
			this.areaId = 450;
			this.monsterGroupMaxSize = 3;
		}
		else if(level < 25) {
			this.areaId = 445;
			this.monsterGroupMaxSize = 3;
		}
		else if(level < 40) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 2;
		}
		else if(level < 70) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 3;
		}
		else if(level < 100) {
			this.areaId = 92;
			this.monsterGroupMaxSize = 5;
		}
		else if(level < 160) {
			this.areaId = 95;
			this.monsterGroupMaxSize = 3;
		}
		else if(level < 200) {
			this.areaId = 95;
			this.monsterGroupMaxSize = 4;
		}
		else if(level < 250) {
			this.areaId = 95;
			this.monsterGroupMaxSize = 5;
		}
		else {
			this.areaId = 95;
			this.monsterGroupMaxSize = 6;
		}
		this.instance.log.graphicalFrame.setAreaLabel(SubArea.getSubAreaById(this.areaId).getName());
		// 92 -> contour d'Astrub
		// 95 -> pious d'Astrub
		// 442 -> lac d'Incarnam
		// 443 -> forêt d'Incarnam
		// 445 -> pâturages d'Incarnam
		// 450 -> route des âmes d'Incarnam
	}
	
	protected void riseIfNecessary() {
		if(this.infos.healthState == PlayerLifeStatusEnum.STATUS_TOMBSTONE) {
			EmptyMessage msg = new EmptyMessage("GameRolePlayFreeSoulRequestMessage");
			this.instance.outPush(msg);
			updateState(CharacterState.IS_LOADED, false);
			useInteractive(287, 479466, 152192, false);
			try {
				Thread.sleep(2000); // temporaire
			} catch(Exception e) {
				interrupt();
			}
		}
	}
	
	@Override
	public void run() {
		waitState(CharacterState.IS_LOADED); // attendre l'entrée en jeu
		checkIfModeratorIsOnline(Main.MODERATOR_NAME);
		changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
		setFightArea(this.infos.level);
		
		if(inState(CharacterState.IN_FIGHT)) { // reprise de combat
			GameContextReadyMessage GCRM = new GameContextReadyMessage(); // je ne sais pas à quoi sert ce message
			GCRM.serialize(this.infos.currentMap.id);
			this.instance.outPush(GCRM);
			fight(true);
		}
		
		while(!isInterrupted()) {
			waitState(CharacterState.IS_LOADED); // important
			
			// besoin de renaître au phénix ?
			riseIfNecessary();
			
			// besoin de mettre à jour ses caractéristiques ou/et ses sorts ?
			levelUpManager();
			
			// besoin d'aller voir la mule ?
			emptyInventoryIfNecessary();
			
			// besoin d'aller dans l'aire de combat ?
			this.mvt.goToArea(this.areaId);
			
			// besoin de récupérer sa vie ?
			regenerateLife();
			
			while(!isInterrupted()) { // boucle recherche & combat
				if(lookForAndLaunchFight()) { // lancement de combat
					if(waitState(CharacterState.IN_FIGHT)) { // on vérifie si le combat a bien été lancé (avec timeout)
						fight(false);
						checkIfModeratorIsOnline(Main.MODERATOR_NAME);
						break;
					}
				}
				else
					this.mvt.changeMap();
			}
		}
		System.out.println("Thread controller of instance with id = " + this.instance.id + " terminated.");
	}
}