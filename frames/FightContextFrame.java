package frames;

import gamedata.fight.FightResultListEntry;
import gamedata.fight.FightResultPlayerListEntry;
import controller.CharacterState;
import controller.FighterController;
import main.Instance;
import messages.Message;
import messages.fights.GameActionAcknowledgementMessage;
import messages.fights.GameActionFightPointsVariationMessage;
import messages.fights.GameActionFightSpellCastMessage;
import messages.fights.GameFightEndMessage;
import messages.fights.GameFightShowFighterMessage;
import messages.fights.GameFightSynchronizeMessage;
import messages.fights.GameFightTurnEndMessage;
import messages.fights.GameFightTurnReadyMessage;
import messages.fights.SequenceEndMessage;

public class FightContextFrame extends Frame {
	private Instance instance;
	private FighterController fighter;

	public FightContextFrame(Instance instance, FighterController fighter) {
		this.instance = instance;
		this.fighter = fighter;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 700 : // GameFightStartingMessage
				this.instance.log.p("Starting fight.");
				/*
				if(this.fighter.infos.fightsWonCounter + this.fighter.infos.fightsLostCounter == 0) { // blocage automatique pour les combats suivants
					GameFightOptionToggleMessage GFOTM = new GameFightOptionToggleMessage();
					GFOTM.serialize(2); // 0 pour interdire les spectateurs
					this.instance.outPush(GFOTM);
					this.instance.log.p("Fight locked.");
				}
				*/
				return true;
			case 5864 : // GameFightShowFighterMessage
				GameFightShowFighterMessage GFSFM = new GameFightShowFighterMessage(msg);
				this.fighter.fightContext.newFighter(GFSFM.informations);
				this.fighter.updateState(CharacterState.NEW_ACTOR_IN_FIGHT, true);
				return true;
			case 715 : // GameFightTurnReadyRequestMessage
				GameFightTurnReadyMessage GFTRM = new GameFightTurnReadyMessage(true);
				GFTRM.serialize();
				instance.outPush(GFTRM);
				this.instance.log.p("Next turn.");
				return true;
			case 6465 : // GameFightTurnStartPlayingMessage
				this.instance.log.p("Begin of my game turn.");
				this.fighter.updateState(CharacterState.IN_GAME_TURN, true);
				return true;
			case 5921 : // GameFightSynchronizeMessage
				GameFightSynchronizeMessage GFSM = new GameFightSynchronizeMessage(msg);
				GFSM.deserialize();
				this.fighter.fightContext.setFightContext(GFSM.fighters);
				this.instance.log.p("Fight context set.");
				//this.instance.log.p("Life points : " + this.fighter.fightContext.self.stats.lifePoints + "/" + this.fighter.fightContext.self.stats.maxLifePoints + ".");
				this.instance.log.graphicalFrame.setLifeLabel(this.fighter.fightContext.self.stats.lifePoints, this.fighter.fightContext.self.stats.maxLifePoints);
				return true;
			case 1010 : // GameActionFightSpellCastMessage
				GameActionFightSpellCastMessage GAFSCM = new GameActionFightSpellCastMessage(msg);
				if(GAFSCM.sourceId == this.fighter.infos.characterId)
					this.fighter.updateState(CharacterState.SPELL_CASTED, true);
				return true;
			case 6132 : // GameActionFightNoSpellCastMessage
				this.fighter.updateState(CharacterState.SPELL_CASTED, true);
				return true;
			case 1030 : // GameActionFightPointsVariationMessage
				GameActionFightPointsVariationMessage GAFPVM = new GameActionFightPointsVariationMessage(msg);
				GAFPVM.deserialize();
				if(this.fighter.fightContext.self != null)
					this.fighter.fightContext.self.stats.actionPoints -= GAFPVM.delta;
				return true;
			case 956 : // SequenceEndMessage
				SequenceEndMessage SEM = new SequenceEndMessage(msg);
				SEM.deserialize();
				if(SEM.authorId == this.fighter.infos.characterId) {
					GameActionAcknowledgementMessage GAAM = new GameActionAcknowledgementMessage(true, SEM.actionId);
					GAAM.serialize();
					instance.outPush(GAAM);
				}
				return true;
			case 719 : // GameFightTurnEndMessage
				GameFightTurnEndMessage GFTEM = new GameFightTurnEndMessage(msg);
				GFTEM.deserialize();
				if(GFTEM.fighterId == this.fighter.infos.characterId)
					this.instance.log.p("End of my game turn.");
				return true;
			case 720 : // GameFightEndMessage
				GameFightEndMessage GFEM = new GameFightEndMessage(msg);
				for(FightResultListEntry result : GFEM.results)
					if(result instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) result).id == this.fighter.infos.characterId) {
						this.fighter.roleplayContext.lastFightOutcome = result.outcome == 2; // 2 = gagné, 0 = perdu
						break;
					}	
				this.instance.log.p("End of fight.");
				return true;
		}
		return false;
	}
}