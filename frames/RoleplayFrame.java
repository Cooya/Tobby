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
				MIRM.serialize(CC.getCurrentMapId());
				instance.outPush(MIRM);
				break;
			case 226 :
				MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
				CC.getContext().newContextActors(MCIDM.actors);
				
				Log.p("Current map : " + MapPosition.getMapPositionById(CC.getCurrentMapId()) + ".\nCurrent cell id : " + CC.getCurrentCellId() + ".");
				
				CC.makeCharacterAccessible(); // on peut maintenant bouger
				break;
			case 5632 :
				GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
				CC.getContext().addContextActor(GRPSAM.informations);
				break;
			case 251 :
				GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
				//CC.getContext().removeContextActor(GCREM.id);
				// problème d'accès concurrents
				break;
			case 951 :
				GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
				CC.getContext().updateContextActorPosition(GMMM.actorId, GMMM.keyMovements.lastElement());
				break;
		}
	}
}