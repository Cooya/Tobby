package frames;

import main.CharacterController;
import main.Instance;
import messages.EmptyMessage;
import messages.Message;
import messages.context.CurrentMapMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
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

	public boolean processMessage(Message msg) {
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
				return true;
			case 220 :
				CurrentMapMessage CMM = new CurrentMapMessage(msg);
				CC.setCurrentMap(CMM.mapId);
				MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
				MIRM.serialize(CC.currentMap.id);
				instance.outPush(MIRM);
				return true;
			case 226 :
				MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
				CC.rcontext.newContextActors(MCIDM.actors);
				
				Log.p("Current map : " + MapPosition.getMapPositionById(CC.currentMap.id) + ".\nCurrent cell id : " + CC.currentCellId + ".");
				
				CC.makeCharacterAccessible(); // on peut maintenant bouger
				return true;
			case 5632 :
				GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
				CC.rcontext.addContextActor(GRPSAM.informations);
				return true;
			case 251 :
				GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
				CC.rcontext.removeContextActor(GCREM.id);
				return true;
			case 951 :
				GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
				CC.rcontext.updateContextActorPosition(GMMM.actorId, GMMM.keyMovements.lastElement());
				return true;
			case 3016 :
				/*InventoryContentMessage ICM = new InventoryContentMessage(msg);
				CC.kamasNumber = ICM.kamas;*/
				return true;
		}
		return false;
	}
}