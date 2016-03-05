package frames;

import gamedata.fight.FightResultListEntry;
import gamedata.fight.FightResultPlayerListEntry;
import controller.CharacterState;
import controller.FighterController;
import main.Instance;
import messages.Message;
import messages.fight.GameActionAcknowledgementMessage;
import messages.fight.GameActionFightPointsVariationMessage;
import messages.fight.GameFightEndMessage;
import messages.fight.GameFightSynchronizeMessage;
import messages.fight.GameFightTurnEndMessage;
import messages.fight.GameFightTurnReadyMessage;
import messages.fight.SequenceEndMessage;

public class FightFrame extends Frame {
	private Instance instance;
	private FighterController fighter;

	public FightFrame(Instance instance, FighterController fighter) {
		this.instance = instance;
		this.fighter = fighter;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
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
			case 719 : // GameFightTurnEndMessage
				GameFightTurnEndMessage GFTEM = new GameFightTurnEndMessage(msg);
				GFTEM.deserialize();
				if(GFTEM.fighterId == this.fighter.infos.characterId)
					this.instance.log.p("End of my game turn.");
				return true;
			case 5921 : // GameFightSynchronizeMessage
				GameFightSynchronizeMessage GFSM = new GameFightSynchronizeMessage(msg);
				GFSM.deserialize();
				this.fighter.fightContext.setFightContext(GFSM.fighters);
				this.instance.log.p("Fight context set.");
				//this.instance.log.p("Life points : " + this.fighter.fightContext.self.stats.lifePoints + "/" + this.fighter.fightContext.self.stats.maxLifePoints + ".");
				this.instance.log.graphicalFrame.setLifeLabel(this.fighter.fightContext.self.stats.lifePoints, this.fighter.fightContext.self.stats.maxLifePoints);
				return true;
			case 720 : // GameFightEndMessage
				GameFightEndMessage GFEM = new GameFightEndMessage(msg);
				for(FightResultListEntry result : GFEM.results)
					if(result instanceof FightResultPlayerListEntry && ((FightResultPlayerListEntry) result).id == this.fighter.infos.characterId) {
						this.fighter.fightContext.lastFightOutcome = result.outcome == 2; // 2 = gagné, 0 = perdu
						break;
					}	
				this.instance.log.p("End of fight.");
				this.fighter.updateState(CharacterState.IS_LOADED, false);
				this.fighter.updateState(CharacterState.IN_FIGHT, false);
				this.fighter.updateState(CharacterState.IN_GAME_TURN, false);
				this.instance.quitFight();
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
			case 1030 : // GameActionFightPointsVariationMessage
				GameActionFightPointsVariationMessage GAFPVM = new GameActionFightPointsVariationMessage(msg);
				GAFPVM.deserialize();
				if(this.fighter.fightContext.self != null)
					this.fighter.fightContext.self.stats.actionPoints -= GAFPVM.delta;
				return true;
		}
		return false;
	}
}