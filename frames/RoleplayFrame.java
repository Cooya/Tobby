package frames;

import controller.CharacterController;
import controller.CharacterState;
import gamedata.d2o.modules.MapPosition;
import main.FatalError;
import main.Instance;
import main.InterClientKeyManager;
import messages.EmptyMessage;
import messages.Message;
import messages.character.CharacterLevelUpMessage;
import messages.character.CharacterStatsListMessage;
import messages.character.InventoryWeightMessage;
import messages.character.LifePointsRegenBeginMessage;
import messages.character.SpellListMessage;
import messages.context.CurrentMapMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.exchange.ExchangeIsReadyMessage;
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
		this.instance.log.refresh(CC.infos);
		switch(msg.getId()) {
		case 6471 : // CharacterLoadingCompleteMessage
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
			CKM.serialize(ICKM, this.instance.id);
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
		case 1200: // SpellListMessage
			SpellListMessage SLM = new SpellListMessage(msg);
			SLM.deserialize();
			CC.infos.loadSpellList(SLM.spells);
			return true;	
		case 220 : // CurrentMapMessage
			CurrentMapMessage CMM = new CurrentMapMessage(msg);
			this.CC.setCurrentMap(CMM.mapId);
			MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
			MIRM.serialize(this.CC.infos.currentMap.id);
			instance.outPush(MIRM);
			return true;
		case 500 : // CharacterStatsListMessage
			CharacterStatsListMessage CSLM = new CharacterStatsListMessage(msg);
			this.CC.infos.stats = CSLM.stats;
			return true;
		case 5670 : // CharacterLevelUpMessage
			CharacterLevelUpMessage CLUM = new CharacterLevelUpMessage(msg);
			this.CC.infos.level = CLUM.newLevel;
			this.CC.updateState(CharacterState.LEVEL_UP, true);
			return true;
		case 226 : // MapComplementaryInformationsDataMessage
			MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
			CC.roleplayContext.newContextActors(MCIDM.actors);
			this.instance.log.p("Current map : " + MapPosition.getMapPositionById(CC.infos.currentMap.id) + ".\nCurrent cell id : " + CC.infos.currentCellId + ".\nCurrent area id : " + CC.infos.currentMap.subareaId + ".");		
			this.CC.updateState(CharacterState.IS_LOADED, true);
			return true;
		case 5632 : // GameRolePlayShowActorMessage
			GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
			CC.roleplayContext.addContextActor(GRPSAM.informations);
			this.CC.updateState(CharacterState.NEW_ACTOR_ON_MAP, true);
			return true;
		case 251 : // GameContextRemoveElementMessage
			GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
			CC.roleplayContext.removeContextActor(GCREM.id);
			return true;
		case 951 : // GameMapMovementMessage
			GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
			int position = GMMM.keyMovements.lastElement();
			CC.roleplayContext.updateContextActorPosition(GMMM.actorId, position);
			if(GMMM.actorId == this.CC.infos.characterId) {
				this.CC.infos.currentCellId = position;
				this.instance.log.p("Current cell id updated : " + position + ".");
			}
			return true;
		case 5684 : // LifePointsRegenBeginMessage
			LifePointsRegenBeginMessage LPRBM = new LifePointsRegenBeginMessage(msg);
			this.CC.infos.regenRate = LPRBM.regenRate;
			return true;
		case 700 : // GameFightStartingMessage
			this.instance.log.p("Starting fight.");
			this.CC.updateState(CharacterState.IN_FIGHT, true);
			return true;
		case 954 : // GameMapNoMovementMessage
			throw new FatalError("Movement refused by server.");
		case 3009 : // InventoryWeightMessage
			InventoryWeightMessage IWM = new InventoryWeightMessage(msg);
			this.CC.infos.weight = IWM.weight;
			this.CC.infos.weightMax = IWM.weightMax;
			if(this.CC.infos.weightMaxAlmostReached()) {
				this.CC.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				this.instance.log.p("Inventory weight maximum almost reached, need to empty.");
			}
			return true;
		case 3016 : // InventoryContentMessage
			/*InventoryContentMessage ICM = new InventoryContentMessage(msg);
				CC.kamasNumber = ICM.kamas;*/
			return true;
		case 5523 : // ExchangeRequestedTradeMessage
			//ExchangeRequestedTradeMessage ERTM = new ExchangeRequestedTradeMessage(msg);
			this.CC.updateState(CharacterState.PENDING_DEMAND, true);
			return true;
		case 5628 : // ExchangeLeaveMessage
			//ExchangeLeaveMessage ELM = new ExchangeLeaveMessage(msg);
			this.CC.updateState(CharacterState.IN_EXCHANGE, false);
			return true;
		case 6129 : // ExchangeStartedWithPodsMessage
			this.CC.updateState(CharacterState.PENDING_DEMAND, false);
			this.CC.updateState(CharacterState.IN_EXCHANGE, true);
			return true;
		case 5509 : // ExchangeIsReadyMessage 
			ExchangeIsReadyMessage EIRM = new ExchangeIsReadyMessage(msg);
			if(EIRM.id != this.CC.infos.characterId)
				this.CC.updateState(CharacterState.EXCHANGE_VALIDATED, true);
			return true;
		}
		return false;
	}
}