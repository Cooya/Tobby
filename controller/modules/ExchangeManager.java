package controller.modules;

import gamedata.enums.PlayerStatusEnum;
import controller.CharacterState;
import controller.characters.Character;
import controller.characters.Fighter;
import controller.characters.Mule;
import main.FatalError;
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
	
	// se rend � la map d'attente de la mule et lui transmet tous les objets de l'inventaire et les kamas
	public void goToExchangeWithMule() {
		// attente de la connexion de la mule si elle n'est pas connect�e
		this.character.waitState(CharacterState.MULE_ONLINE);
		Mule mule = ((Fighter) character).getMule();
		
		// aller sur la map d'attente de la mule
		this.character.mvt.goTo(mule.getWaitingMapId(), false);
		
		// tentative d'�change avec elle
		while(!Thread.currentThread().isInterrupted()) {
			if(!this.character.roleplayContext.actorIsOnMap(mule.infos.getCharacterId())) // si la mule n'est pas sur la map
				this.character.waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
			else {
				this.character.waitState(CharacterState.MULE_AVAILABLE);
				if(sendExchangeRequest(mule.infos.getCharacterId())) // si l'�change a �t� accept�
					break;
				try {
					Thread.sleep(5000); // pour pas flooder de demandes d'�change
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
		
		// transfert de tous les objets de l'inventaire
		this.character.log.p("Transfering all objects from inventory.");
		this.character.net.send(new UnhandledMessage("ExchangeObjectTransfertAllFromInvMessage"));
		
		// les kamas aussi
		ExchangeObjectMoveKamaMessage EOMKM = new ExchangeObjectMoveKamaMessage();
		EOMKM.quantity = this.character.inventory.getKamas();
		this.character.net.send(EOMKM);
		
		// on attend de pouvoir valider l'�change (bouton bloqu� pendant 3 secondes apr�s chaque action)
		try {
			Thread.sleep(3000);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		// validation de l'�change c�t� combattant
		validExchange();
		
		// et agissement en cons�quence
		if(this.character.roleplayContext.lastExchangeOutcome) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			this.character.log.p("Exchange with mule terminated successfully.");
		}
		else
			throw new FatalError("Exchange with mule has failed.");
		
		// on repasse en mode absent apr�s l'�change
		this.character.changePlayerStatus(PlayerStatusEnum.PLAYER_STATUS_AFK);
	}
	
	// traite une demande d'�change (acceptation ou refus)
	public boolean processExchangeDemand(double actorIdDemandingExchange) {
		// si ce n'est pas un client, on refuse l'�change avec un sleep
		if(!((Mule) this.character).isCustomer(actorIdDemandingExchange)) {
			try {
				Thread.sleep(2000); // pour faire un peu normal
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			this.character.log.p("Refusing exchange request received from an unknown.");
			this.character.net.send(new UnhandledMessage("LeaveDialogRequestMessage"));
			return false;
		}
		
		// si le caract�re actuel est une mule et qu'il est occup�, on refuse
		// ou si le caract�re (peu importe le type) a besoin de vider son inventaire, on refuse aussi
		if((this.character instanceof Mule && !this.character.inState(CharacterState.MULE_AVAILABLE)) ||
				this.character.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.character.log.p("Refusing exchange request because I am busy.");
			this.character.net.send(new UnhandledMessage("LeaveDialogRequestMessage"));
			return false;
		}
			
		// sinon on accepte l'�change
		this.character.log.p("Accepting exchange request received from a customer.");
		this.character.net.send(new UnhandledMessage("ExchangeAcceptMessage"));
		this.character.waitState(CharacterState.IN_EXCHANGE);
		return true;
	}

	// envoie une demande d'�change et attend qu'elle soit accept�e
	private boolean sendExchangeRequest(double characterId) {
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