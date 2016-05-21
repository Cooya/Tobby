package frames;

import controller.CharacterState;
import controller.characters.Character;
import messages.exchanges.ExchangeBidHouseItemAddOkMessage;
import messages.exchanges.ExchangeBidHouseItemRemoveOkMessage;
import messages.exchanges.ExchangeBidPriceForSellerMessage;
import messages.exchanges.ExchangeErrorMessage;
import messages.exchanges.ExchangeIsReadyMessage;
import messages.exchanges.ExchangeKamaModifiedMessage;
import messages.exchanges.ExchangeLeaveMessage;
import messages.exchanges.ExchangeObjectAddedMessage;
import messages.exchanges.ExchangeObjectModifiedMessage;
import messages.exchanges.ExchangeObjectRemovedMessage;
import messages.exchanges.ExchangeObjectsAddedMessage;
import messages.exchanges.ExchangeObjectsModifiedMessage;
import messages.exchanges.ExchangeObjectsRemovedMessage;
import messages.exchanges.ExchangePodsModifiedMessage;
import messages.exchanges.ExchangeRequestedTradeMessage;
import messages.exchanges.ExchangeSellOkMessage;
import messages.exchanges.ExchangeStartOkNpcShopMessage;
import messages.exchanges.ExchangeStartedBidSellerMessage;
import messages.exchanges.ExchangeStartedWithPodsMessage;
import messages.exchanges.ExchangeStartedWithStorageMessage;
import messages.exchanges.StorageInventoryContentMessage;
import messages.exchanges.StorageKamasUpdateMessage;
import messages.exchanges.StorageObjectRemoveMessage;
import messages.exchanges.StorageObjectUpdateMessage;
import messages.exchanges.StorageObjectsRemoveMessage;
import messages.exchanges.StorageObjectsUpdateMessage;

public class ExchangeContextFrame extends Frame {
	
	public ExchangeContextFrame(Character character) {
		super(character);
	}
	
	protected void process(ExchangeRequestedTradeMessage msg) {
		this.character.log.p("Exchange demand dialog displayed");
		this.character.roleplayContext.lastExchangeDemandOutcome = true; // utile pour l'émetteur de la demande
		this.character.roleplayContext.actorDemandingExchange = msg.source;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
		this.character.updateState(CharacterState.PENDING_DEMAND, true);
	}
	
	protected void process(ExchangeStartedWithPodsMessage msg) {
		this.character.log.p("Exchange with pods started.");
		this.character.updateState(CharacterState.PENDING_DEMAND, false);
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeStartedWithStorageMessage msg) {
		this.character.log.p("Exchange with storage started.");
		this.character.exchangeManager.resetStepCounter();
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeStartedBidSellerMessage msg) {
		this.character.salesManager.setObjectsInBid(msg.objectsInfos);
		this.character.salesManager.setInfos(msg.sellerDescriptor);
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeStartOkNpcShopMessage msg) {
		this.character.log.p("Exchange with npc started.");
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	// reçu aussi à la fin d'un échange pour chaque perso
	protected void process(ExchangeIsReadyMessage msg) {
		if(msg.id != this.character.infos.getCharacterId()) {
			if(msg.ready) {
				this.character.log.p("Exchange validated by peer.");
				this.character.updateState(CharacterState.EXCHANGE_VALIDATED_BY_PEER, true);
			}
			else
				this.character.updateState(CharacterState.EXCHANGE_VALIDATED_BY_PEER, false);
		}
		else
			if(msg.ready)
				this.character.log.p("Exchange validated from my side.");
	}
	
	protected void process(ExchangeLeaveMessage msg) {
		this.character.roleplayContext.lastExchangeOutcome = msg.success;
		if(this.character.inState(CharacterState.IN_EXCHANGE)) // on a quitté un échange
			this.character.updateState(CharacterState.IN_EXCHANGE, false);
		else { // on a refusé un échange
			this.character.roleplayContext.actorDemandingExchange = 0;
			this.character.updateState(CharacterState.PENDING_DEMAND, false);
		}
	}
	
	protected void process(ExchangeErrorMessage msg) {
		this.character.log.p("Exchange demand failed.");
		this.character.roleplayContext.lastExchangeDemandOutcome = false;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
	}
	
	protected void process(ExchangeObjectModifiedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeObjectsModifiedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeObjectAddedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeObjectsAddedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeObjectRemovedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeObjectsRemovedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangeKamaModifiedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(ExchangePodsModifiedMessage msg) {
		this.character.exchangeManager.incStepCounter();
	}
	
	protected void process(StorageInventoryContentMessage msg) {
		this.character.bank.setObjects(msg.objects);
		this.character.bank.setKamas(msg.kamas);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(StorageObjectUpdateMessage msg) {
		this.character.bank.addObject(msg.object);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(StorageObjectsUpdateMessage msg) {
		this.character.inventory.addObjects(msg.objectList);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(StorageObjectRemoveMessage msg) {
		this.character.bank.removeObject(msg.objectUID);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(StorageObjectsRemoveMessage msg) {
		this.character.bank.removeObjects(msg.objectUIDList);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(StorageKamasUpdateMessage msg) {
		this.character.bank.setKamas(msg.kamasTotal);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(ExchangeBidPriceForSellerMessage msg) {
		this.character.salesManager.setAskedInfos(msg);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(ExchangeBidHouseItemAddOkMessage msg) {
		this.character.salesManager.objectToSellAdded(msg.itemInfo);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(ExchangeBidHouseItemRemoveOkMessage msg) {
		this.character.salesManager.objectToSellRemoved(msg.sellerId);
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
	
	protected void process(ExchangeSellOkMessage msg) {
		this.character.updateState(CharacterState.EXCHANGE_ACTION_RESPONSE, true);
	}
}