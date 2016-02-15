package frames;

import main.CharacterController;
import main.Instance;
import messages.Message;
import messages.fight.GameActionAcknowledgementMessage;
import messages.fight.GameActionFightPointsVariationMessage;
import messages.fight.GameFightSynchronizeMessage;
import messages.fight.GameFightTurnEndMessage;
import messages.fight.GameFightTurnReadyMessage;
import messages.fight.SequenceEndMessage;

public class FightFrame implements Frame{

	private Instance instance;
	private CharacterController CC;

	public FightFrame(Instance instance, CharacterController CC) {
		this.instance = instance;
		this.CC = CC;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
		case 700 :
			//GameFightStartingMessage GFSM = new GameFightStartingMessage(msg);
			CC.fcontext.fight = true;
			return true;
		case 715:
			GameFightTurnReadyMessage GFTRM = new GameFightTurnReadyMessage(true);
			GFTRM.serialize();
			instance.outPush(GFTRM);
			return true;
		case 6465:
			CC.fcontext.turn=true;
			System.out.println("turn="+CC.fcontext.turn+" et inAction="+CC.fcontext.inAction);
			break;
		case 5921:
			System.out.println("Synchronized");
			GameFightSynchronizeMessage GFSM1=new GameFightSynchronizeMessage(msg);
			GFSM1.deserialize();
			CC.fcontext.newContextFightersInformations(GFSM1.fighters);
			CC.fcontext.nbMonstersAlive=CC.fcontext.getAliveMonsters().size();
			break;
		case 719:
			GameFightTurnEndMessage GFTEM=new GameFightTurnEndMessage(msg);
			GFTEM.deserialize();
			if(GFTEM.fighterId==CC.characterId)
				System.out.println("Fin de mon tour");
			CC.fcontext.turn=false;
			break;
		case 720:
			CC.fcontext.fight=false;
			CC.fcontext.turn=false;
			break;
		case 956:
			SequenceEndMessage SEM=new SequenceEndMessage(msg);
			SEM.deserialize();
			if(SEM.authorId==CC.characterId){
				System.out.println("Fin de l'action");
				GameActionAcknowledgementMessage GAAM=new GameActionAcknowledgementMessage(true,SEM.actionId);
				GAAM.serialize();
				instance.outPush(GAAM);
				CC.fcontext.inAction=false;
			}
			break;
		case 1030:
			GameActionFightPointsVariationMessage GAFPVM=new GameActionFightPointsVariationMessage(msg);
			GAFPVM.deserialize();
			CC.fcontext.selfInfo.stats.actionPoints-=GAFPVM.delta;
			break;
		}
		return false;
	}
}
