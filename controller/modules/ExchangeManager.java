package controller.modules;

import controller.CharacterState;
import controller.characters.Character;
import messages.UnhandledMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;
import messages.exchanges.ExchangeObjectMoveMessage;
import messages.exchanges.ExchangePlayerRequestMessage;
import messages.exchanges.ExchangeReadyMessage;

public class ExchangeManager {
	private Character character;
	private int stepCounter;
	
	public ExchangeManager(Character character) {
		this.character = character;
		this.stepCounter = 0;
	}
	
	public void incStepCounter() {
		this.stepCounter++;
	}
	
	public void resetStepCounter() {
		this.stepCounter = 0;
	}
	
	public void putObjectIntoBank(int objectUID, int quantity) {
		ExchangeObjectMoveMessage msg = new ExchangeObjectMoveMessage();
		msg.objectUID = objectUID;
		msg.quantity = quantity;
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void getObjectFromBank(int objectUID, int quantity) {
		ExchangeObjectMoveMessage msg = new ExchangeObjectMoveMessage();
		msg.objectUID = objectUID;
		msg.quantity = -quantity; // quantité négative
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void putKamasIntoBank(int quantity) {
		ExchangeObjectMoveKamaMessage msg = new ExchangeObjectMoveKamaMessage();
		msg.quantity = quantity;
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void getKamasFromBank(int quantity) {
		ExchangeObjectMoveKamaMessage msg = new ExchangeObjectMoveKamaMessage();
		msg.quantity = -quantity; // quantité négative
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void transfertAllObjectsFromInventory() {
		// on transfère tous les objets de l'inventaire
		this.character.net.send(new UnhandledMessage("ExchangeObjectTransfertAllFromInvMessage"));

		// on attend la confirmation du transfert
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void validExchange() {
		// envoi du message de validation
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.ready = true;
		ERM.step = this.stepCounter;
		this.character.net.send(ERM);
		
		// attente de la fin de l'échange
		this.character.waitState(CharacterState.NOT_IN_EXCHANGE);
	}
	
	public void waitExchangeValidatedForValidExchange() {
		// attente de validation de l'échange du client
		this.character.waitState(CharacterState.EXCHANGE_VALIDATED_BY_PEER);
		
		// la mule valide l'échange à son tour
		validExchange();
	}

	// envoie une demande d'échange et attend qu'elle soit acceptée
	public boolean sendExchangeRequest(double characterId) {
		// envoi de la demande d'échange
		ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage();
		EPRM.target = characterId;
		EPRM.exchangeType = 1;
		this.character.net.send(EPRM);
		this.character.log.p("Sending exchange demand.");
		
		// attente du résultat de la requête
		if(!this.character.waitState(CharacterState.EXCHANGE_DEMAND_OUTCOME))
			return false; // timeout atteint avant la réception du résultat de la demande
		if(!this.character.roleplayContext.lastExchangeDemandOutcome)
			return false; // la requête de demande d'échange a échouée
		
		// attente de l'acceptation de la demande d'échange (avec timeout de 5 secondes)
		return this.character.waitState(CharacterState.IN_EXCHANGE);
	}
}