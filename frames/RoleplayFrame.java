package frames;

import game.InterClientKeyManager;
import game.d2o.modules.MapPosition;
import main.CharacterController;
import main.Event;
import main.Instance;
import messages.EmptyMessage;
import messages.Message;
import messages.character.LifePointsRegenBeginMessage;
import messages.context.CurrentMapMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.PrismsListRegisterMessage;

public class RoleplayFrame implements IFrame {
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
				MIRM.serialize(CC.infos.currentMap.id);
				instance.outPush(MIRM);
				return true;
			case 226 :
				MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
				CC.roleplayContext.newContextActors(MCIDM.actors);
				
				this.instance.log.p("Current map : " + MapPosition.getMapPositionById(CC.infos.currentMap.id) + ".\nCurrent cell id : " + CC.infos.currentCellId + ".\nCurrent area id : " + CC.infos.currentMap.subareaId + ".");
				
				this.CC.emit(Event.CHARACTER_LOADED);
				this.CC.emit(Event.FIGHT_END);
				return true;
			case 5632 :
				GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
				CC.roleplayContext.addContextActor(GRPSAM.informations);
				return true;
			case 251 :
				GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
				CC.roleplayContext.removeContextActor(GCREM.id);
				return true;
			case 951 :
				GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
				int position = GMMM.keyMovements.lastElement();
				CC.roleplayContext.updateContextActorPosition(GMMM.actorId, position);
				if(GMMM.actorId == this.CC.infos.characterId) {
					this.CC.infos.currentCellId = position;
					this.instance.log.p("Current cell id updated : " + position + ".");
				}
				return true;
			case 3016 :
				/*InventoryContentMessage ICM = new InventoryContentMessage(msg);
				CC.kamasNumber = ICM.kamas;*/
				return true;
			case 5684 : // LifePointsRegenBeginMessage
				LifePointsRegenBeginMessage LPRBM = new LifePointsRegenBeginMessage(msg);
				this.CC.infos.regenRate = LPRBM.regenRate;
				return true;
			case 700 : // début d'un combat
				this.instance.log.p("Starting fight.");
				this.CC.emit(Event.FIGHT_START);
				return true;
		}
		return false;
	}
}