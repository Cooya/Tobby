package frames;

import gamedata.enums.DialogTypeEnum;
import controller.CharacterState;
import controller.characters.Character;
import main.Instance;
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

	public DialogFrame(Instance instance, Character character) {
		super(instance, character);
	}
	
	protected void process(ExchangeErrorMessage EEM) {
		this.instance.log.p("Exchange demand failed.");
		this.character.roleplayContext.lastExchangeDemandOutcome = false;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
	}
	
	protected void process(ExchangeRequestedTradeMessage ERTM) {
		this.instance.log.p("Exchange demand dialog displayed");
		this.character.roleplayContext.lastExchangeDemandOutcome = true; // utile pour l'émetteur de la demande
		this.character.roleplayContext.actorDemandingExchange = ERTM.source;
		this.character.updateState(CharacterState.EXCHANGE_DEMAND_OUTCOME, true);
		this.character.updateState(CharacterState.PENDING_DEMAND, true);
	}
	
	protected void process(ExchangeStartedWithPodsMessage ESWPM) {
		this.instance.log.p("Exchange with pods started.");
		this.character.updateState(CharacterState.PENDING_DEMAND, false);
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeStartedWithStorageMessage ESWSM) {
		this.instance.log.p("Exchange with storage started.");
		this.character.updateState(CharacterState.IN_EXCHANGE, true);
	}
	
	protected void process(ExchangeIsReadyMessage EIRM) {
		if(EIRM.id != this.character.infos.characterId) {
			this.instance.log.p("Exchange validated by peer.");
			ExchangeReadyMessage ERM = new ExchangeReadyMessage();
			ERM.serialize(true, 2); // car il y a eu 2 actions lors de l'échange
			this.character.instance.outPush(ERM); // on valide de notre côté
			this.character.instance.log.p("Exchange validated from my side.");
		}
	}
	
	protected void process(LeaveDialogMessage LDM) {
		if(LDM.dialogType == DialogTypeEnum.DIALOG_DIALOG) {
			this.instance.log.p("Dialog window closed.");
			this.character.updateState(CharacterState.DIALOG_DISPLAYED, false);
		}
		else if(LDM.dialogType == DialogTypeEnum.DIALOG_EXCHANGE) {
			this.instance.log.p("Exchange closed.");
			this.character.updateState(CharacterState.IN_EXCHANGE, false);
		}
		else
			this.instance.log.p("Unknown dialog window closed.");
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
		this.instance.log.p("Bank transfer done.");
		this.character.updateState(CharacterState.BANK_TRANSFER, true);
	}
	
	protected void process(NpcDialogCreationMessage NDCM) {
		this.instance.log.p("NPC dialog displayed.");
		this.character.updateState(CharacterState.DIALOG_DISPLAYED, true);
	}
}