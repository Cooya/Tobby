package controller.api;

import gui.Controller;
import controller.CharacterState;
import controller.characters.Character;
import controller.characters.Mule;
import main.FatalError;
import messages.EmptyMessage;
import messages.character.PlayerStatusUpdateRequestMessage;
import messages.exchanges.ExchangeObjectMoveKamaMessage;
import messages.exchanges.ExchangePlayerRequestMessage;
import messages.exchanges.ExchangeReadyMessage;
import messages.parties.PartyLeaveRequestMessage;

public class SocialAPI {
	private Character character;
	
	public SocialAPI(Character character) {
		this.character = character;
	}

	public void changePlayerStatus(int status) {
		PlayerStatusUpdateRequestMessage PSURM = new PlayerStatusUpdateRequestMessage();
		PSURM.serialize(status);
		this.character.instance.outPush(PSURM);
		this.character.instance.log.p("Passing in away mode.");
	}
	
	public void leaveGroup() {
		PartyLeaveRequestMessage PLRM = new PartyLeaveRequestMessage();
		PLRM.partyId = this.character.infos.partyId;
		PLRM.serialize();
		this.character.instance.outPush(PLRM);
		this.character.instance.log.p("Leaving group request sent.");
		this.character.waitState(CharacterState.NOT_IN_PARTY);
	}
	
	// fonction à améliorer (obtenir le résultat de la demande)
	public boolean exchangeDemand(double characterId) { // timeout de 5 secondes si demande pas acceptée
		this.character.waitState(CharacterState.IS_FREE);
		ExchangePlayerRequestMessage EPRM = new ExchangePlayerRequestMessage();
		EPRM.serialize(characterId, 1, this.character.instance.id);
		this.character.instance.outPush(EPRM);
		this.character.instance.log.p("Sending exchange demand.");
		return this.character.waitState(CharacterState.IN_EXCHANGE); // retourne le résultat de la demande d'échange (avec timeout)
	}
	
	// fonction à améliorer
	public void goToExchange(Character target) {
		this.character.waitState(CharacterState.IS_LOADED);
		
		int targetMapId;
		if(target instanceof Mule) {
			this.character.waitState(CharacterState.MULE_AVAILABLE);
			targetMapId = ((Mule) target).getWaitingMapId();
		}
		else
			targetMapId = character.infos.currentMap.id;
		
		if(!Thread.currentThread().isInterrupted() && this.character.infos.currentMap.id != targetMapId)
			this.character.mvt.goTo(targetMapId);
		
		this.character.waitState(CharacterState.IS_LOADED);
		
		while(!Thread.currentThread().isInterrupted() && !this.character.inState(CharacterState.IN_EXCHANGE)) {
			if(!this.character.roleplayContext.actorIsOnMap(target.infos.characterId)) // si la mule n'est pas sur la map
				this.character.waitState(CharacterState.NEW_ACTOR_ON_MAP); // on attend qu'elle arrive
			exchangeDemand(target.infos.characterId);
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage"); // on transfère tous les objets
		this.character.instance.outPush(EM);
		this.character.instance.log.p("Transfering all objects.");
		ExchangeObjectMoveKamaMessage EOMKM = new ExchangeObjectMoveKamaMessage(); // et les kamas
		EOMKM.serialize(this.character.infos.stats.kamas);
		this.character.instance.outPush(EOMKM);
		
		try {
			Thread.sleep(5000); // on attend de pouvoir valider l'échange
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'échange
		this.character.instance.outPush(ERM); // on valide de notre côté
		this.character.instance.log.p("Exchange validated from my side.");
		
		this.character.waitState(CharacterState.IS_FREE); // pour obtenir le résultat de l'échange
		if(this.character.roleplayContext.lastExchangeOutcome) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, false);
			this.character.instance.log.p("Exchange with mule terminated successfully.");
		}
		else
			throw new FatalError("Exchange with mule has failed.");	
	}
	
	public boolean processExchangeDemand(double actorIdDemandingExchange) {
		if(!this.character.inState(CharacterState.NEED_TO_EMPTY_INVENTORY) && Controller.isWorkmate(actorIdDemandingExchange)) {
			EmptyMessage EM = new EmptyMessage("ExchangeAcceptMessage"); // accepter l'échange
			this.character.instance.outPush(EM);
			return true;
		}
		else { // on refuse l'échange
			try {
				Thread.sleep(2000); // pour faire un peu normal
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			EmptyMessage EM = new EmptyMessage("LeaveDialogRequestMessage");
			this.character.instance.outPush(EM);
			return false;
		}	
	}
	
	public void acceptExchangeAsReceiver() {
		this.character.waitState(CharacterState.EXCHANGE_VALIDATED); // attendre que l'échange soit validé
		ExchangeReadyMessage ERM = new ExchangeReadyMessage();
		ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'échange
		this.character.instance.outPush(ERM); // on valide de notre côté
		this.character.instance.log.p("Exchange validated from my side.");
	}
}