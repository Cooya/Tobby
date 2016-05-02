package frames;

import gamedata.enums.DialogTypeEnum;
import controller.CharacterState;
import controller.characters.Character;
import messages.exchanges.ExchangeErrorMessage;
import messages.exchanges.ExchangeIsReadyMessage;
import messages.exchanges.ExchangeLeaveMessage;
import messages.exchanges.ExchangeReadyMessage;
import messages.exchanges.ExchangeRequestedTradeMessage;
import messages.exchanges.ExchangeStartedWithPodsMessage;
import messages.exchanges.ExchangeStartedWithStorageMessage;
import messages.exchanges.LeaveDialogMessage;
import messages.exchanges.StorageObjectsUpdateMessage;
import messages.interactions.NpcDialogCreationMessage;

public class DialogFrame extends Frame {

	public DialogFrame(Character character) {
		super(character);
	}
	
	protected void process(ExchangeErrorMessage EEM) {
		this.character.log.p("Exchange demand failed.");
		this.character.roleplayContext.lastExchangeDemandOutcome = false;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
	}
	
	protected void process(ExchangeRequestedTradeMessage ERTM) {
		this.character.log.p("Exchange demand dialog displayed");
		this.character.roleplayContext.lastExchangeDemandOutcome = true; // utile pour l'émetteur de la demande
		this.character.roleplayContext.actorDemandingExchange = ERTM.source;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
		this.character.updateState(CharacterState.PENDING_DEMAND, true);
	}
	
	protected void process(ExchangeStartedWithPodsMessage ESWPM) {
		this.character.log.p("Exchange with pods started.");
		this.character.updateState(CharacterState.PENDING_DEMAND, false);
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeStartedWithStorageMessage ESWSM) {
		this.character.log.p("Exchange with storage started.");
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeIsReadyMessage EIRM) {
		if(EIRM.id != this.character.infos.characterId) {
			this.character.log.p("Exchange validated by peer.");
			ExchangeReadyMessage ERM = new ExchangeReadyMessage();
			ERM.ready = true;
			ERM.step = 2; // car il y a eu 2 actions lors de l'échange
			this.character.net.send(ERM); // on valide de notre côté
			this.character.log.p("Exchange validated from my side.");
		}
	}
	
	protected void process(LeaveDialogMessage LDM) {
		if(LDM.dialogType == DialogTypeEnum.DIALOG_DIALOG) {
			this.character.log.p("Dialog window closed.");
			this.character.updateState(CharacterState.DIALOG_DISPLAYED, false);
		}
		else if(LDM.dialogType == DialogTypeEnum.DIALOG_EXCHANGE) {
			this.character.log.p("Exchange closed.");
			this.character.updateState(CharacterState.IN_EXCHANGE, false);
		}
		else
			this.character.log.p("Unknown dialog window closed.");
	}
	
	protected void process(ExchangeLeaveMessage ELM) {
		this.character.roleplayContext.lastExchangeOutcome = ELM.success;
		if(this.character.inState(CharacterState.IN_EXCHANGE)) // on a quitté un échange
			this.character.updateState(CharacterState.IN_EXCHANGE, false);
		else { // on a refusé un échange
			this.character.roleplayContext.actorDemandingExchange = 0;
			this.character.updateState(CharacterState.PENDING_DEMAND, false);
		}
	}
	
	protected void process(StorageObjectsUpdateMessage SOUM) {
		this.character.log.p("Bank transfer done.");
		this.character.updateState(CharacterState.BANK_TRANSFER, true);
	}
	
	protected void process(NpcDialogCreationMessage NDCM) {
		this.character.log.p("NPC dialog displayed.");
		this.character.updateState(CharacterState.DIALOG_DISPLAYED, true);
	}
}