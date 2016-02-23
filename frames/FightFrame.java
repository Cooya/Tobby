package frames;

import controller.CharacterState;
import controller.FighterController;
import main.Instance;
import messages.Message;
import messages.fight.GameActionAcknowledgementMessage;
import messages.fight.GameActionFightPointsVariationMessage;
import messages.fight.GameFightOptionStateUpdateMessage;
import messages.fight.GameFightOptionToggleMessage;
import messages.fight.GameFightSynchronizeMessage;
import messages.fight.GameFightTurnEndMessage;
import messages.fight.GameFightTurnReadyMessage;
import messages.fight.SequenceEndMessage;

public class FightFrame implements IFrame {
	private Instance instance;
	private FighterController fighter;

	public FightFrame(Instance instance, FighterController fighter) {
		this.instance = instance;
		this.fighter = fighter;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 715 : // tour du personnage suivant
				GameFightTurnReadyMessage GFTRM = new GameFightTurnReadyMessage(true);
				GFTRM.serialize();
				instance.outPush(GFTRM);
				this.instance.log.p("Next turn.");
				return true;
			case 6465 : // début du tour
				this.instance.log.p("Begin of my game turn.");
				this.fighter.updateState(CharacterState.IN_GAME_TURN, true);
				return true;
			case 719 : // fin du tour
				GameFightTurnEndMessage GFTEM = new GameFightTurnEndMessage(msg);
				GFTEM.deserialize();
				if(GFTEM.fighterId == this.fighter.infos.characterId)
					this.instance.log.p("End of my game turn.");
				return true;
			case 5921 : // synchronisation avec le serveur
				GameFightSynchronizeMessage GFSM = new GameFightSynchronizeMessage(msg);
				GFSM.deserialize();
				this.fighter.fightContext.setFightContext(GFSM.fighters);
				this.instance.log.p("Fight context set.");
				this.instance.log.p("Life points : " + this.fighter.fightContext.self.stats.lifePoints + "/" + this.fighter.fightContext.self.stats.maxLifePoints + ".");
				return true;
			case 720 : // fin du combat
				this.instance.log.p("End of fight.");
				this.instance.quitFight();
				this.fighter.updateState(CharacterState.IN_FIGHT, false);
				return true;
			case 956 : // action terminée
				SequenceEndMessage SEM = new SequenceEndMessage(msg);
				SEM.deserialize();
				if(SEM.authorId == this.fighter.infos.characterId) {
					GameActionAcknowledgementMessage GAAM = new GameActionAcknowledgementMessage(true, SEM.actionId);
					GAAM.serialize();
					instance.outPush(GAAM);
				}
				return true;
			case 1030 : // variation des points d'action
				GameActionFightPointsVariationMessage GAFPVM = new GameActionFightPointsVariationMessage(msg);
				GAFPVM.deserialize();
				this.fighter.fightContext.self.stats.actionPoints -= GAFPVM.delta;
				return true;
			case 5927 : // GameFightOptionStateUpdateMessage
				GameFightOptionStateUpdateMessage GFOSUM = new GameFightOptionStateUpdateMessage(msg);
				if(GFOSUM.option == 2) {
					if(!GFOSUM.state)
						this.instance.log.p("Fight already locked.");
					else {
						GameFightOptionToggleMessage GFOTM = new GameFightOptionToggleMessage();
						GFOTM.serialize(2); // lock (0 pour interdire les spectateurs)
						this.instance.outPush(GFOTM);
						this.instance.log.p("Fight locked.");
					}
				}
				return true;
		}
		return false;
	}
}