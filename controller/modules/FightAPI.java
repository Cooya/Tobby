package controller.modules;

import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.enums.PlayerLifeStatusEnum;
import gamedata.fight.GameFightMonsterInformations;
import gamedata.inventory.SpellItem;

import java.util.HashMap;
import java.util.Vector;

import controller.CharacterState;
import controller.characters.Character;
import controller.characters.LoneFighter;
import controller.informations.FightOptions;
import messages.UnhandledMessage;
import messages.character.SpellModifyRequestMessage;
import messages.character.StatsUpgradeRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.fights.GameActionFightCastOnTargetRequestMessage;
import messages.fights.GameFightReadyMessage;
import messages.fights.GameFightTurnFinishMessage;

public class FightAPI {
	private Character character;
	private FightOptions fightOptions;
	private int lastFight;

	public FightAPI(Character fighter, int fightAreaId) {
		this.character = fighter;
		this.fightOptions = new FightOptions(fightAreaId);
		this.lastFight = 0;
	}
	
	public FightAPI(Character fighter) {
		this(fighter, 0);
	}

	public int getFightAreaId() {
		return this.fightOptions.getFightAreaId();
	}

	public void updateFightArea(int level) {
		this.fightOptions.setFightArea(level);
	}
	
	public void fightAreaReplacementManager() {
		int mapsTravelledCounter = this.character.infos.getMapsTravelledCounter();
		if(mapsTravelledCounter - this.lastFight > 50) {
			this.lastFight = mapsTravelledCounter;
			this.fightOptions.replaceFightArea(this.character.infos.getLevel());
			this.character.mvt.goToArea(this.character.fight.getFightAreaId());
		}
	}

	public void levelUpManager() {
		if(this.character.inState(CharacterState.LEVEL_UP)) {
			upgradeSpell();
			increaseStats();
			if(this.character instanceof LoneFighter)
				this.fightOptions.setFightArea(this.character.infos.getLevel());
			else
				this.character.partyManager.incPartyLevel();
			this.character.updateState(CharacterState.LEVEL_UP, false);
		}
	}

	public void lifeManager() {
		int missingLife = this.character.infos.missingLife();
		if(missingLife > 0) {
			this.character.log.p("Break for life regeneration, " + missingLife + " life points missing.");
			this.character.waitState(CharacterState.IN_REGENERATION, this.character.infos.getRegenRate() * 100 * missingLife);
			this.character.infos.setLifePoints(this.character.infos.getMaxLifePoints());
		}
	}
	
	public void rebirthManager() {
		if(this.character.infos.getHealthState() == PlayerLifeStatusEnum.STATUS_TOMBSTONE) {
			this.character.net.send(new UnhandledMessage("GameRolePlayFreeSoulRequestMessage"));
			this.character.updateState(CharacterState.IS_LOADED, false);
			this.character.waitState(CharacterState.IS_LOADED);
		}
		if(this.character.infos.getHealthState() == PlayerLifeStatusEnum.STATUS_PHANTOM)
			this.character.interaction.useInteractive(287, 479466, false); // statue phénix
	}

	public void inventoryManager() {
		if(this.character.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.character.log.p("Need to empty inventory.");
			this.character.salesManager.npcSelling();
			if(!this.character.infos.inventoryIsFull(0.8f)) { // - de 80% de l'inventaire occupé
				this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
				return;
			}
			this.character.log.p("Need to go to empty inventory at Astrub bank.");
			this.character.mvt.goToInsideBank();
			this.character.interaction.openBankStorage();	
			this.character.exchangeManager.transfertAllObjectsFromInventory();
			int kamasInBank = this.character.bank.getKamas();
			if(kamasInBank > 0)
				this.character.exchangeManager.getKamasFromBank(kamasInBank);
			this.character.interaction.closeStorage();
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			if(this.character.infos.getLevel() >= 20)
				this.character.salesManager.bidHouseSelling();
		}
	}

	public boolean fightSearchManager() {
		this.character.log.p("Searching for monster group to fight.");
		int monsterGroupMaxSize = this.fightOptions.getMonsterGroupMaxSize();
		for(GameRolePlayGroupMonsterInformations monsterGroup : this.character.roleplayContext.getMonsterGroups()) {
			if(monsterGroup.staticInfos.underlings.length + 1 <= monsterGroupMaxSize) {
				// on se dirige vers le groupe de monstre et on l'attaque
				if(!attackMonsterGroup(monsterGroup))
					continue;
				while(!this.character.isInterrupted()) {
					// un combat a été lancé sur la map
					if(this.character.waitState(CharacterState.NEW_FIGHT_ON_MAP)) {
						// on est entré dans ce nouveau combat
						if(this.character.inState(CharacterState.IN_FIGHT))
							return true;
						// le groupe de monstre visé a été attaqué par quelqu'un d'autre
						if(this.character.roleplayContext.monsterGroupIsInFight(monsterGroup.contextualId))
							break;
						// le combat lancé ne concerne pas le groupe de monstre visé
					}
					// timeout => le groupe de monstre a bougé ou le combat met du temps à se lancer
					// on vérifie si le groupe de monstre a bougé
					else if(this.character.infos.getCurrentCellId() != monsterGroup.disposition.cellId)
						// si c'est le cas, on relance l'attaque
						if(!attackMonsterGroup(monsterGroup))
							break;
						// sinon on attend encore un peu
				}
			}
		}
		this.character.log.p("None monster group available or attackable on the map.");
		return false;
	}

