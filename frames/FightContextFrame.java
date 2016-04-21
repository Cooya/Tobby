package frames;

import gamedata.fight.FightResultListEntry;
import gamedata.fight.FightResultPlayerListEntry;
import controller.CharacterState;
import controller.characters.Fighter;
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

	public FightContextFrame(Instance instance, Fighter character) {
		super(instance, character);
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
				((Fighter) this.character).fightContext.newFighter(GFSFM.informations);
				this.character.updateState(CharacterState.NEW_ACTOR_IN_FIGHT, true);
				return true;
			case 715 : // GameFightTurnReadyRequestMessage
				GameFightTurnReadyMessage GFTRM = new GameFightTurnReadyMessage(true);
				GFTRM.serialize();
				instance.outPush(GFTRM);
				this.instance.log.p("Next turn.");
				return true;
			case 6465 : // GameFightTurnStartPlayingMessage
				this.instance.log.p("Begin of my game turn.");
				this.character.updateState(CharacterState.IN_GAME_TURN, true);
				return true;
			case 5921 : // GameFightSynchronizeMessage
				GameFightSynchronizeMessage GFSM = new GameFightSynchronizeMessage(msg);
				Fighter fighter = (Fighter) this.character;
				fighter.fightContext.setFightContext(GFSM.fighters);
				this.instance.log.p("Fight context set.");
				//this.instance.log.p("Life points : " + this.fighter.fightContext.self.stats.lifePoints + "/" + this.fighter.fightContext.self.stats.maxLifePoints + ".");
				this.instance.log.graphicalFrame.setLifeLabel(fighter.fightContext.self.stats.lifePoints, fighter.fightContext.self.stats.maxLifePoints);
				return true;
			case 1010 : // GameActionFightSpellCastMessage
				GameActionFightSpellCastMessage GAFSCM = new GameActionFightSpellCastMessage(msg);
				if(GAFSCM.sourceId == this.character.infos.characterId)
					this.character.updateState(CharacterState.SPELL_CASTED, true);
				return true;
			case 6132 : // GameActionFightNoSpellCastMessage
				this.character.updateState(CharacterState.SPELL_CASTED, true);
				return true;
			case 1030 : // GameActionFightPointsVariationMessage
				GameActionFightPointsVariationMessage GAFPVM = new GameActionFightPointsVariationMessage(msg);
				GAFPVM.deserialize();
				if(((Fighter) this.character).fightContext.self != null)
					((Fighter) this.character).fightContext.self.stats.actionPoints -= GAFPVM.delta;
				return true;
			case 956 : // SequenceEndMessage
				SequenceEndMessage SEM = new SequenceEndMessage(msg);
				SEM.deserialize();
				if(SEM.authorId == this.character.infos.characterId) {
					GameActionAcknowledgementMessage GAAM = new GameActionAcknowledgementMessage(true, SEM.actionId);
					GAAM.serialize();
					instance.outPush(GAAM);
				}
				return true;
			case 719 : // GameFightTurnEndMessage
				GameFightTurnEndMessage GFTEM = new GameFightTurnEndMessage(msg);
				GFTEM.deserialize();
				if(GFTEM.fighterId == this.character.infos.characterId) {
					this.character.updateState(CharacterState.IN_GAME_TURN, false);
					this.instance.log.p("End of my game turn.");
				}
				return true;
			case 720 : // GameFightEndMessage
				GameFightEndMessage GFEM = new GameFightEndMessage(msg);
				for(FightResultListEntry result : GFEM.results)
					if(result instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) result).id == this.character.infos.characterId) {
						this.character.roleplayContext.lastFightOutcome = result.outcome == 2; // 2 = gagné, 0 = perdu
						break;
					}	
				this.instance.log.p("End of fight.");
				return true;
		}
		return false;
	}
}