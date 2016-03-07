package frames;

import controller.CharacterController;
import controller.CharacterState;
import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.MapsCache;
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
import messages.context.GameContextCreateMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.exchange.ExchangeLeaveMessage;
import messages.exchange.ExchangeRequestedTradeMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.PrismsListRegisterMessage;

public class RoleplayContextFrame extends Frame {
	private Instance instance;
	private CharacterController character;

	public RoleplayContextFrame(Instance instance, CharacterController character) {
		this.instance = instance;
		this.character = character;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
		case 6471 : // CharacterLoadingCompleteMessage
			this.instance.log.graphicalFrame.setFightsWonLabel(0);
			this.instance.log.graphicalFrame.setFightsLostLabel(0);
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
		case 200 : // GameContextCreateMessage
			GameContextCreateMessage GCCM = new GameContextCreateMessage(msg);
			if(GCCM.context == 1 && this.character.inState(CharacterState.IN_FIGHT)) {
				this.character.updateState(CharacterState.IN_FIGHT, false);
				this.character.updateState(CharacterState.IN_GAME_TURN, false);
				this.instance.quitFightContext();
			}
			else if(GCCM.context == 2) {
				this.character.updateState(CharacterState.IS_LOADED, false);
				this.character.updateState(CharacterState.IN_FIGHT, true);
				this.instance.startFightContext();
			}
			return true;
		case 1200 : // SpellListMessage
			SpellListMessage SLM = new SpellListMessage(msg);
			SLM.deserialize();
			this.character.infos.loadSpellList(SLM.spells);
			return true;	
		case 220 : // CurrentMapMessage
			CurrentMapMessage CMM = new CurrentMapMessage(msg);
			this.character.infos.currentMap = MapsCache.loadMap(CMM.mapId);
			MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
			MIRM.serialize(this.character.infos.currentMap.id);
			instance.outPush(MIRM);
			return true;
		case 500 : // CharacterStatsListMessage
			CharacterStatsListMessage CSLM = new CharacterStatsListMessage(msg);
			this.character.infos.stats = CSLM.stats;
			this.instance.log.graphicalFrame.setKamasLabel(this.character.infos.stats.kamas);
			this.instance.log.graphicalFrame.setExperienceLabel((int) this.character.infos.stats.experience, (int) this.character.infos.stats.experienceNextLevelFloor);
			return true;
		case 5670 : // CharacterLevelUpMessage
			CharacterLevelUpMessage CLUM = new CharacterLevelUpMessage(msg);
			this.character.infos.level = CLUM.newLevel;
			this.instance.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
			this.character.updateState(CharacterState.LEVEL_UP, true);
			return true;
		case 226 : // MapComplementaryInformationsDataMessage
			MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
			this.character.roleplayContext.newContextActors(MCIDM.actors);
			this.instance.log.p("Current map : " + MapPosition.getMapPositionById(this.character.infos.currentMap.id) + ".\nCurrent cell id : " + this.character.infos.currentCellId + ".\nCurrent area id : " + this.character.infos.currentMap.subareaId + ".");
			this.instance.log.graphicalFrame.setMapLabel(String.valueOf(MapPosition.getMapPositionById(this.character.infos.currentMap.id)));
			this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
			this.character.updatePosition(this.character.infos.currentMap, this.character.infos.currentCellId);
			this.character.updateState(CharacterState.IS_LOADED, true);
			return true;
		case 5632 : // GameRolePlayShowActorMessage
			GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
			this.character.roleplayContext.addContextActor(GRPSAM.informations);
			this.character.updateState(CharacterState.NEW_ACTOR_ON_MAP, true);
			return true;
		case 251 : // GameContextRemoveElementMessage
			GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
			this.character.roleplayContext.removeContextActor(GCREM.id);
			return true;
		case 951 : // GameMapMovementMessage
			GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
			int position = GMMM.keyMovements.lastElement();
			this.character.roleplayContext.updateContextActorPosition(GMMM.actorId, position);
			if(GMMM.actorId == this.character.infos.characterId) {
				this.character.infos.currentCellId = position;
				this.instance.log.p("Next cell id after movement : " + position + ".");
				this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
				this.character.updatePosition(this.character.infos.currentCellId);
				this.character.updateState(CharacterState.CAN_MOVE, true);
			}
			return true;
		case 5684 : // LifePointsRegenBeginMessage
			LifePointsRegenBeginMessage LPRBM = new LifePointsRegenBeginMessage(msg);
			this.character.infos.regenRate = LPRBM.regenRate;
			return true;
		case 954 : // GameMapNoMovementMessage
			throw new FatalError("Movement refused by server.");
		case 3009 : // InventoryWeightMessage
			InventoryWeightMessage IWM = new InventoryWeightMessage(msg);
			this.character.infos.weight = IWM.weight;
			this.character.infos.weightMax = IWM.weightMax;
			this.instance.log.graphicalFrame.setWeightLabel(this.character.infos.weight, this.character.infos.weightMax);
			if(this.character.infos.weightMaxAlmostReached()) {
				this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
				this.instance.log.p("Inventory weight maximum almost reached, need to empty.");
			}
			return true;
		case 5523 : // ExchangeRequestedTradeMessage
			ExchangeRequestedTradeMessage ERTM = new ExchangeRequestedTradeMessage(msg);
			this.character.roleplayContext.actorDemandingExchange = ERTM.source;
			this.character.updateState(CharacterState.PENDING_DEMAND, true);
			return true;
		case 6129 : // ExchangeStartedWithPodsMessage
			this.instance.startExchangeContext();
			this.character.updateState(CharacterState.PENDING_DEMAND, false);
			this.character.updateState(CharacterState.IN_EXCHANGE, true);
			return true;
		case 5628 : // ExchangeLeaveMessage
			ExchangeLeaveMessage ELM = new ExchangeLeaveMessage(msg);
			this.character.roleplayContext.lastExchangeResult = ELM.success;
			if(this.character.inState(CharacterState.IN_EXCHANGE)) { // on quitte un échange
				this.instance.quitExchangeContext();
				this.character.updateState(CharacterState.IN_EXCHANGE, false);
			}
			else // on refuse un échange
				this.character.updateState(CharacterState.PENDING_DEMAND, false);
			return true;
		}
		return false;
	}
}