	public void fightManager(boolean fightRecovery) {
		this.lastFight = this.character.infos.getMapsTravelledCounter();
		if(!fightRecovery) { // si c'est un combat tout frais
			GameFightReadyMessage GFRM = new GameFightReadyMessage();
			GFRM.isReady = true;
			this.character.net.send(GFRM);
		}
		while(!Thread.currentThread().isInterrupted() && this.character.inState(CharacterState.IN_FIGHT)) {
			this.character.waitState(CharacterState.IN_GAME_TURN); // attente du début du prochain tour ou de la fin du combat
			if(!this.character.inState(CharacterState.IN_FIGHT))
				break;
			Vector<GameFightMonsterInformations> aliveMonsters = this.character.fightContext.getAliveMonsters();
			this.character.log.p(aliveMonsters.size() + " alive monster(s) remaining.");
			for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
				if(this.character.inState(CharacterState.IN_GAME_TURN) && this.character.fightContext.self.stats.actionPoints >= this.character.infos.getAttackSpellActionPoints())
					castSpellOverMonster(aliveMonster.contextualId);
				else
					break;
			}
			if(this.character.inState(CharacterState.IN_FIGHT)) {
				GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
				this.character.net.send(GFTFM);
				this.character.updateState(CharacterState.IN_GAME_TURN, false);
			}
		}
		if(this.character.roleplayContext.lastFightOutcome) // si on a gagné le combat
			this.character.infos.incFightsWonCounter();
		else {
			this.character.infos.incFightsLostCounter();
			this.character.log.p("Fight lost.");
		}
		
		// pour être bien sûr
		this.character.waitState(CharacterState.IS_LOADED);
	}

	private void upgradeSpell() {
		int spellId = this.character.infos.getAttackSpell();
		HashMap<Integer, SpellItem> spellList = this.character.infos.getSpellList();
		if(spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			spellList.get(spellId).spellLevel++;
			SpellModifyRequestMessage SMRM = new SpellModifyRequestMessage();
			SMRM.spellId = spellId;
			SMRM.spellLevel = spellList.get(spellId).spellLevel;
			this.character.net.send(SMRM);
			this.character.log.p("Increasing attack spell to level " + spellList.get(spellId).spellLevel + ".");
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = this.character.infos.getSpellList().get(spellId).spellLevel;
		if(level < 5)
			return this.character.infos.getSpellsPoints() >= level;
		return false;
	}

	private void increaseStats() {
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.statId = this.character.infos.getElement();
		SURM.boostPoint = this.character.infos.calculateMaxStatsPoints();
		this.character.net.send(SURM);
		this.character.log.p("Increase stat : " + SURM.statId + ".");
	}
	
	private boolean attackMonsterGroup(GameRolePlayGroupMonsterInformations monsterGroup) {
		this.character.log.p("Going to take a monster group of size " + (monsterGroup.staticInfos.underlings.length + 1) + " on cell id " + monsterGroup.disposition.cellId + ".");
		if(!this.character.mvt.moveTo(monsterGroup.disposition.cellId)) // groupe de monstres inatteignable
			return false;
		this.character.log.p("Sending attack request.");
		GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
		GRPAMRM.monsterGroupId = monsterGroup.contextualId;
		this.character.net.send(GRPAMRM);
		return true;
	}

	private void castSpellOverMonster(double monsterId) {
		this.character.log.p("Trying to cast a spell over a monster.");
		GameActionFightCastOnTargetRequestMessage GAFCOTRM = new GameActionFightCastOnTargetRequestMessage();
		GAFCOTRM.spellId = this.character.infos.getAttackSpell();
		GAFCOTRM.targetId = monsterId;
		this.character.net.send(GAFCOTRM);
		/*
		GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
		this.fighter.net.send(GAFCRM);
		 */
		this.character.waitState(CharacterState.SPELL_CASTED);
		try {
			Thread.sleep(1000); // nécessaire sinon kick par le serveur
		} catch(Exception e) {
			Thread.currentThread().interrupt();
		}
	}
}