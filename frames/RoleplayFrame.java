package frames;

import main.CharacterController;
import main.Instance;
import messages.EmptyMessage;
import messages.Message;
import messages.character.InventoryContentMessage;
import messages.context.CurrentMapMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.fight.GameActionAcknowledgementMessage;
import messages.fight.GameActionFightPointsVariationMessage;
import messages.fight.GameFightStartingMessage;
import messages.fight.GameFightSynchronizeMessage;
import messages.fight.GameFightTurnEndMessage;
import messages.fight.GameFightTurnReadyMessage;
import messages.fight.SequenceEndMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.PrismsListRegisterMessage;
import roleplay.InterClientKeyManager;
import roleplay.d2o.modules.MapPosition;
import utilities.Log;

public class RoleplayFrame implements Frame {
	private Instance instance;
	private CharacterController CC;

	public RoleplayFrame(Instance instance, CharacterController CC) {
		this.instance = instance;
		this.CC = CC;
	}

	public void processMessage(Message msg) {
		switch(msg.getId()) {
		case 6471 :
			InterClientKeyManager ICKM = InterClientKeyManager.getInstance();
			ICKM.getKey();
			EmptyMessage EM1 = new EmptyMessage("FriendsGetListMessage");
			EmptyMessage EM2 = new EmptyMessage("IgnoredGetListMessage");
			EmptyMessage EM3 = new EmptyMessage("SpouseGetInformationsMessage");
			EmptyMessage EM4 = new EmptyMessage("GameContextCreateRequestMessage");
			//EmptyMessage EM5 = new EmptyMessage("ObjectAveragePricesGetMessage");
			EmptyMessage EM6 = new EmptyMessage("QuestListRequestMessage");
			PrismsListRegisterMessage PLRM = new PrismsListRegisterMessage();
			PLRM.serialize();
			ChannelEnablingMessage CEM = new ChannelEnablingMessage();
			CEM.serialize();
			ClientKeyMessage CKM = new ClientKeyMessage();
			CKM.serialize(ICKM);
			instance.outPush(EM1);
			instance.outPush(EM2);
			instance.outPush(EM3);
			instance.outPush(CKM);
			instance.outPush(EM4);
			//instance.outPush(EM5);
			instance.outPush(EM6);
			instance.outPush(PLRM);
			instance.outPush(CEM);
			break;
		case 220 :
			CurrentMapMessage CMM = new CurrentMapMessage(msg);
			CC.setCurrentMap(CMM.mapId);
			MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
			MIRM.serialize(CC.currentMap.id);
			instance.outPush(MIRM);
			break;
		case 226 :
			MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
			CC.context.newContextActors(MCIDM.actors);

			Log.p("Current map : " + MapPosition.getMapPositionById(CC.currentMap.id) + ".\nCurrent cell id : " + CC.currentCellId + ".");

			CC.makeCharacterAccessible(); // on peut maintenant bouger
			break;
		case 5632 :
			GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
			CC.context.addContextActor(GRPSAM.informations);
			break;
		case 251 :
			GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
			CC.context.removeContextActor(GCREM.id);
			break;
		case 951 :
			GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
			CC.context.updateContextActorPosition(GMMM.actorId, GMMM.keyMovements.lastElement());
			break;
		case 3016 :
			InventoryContentMessage ICM = new InventoryContentMessage(msg);
			CC.kamasNumber = ICM.kamas;
			break;
		case 700:
			GameFightStartingMessage GFSM=new GameFightStartingMessage(msg);
			CC.context.fight=true;
			break;
		case 715:
			GameFightTurnReadyMessage GFTRM=new GameFightTurnReadyMessage(true);
			GFTRM.serialize();
			instance.outPush(GFTRM);
			break;
		case 6465:
			CC.context.turn=true;
			System.out.println("turn="+CC.context.turn+" et inAction="+CC.context.inAction);
			break;
		case 5921:
			System.out.println("Synchronized");
			GameFightSynchronizeMessage GFSM1=new GameFightSynchronizeMessage(msg);
			GFSM1.deserialize();
			CC.context.newContextFightersInformations(GFSM1.fighters);
			CC.context.nbMonstersAlive=CC.context.getAliveMonsters().size();
			break;
		case 719:
			GameFightTurnEndMessage GFTEM=new GameFightTurnEndMessage(msg);
			GFTEM.deserialize();
			if(GFTEM.fighterId==CC.characterId)
				System.out.println("Fin de mon tour");
			CC.context.turn=false;
			break;
		case 720:
			CC.context.fight=false;
			CC.context.turn=false;
			break;
		case 956:
			SequenceEndMessage SEM=new SequenceEndMessage(msg);
			SEM.deserialize();
			if(SEM.authorId==CC.characterId){
				System.out.println("Fin de l'action");
				GameActionAcknowledgementMessage GAAM=new GameActionAcknowledgementMessage(true,SEM.actionId);
				GAAM.serialize();
				instance.outPush(GAAM);
				CC.context.inAction=false;
			}
			break;
		case 1030:
			GameActionFightPointsVariationMessage GAFPVM=new GameActionFightPointsVariationMessage(msg);
			GAFPVM.deserialize();
			if(GAFPVM.sourceId==CC.characterId)
				CC.context.selfInfo.stats.actionPoints-=GAFPVM.delta;
			break;
		}
	}
}
