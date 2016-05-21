package controller.modules;

import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.enums.PlayerLifeStatusEnum;
import gamedata.fight.GameFightMonsterInformations;
import gamedata.inventory.SpellItem;

import java.util.HashMap;
import java.util.Vector;

import controller.CharacterState;
import controller.characters.Fighter;
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
	private Fighter fighter;
	private FightOptions fightOptions;

	public FightAPI(Fighter fighter, int fightAreaId) {
		this.fighter = fighter;
		this.fightOptions = new FightOptions(fightAreaId);
	}
	
	public FightAPI(Fighter fighter) {
		this(fighter, 0);
	}

	public int getFightAreaId() {
		return this.fightOptions.getFightAreaId();
	}

	public void updateFightArea(int level) {
		this.fightOptions.updateFightArea(level);
	}

	public void levelUpManager() {
		if(this.fighter.inState(CharacterState.LEVEL_UP)) {
			upgradeSpell();
			increaseStats();
			if(this.fighter instanceof LoneFighter)
				this.fightOptions.updateFightArea(this.fighter.infos.getLevel());
			else
				this.fighter.partyManager.incPartyLevel();
			this.fighter.updateState(CharacterState.LEVEL_UP, false);
		}
	}

	public void lifeManager() {
		int missingLife = this.fighter.infos.missingLife();
		if(missingLife > 0) {
			this.fighter.log.p("Break for life regeneration, " + missingLife + " life points missing.");
			this.fighter.waitState(CharacterState.IN_REGENERATION, this.fighter.infos.getRegenRate() * 100 * missingLife);
			this.fighter.infos.setLifePoints(this.fighter.infos.getMaxLifePoints());
		}
	}
	
	public void rebirthManager() {
		if(this.fighter.infos.getHealthState() == PlayerLifeStatusEnum.STATUS_TOMBSTONE) {
			this.fighter.net.send(new UnhandledMessage("GameRolePlayFreeSoulRequestMessage"));
			this.fighter.updateState(CharacterState.IS_LOADED, false);
			this.fighter.interaction.useInteractive(287, 479466, false); // status phénix
			try {
				Thread.sleep(2000); // TODO -> implémenter la réponse correspondante
			} catch(Exception e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void inventoryManager() {
		if(this.fighter.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.fighter.log.p("Need to empty inventory.");
			this.fighter.salesManager.npcSelling();
			if(!this.fighter.infos.inventoryIsFull(0.8f)) { // - de 80% de l'inventaire occupé
				this.fighter.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
				return;
			}
			this.fighter.exchangeManager.goToExchangeWithMule();
		}
	}

	public boolean fightSearchManager() {
		this.fighter.log.p("Searching for monster group to fight.");
		int monsterGroupSize;
		int monsterGroupMaxSize = this.fightOptions.getMonsterGroupMaxSize();
		for(GameRolePlayGroupMonsterInformations monsterGroup : this.fighter.roleplayContext.getMonsterGroups()) {
			monsterGroupSize = monsterGroup.staticInfos.underlings.size() + 1;
			if(monsterGroupSize <= monsterGroupMaxSize) {
				this.fighter.log.p("Going to take a monster group of size " + monsterGroupSize + " on cell id " + monsterGroup.disposition.cellId + ".");
				if(!this.fighter.mvt.moveTo(monsterGroup.disposition.cellId)) // groupe de monstres inatteignable
					continue;
				this.fighter.log.p("Sending attack request.");
				GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
				GRPAMRM.monsterGroupId = monsterGroup.contextualId;
				this.fighter.net.send(GRPAMRM);
				return true;
			}
		}
		this.fighter.log.p("None monster group available or attackable on the map.");
		return false;
	}

	public void fightManager(boolean fightRecovery) {
		if(!fightRecovery) { // si c'est un combat tout frais
			GameFightReadyMessage GFRM = new GameFightReadyMessage();
			GFRM.isReady = true;
			this.fighter.net.send(GFRM);
		}
		while(!Thread.currentThread().isInterrupted() && this.fighter.inState(CharacterState.IN_FIGHT)) {
			this.fighter.waitState(CharacterState.IN_GAME_TURN); // attente du début du prochain tour ou de la fin du combat
			if(!this.fighter.inState(CharacterState.IN_FIGHT))
				break;
			Vector<GameFightMonsterInformations> aliveMonsters = this.fighter.fightContext.getAliveMonsters();
			this.fighter.log.p(aliveMonsters.size() + " alive monster(s) remaining.");
			for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
				if(this.fighter.inState(CharacterState.IN_GAME_TURN) && this.fighter.fightContext.self.stats.actionPoints >= this.fighter.infos.getAttackSpellActionPoints())
					castSpellOverMonster(aliveMonster);
				else
					break;
			}
			if(this.fighter.inState(CharacterState.IN_FIGHT)) {
				GameFightTurnFinishMessage GFTFM = new GameFightTurnFinishMessage();
				this.fighter.net.send(GFTFM);
				this.fighter.updateState(CharacterState.IN_GAME_TURN, false);
			}
		}
		if(this.fighter.roleplayContext.lastFightOutcome) // si on a gagné le combat
			this.fighter.infos.incFightsWonCounter();
		else {
			this.fighter.infos.incFightsLostCounter();
			this.fighter.log.p("Fight lost.");
		}
		
		// pour être bien sûr
		this.fighter.waitState(CharacterState.IS_LOADED);
	}

	private void upgradeSpell() {
		int spellId = this.fighter.infos.getAttackSpell();
		HashMap<Integer, SpellItem> spellList = this.fighter.infos.getSpellList();
		if(spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			spellList.get(spellId).spellLevel++;
			SpellModifyRequestMessage SMRM = new SpellModifyRequestMessage();
			SMRM.spellId = spellId;
			SMRM.spellLevel = spellList.get(spellId).spellLevel;
			this.fighter.net.send(SMRM);
			this.fighter.log.p("Increasing attack spell to level " + spellList.get(spellId).spellLevel + ".");
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = this.fighter.infos.getSpellList().get(spellId).spellLevel;
		if(level < 5)
			return this.fighter.infos.getSpellsPoints() >= level;
		return false;
	}

	private void increaseStats() {
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.statId = this.fighter.infos.getElement();
		SURM.boostPoint = this.fighter.infos.calculateMaxStatsPoints();
		this.fighter.net.send(SURM);
		this.fighter.log.p("Increase stat : " + SURM.statId + ".");
	}

	private void castSpellOverMonster(GameFightMonsterInformations monster) {
		this.fighter.log.p("Trying to cast a spell over a monster.");
		GameActionFightCastOnTargetRequestMessage GAFCOTRM = new GameActionFightCastOnTargetRequestMessage();
		GAFCOTRM.spellId = this.fighter.infos.getAttackSpell();
		GAFCOTRM.targetId = monster.contextualId;
		this.fighter.net.send(GAFCOTRM);
		/*
		GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
		this.fighter.net.send(GAFCRM);
		 */
		this.fighter.waitState(CharacterState.SPELL_CASTED);
		try {
			Thread.sleep(1000); // nécessaire sinon kick par le serveur
		} catch(Exception e) {
			Thread.currentThread().interrupt();
		}
	}
}