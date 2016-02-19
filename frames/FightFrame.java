package frames;

import main.CharacterController;
import main.Event;
import main.Instance;
import messages.Message;
import messages.fight.GameActionAcknowledgementMessage;
import messages.fight.GameActionFightPointsVariationMessage;
import messages.fight.GameFightSynchronizeMessage;
import messages.fight.GameFightTurnEndMessage;
import messages.fight.GameFightTurnReadyMessage;
import messages.fight.SequenceEndMessage;

public class FightFrame implements IFrame {
	private Instance instance;
	private CharacterController CC;

	public FightFrame(Instance instance, CharacterController CC) {
		this.instance = instance;
		this.CC = CC;
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
				this.CC.emit(Event.GAME_TURN_START);
				return true;
			case 719 : // fin du tour
				GameFightTurnEndMessage GFTEM = new GameFightTurnEndMessage(msg);
				GFTEM.deserialize();
				if(GFTEM.fighterId == this.CC.infos.characterId)
					this.instance.log.p("End of my game turn.");
				return true;
			case 5921 : // synchronisation avec le serveur
				GameFightSynchronizeMessage GFSM = new GameFightSynchronizeMessage(msg);
				GFSM.deserialize();
				this.CC.fightContext.setFightContext(GFSM.fighters);
				this.instance.log.p("Fight context set.");
				this.instance.log.p("Life points : " + this.CC.fightContext.self.stats.lifePoints + "/" + this.CC.fightContext.self.stats.maxLifePoints + ".");
				return true;
			case 720 : // fin du combat
				this.instance.log.p("End of fight.");
				this.instance.quitFight();
				this.CC.infos.missingLife = this.CC.fightContext.missingLifePoints();
				return true;
			case 956 : // action terminée
				SequenceEndMessage SEM = new SequenceEndMessage(msg);
				SEM.deserialize();
				if(SEM.authorId == CC.infos.characterId) {
					GameActionAcknowledgementMessage GAAM = new GameActionAcknowledgementMessage(true, SEM.actionId);
					GAAM.serialize();
					instance.outPush(GAAM);
				}
				return true;
			case 1030 : // variation des points d'action
				GameActionFightPointsVariationMessage GAFPVM = new GameActionFightPointsVariationMessage(msg);
				GAFPVM.deserialize();
				CC.fightContext.self.stats.actionPoints -= GAFPVM.delta;
				return true;
		}
		return false;
	}
}