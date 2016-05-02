package controller.api;

import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.enums.PlayerLifeStatusEnum;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.CharacterState;
import controller.FightOptions;
import controller.characters.Fighter;
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
		this.fighter.log.graphicalFrame.setAreaLabel(this.fightOptions.getFightAreaId());
	}

	public void levelUpManager() {
		if(!this.fighter.inState(CharacterState.LEVEL_UP))
			return;
		this.fighter.waitState(CharacterState.IS_LOADED);
		upgradeSpell();
		increaseStats();
		this.fighter.partyManager.incPartyLevel();
	}

	public void lifeManager() {
		this.fighter.waitState(CharacterState.IS_LOADED);

		int missingLife = this.fighter.infos.missingLife();
		if(missingLife > 0) {
			this.fighter.log.p("Break for life regeneration, " + missingLife + " life points missing.");
			this.fighter.waitState(CharacterState.IN_REGENERATION, this.fighter.infos.regenRate * 100 * missingLife);
			this.fighter.infos.stats.lifePoints = this.fighter.infos.stats.maxLifePoints;
			this.fighter.log.graphicalFrame.setLifeLabel(this.fighter.infos.stats.lifePoints, this.fighter.infos.stats.maxLifePoints);
		}
	}
	
	public void rebirthManager() {
		if(this.fighter.infos.healthState == PlayerLifeStatusEnum.STATUS_TOMBSTONE) {
			this.fighter.net.send(new UnhandledMessage("GameRolePlayFreeSoulRequestMessage"));
			this.fighter.updateState(CharacterState.IS_LOADED, false);
			this.fighter.interaction.useInteractive(287, 479466, 152192, false);
			try {
				Thread.sleep(2000); // temporaire
			} catch(Exception e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void inventoryManager() {
		if(this.fighter.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.fighter.log.p("Need to empty inventory.");
			this.fighter.social.goToExchangeWithMule();
		}
	}

	public boolean fightSearchManager() {
		this.fighter.waitState(CharacterState.IS_LOADED);

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
			this.fighter.waitState(CharacterState.IN_GAME_TURN); // attente du d�but du prochain tour ou de la fin du combat
			if(!this.fighter.inState(CharacterState.IN_FIGHT))
				break;
			Vector<GameFightMonsterInformations> aliveMonsters = this.fighter.fightContext.getAliveMonsters();
			this.fighter.log.p(aliveMonsters.size() + " alive monster(s) remaining.");
			for(GameFightMonsterInformations aliveMonster : aliveMonsters) {
				if(this.fighter.inState(CharacterState.IN_GAME_TURN) && this.fighter.fightContext.self.stats.actionPoints >= this.fighter.infos.attackSpellActionPoints)
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
		if(this.fighter.roleplayContext.lastFightOutcome) { // si on a gagn� le combat
			this.fighter.infos.fightsWonCounter++;
			this.fighter.log.graphicalFrame.setFightsWonLabel(this.fighter.infos.fightsWonCounter);
		}
		else {
			this.fighter.infos.fightsLostCounter++;
			this.fighter.log.graphicalFrame.setFightsLostLabel(this.fighter.infos.fightsLostCounter);
			this.fighter.log.p("Fight lost.");
		}
	}

	private void upgradeSpell() {
		int spellId = this.fighter.infos.attackSpell;
		if(this.fighter.infos.spellList.get(spellId) != null && canUpgradeSpell(spellId)) {
			this.fighter.infos.spellList.get(spellId).spellLevel++;
			SpellModifyRequestMessage SMRM = new SpellModifyRequestMessage();
			SMRM.spellId = spellId;
			SMRM.spellLevel = this.fighter.infos.spellList.get(spellId).spellLevel;
			this.fighter.net.send(SMRM);
			this.fighter.log.p("Increasing attack spell to level " + this.fighter.infos.spellList.get(spellId).spellLevel + ".");
		}
	}

	private boolean canUpgradeSpell(int spellId) {
		int level = this.fighter.infos.spellList.get(spellId).spellLevel;
		if(level < 5)
			return this.fighter.infos.stats.spellsPoints >= level;
			return false;
	}

	private void increaseStats() {
		StatsUpgradeRequestMessage SURM = new StatsUpgradeRequestMessage();
		SURM.statId = this.fighter.infos.element;
		SURM.boostPoint = calculateMaxStatsPoints();
		this.fighter.net.send(SURM);
		this.fighter.log.p("Increase stat : " + this.fighter.infos.element + ".");
	}

	private int calculateMaxStatsPoints() {
		int stage = (getElementInfoById() / 100) + 1;
		return this.fighter.infos.stats.statsPoints - (this.fighter.infos.stats.statsPoints % stage);
	}

	private int getElementInfoById() {
		switch(this.fighter.infos.element) {
			case 10 : return this.fighter.infos.stats.strength.base;
			case 13 : return this.fighter.infos.stats.chance.base;
			case 14 : return this.fighter.infos.stats.agility.base;
			case 15 : return this.fighter.infos.stats.intelligence.base;
		}
		return 0;
	}

	private void castSpellOverMonster(GameFightMonsterInformations monster) {
		this.fighter.log.p("Trying to cast a spell over a monster.");
		GameActionFightCastOnTargetRequestMessage GAFCOTRM = new GameActionFightCastOnTargetRequestMessage();
		GAFCOTRM.spellId = this.fighter.infos.attackSpell;
		GAFCOTRM.targetId = monster.contextualId;
		this.fighter.net.send(GAFCOTRM);
		/*
		GameActionFightCastRequestMessage GAFCRM = new GameActionFightCastRequestMessage();
		this.fighter.net.send(GAFCRM);
		 */
		this.fighter.waitState(CharacterState.SPELL_CASTED);
		try {
			Thread.sleep(1000); // n�cessaire sinon kick par le serveur
		} catch(Exception e) {
			Thread.currentThread().interrupt();
		}
	}
}