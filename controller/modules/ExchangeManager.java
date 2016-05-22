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
		msg.quantity = -quantity; // quantit� n�gative
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
		msg.quantity = -quantity; // quantit� n�gative
		this.character.net.send(msg);
		
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	public void transfertAllObjectsFromInventory() {
		// on transf�re tous les objets de l'inventaire
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
		
		// attente de la fin de l'�change
		this.character.waitState(CharacterState.NOT_IN_EXCHANGE);
	}
	
	public void waitExchangeValidatedForValidExchange() {
		// attente de validation de l'�change du client
		this.character.waitState(CharacterState.EXCHANGE_VALIDATED_BY_PEER);
		
		// la mule valide l'�change � son tour
		validExchange();
	}

	// envoie une demande d'�change et attend qu'elle soit accept�e
	public boolean sendExchangeRequest(double characterId) {
		// envoi de la demande d'�change
		ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage();
		EPRM.target = characterId;
		EPRM.exchangeType = 1;
		this.character.net.send(EPRM);
		this.character.log.p("Sending exchange demand.");
		
		// attente du r�sultat de la requ�te
		if(!this.character.waitState(CharacterState.EXCHANGE_DEMAND_OUTCOME))
			return false; // timeout atteint avant la r�ception du r�sultat de la demande
		if(!this.character.roleplayContext.lastExchangeDemandOutcome)
			return false; // la requ�te de demande d'�change a �chou�e
		
		// attente de l'acceptation de la demande d'�change (avec timeout de 5 secondes)
		return this.character.waitState(CharacterState.IN_EXCHANGE);
	}
}