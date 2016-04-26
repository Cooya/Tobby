package controller.api;

import controller.CharacterState;
import controller.characters.Character;
import controller.characters.Fighter;
import controller.characters.Mule;
import main.FatalError;
import messages.EmptyMessage;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;
import messages.exchanges.ExchangePlayerRequestMessage;
import messages.exchanges.ExchangeReadyMessage;

public class SocialAPI {
	private Character character;
	
	public SocialAPI(Character character) {
		this.character = character;
	}

	// change le statut du personnage
	public void changePlayerStatus(int status) {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(status);
		this.character.instance.outPush(PSURM);
		this.character.instance.log.p("Passing in away mode.");
	}
	
	// se rend à la map d'attente de la mule et lui transmet tous les objets de l'inventaire et les kamas
	public void goToExchangeWithMule() {
		// attente de la connexion de la mule si elle n'est pas connectée
		this.character.waitState(CharacterState.MULE_ONLINE);
		Mule mule = ((Fighter) character).getMule();
		
		// aller sur la map d'attente de la mule
		this.character.mvt.goTo(mule.getWaitingMapId());
		
		// tentative d'échange avec elle
		while(!Thread.currentThread().isInterrupted()) {
			if(!this.character.roleplayContext.actorIsOnMap(mule.infos.characterId)) // si la mule n'est pas sur la map
				this.character.waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
			else {
				this.character.waitState(CharacterState.MULE_AVAILABLE);
				if(exchangeDemand(mule.infos.characterId)) // si l'échange a été accepté
					break;
				try {
					Thread.sleep(5000); // pour pas flooder de demandes d'échange
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
		
		// transfert de tous les objets de l'inventaire
		this.character.instance.log.p("Transfering all objects from inventory.");
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.character.instance.outPush(EM);
		
		// les kamas aussi
		ExchangeObjectMoveKamaMessage EOMKM = new ExchangeObjectMoveKamaMessage();
		EOMKM.serialize(this.character.infos.stats.kamas);
		this.character.instance.outPush(EOMKM);
		
		// on attend de pouvoir valider l'échange (bouton bloqué pendant 3 secondes après chaque action)
		try {
			Thread.sleep(3000);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		// validation de l'échange côté combattant
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'échange
		this.character.instance.outPush(ERM);
		this.character.instance.log.p("Exchange validated on my side.");
		
		// attente du résulat de l'échange
		this.character.waitState(CharacterState.IS_FREE);
		
		// et agissement en conséquence
		if(this.character.roleplayContext.lastExchangeOutcome) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			this.character.instance.log.p("Exchange with mule terminated successfully.");
		}
		else
			throw new FatalError("Exchange with mule has failed.");	
	}
	
	// traite une demande d'échange (acceptation ou refus)
	public boolean processExchangeDemand(double actorIdDemandingExchange) {
		// si ce n'est pas un client, on refuse l'échange avec un sleep
		if(!((Mule) this.character).isCustomer(actorIdDemandingExchange)) {
			try {
				Thread.sleep(2000); // pour faire un peu normal
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			this.character.instance.log.p("Refusing exchange request received from an unknown.");
			EmptyMessage EM = new EmptyMessage("LeaveDialogRequestMessage");
			this.character.instance.outPush(EM);
			return false;
		}
		
		// si le caractère actuel est une mule et qu'il est occupé, on refuse
		// ou si le caractère (peu importe le type) a besoin de vider son inventaire, on refuse aussi
		if((this.character instanceof Mule && !this.character.inState(CharacterState.MULE_AVAILABLE)) ||
				this.character.inState(CharacterState.NEED_TO_EMPTY_INVENTORY)) {
			this.character.instance.log.p("Refusing exchange request because I am busy.");
			EmptyMessage EM = new EmptyMessage("LeaveDialogRequestMessage");
			this.character.instance.outPush(EM);
			return false;
		}
			
		// sinon on accepte l'échange
		this.character.instance.log.p("Accepting exchange request received from a customer.");
		EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'échange
		this.character.instance.outPush(EM);
		this.character.waitState(CharacterState.IN_EXCHANGE); // important
		return true;
	}
	
	// envoie une demande d'échange et attend son acceptation
	private boolean exchangeDemand(double characterId) {
		this.character.waitState(CharacterState.IS_FREE);
		
		// envoi de la demande d'échange
		ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage();
		EPRM.serialize(characterId, 1, this.character.instance.id);
		this.character.instance.outPush(EPRM);
		this.character.instance.log.p("Sending exchange demand.");
		
		// attente du résultat de la requête
		if(!this.character.waitState(CharacterState.EXCHANGE_DEMAND_OUTCOME))
			return false; // timeout atteint avant la réception du résultat de la demande
		if(!this.character.roleplayContext.lastExchangeDemandOutcome)
			return false; // la requête de demande d'échange a échouée
		
		// attente de l'acceptation de la demande d'échange (avec timeout de 5 secondes)
		return this.character.waitState(CharacterState.IN_EXCHANGE);
	}
}