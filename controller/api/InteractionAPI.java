package controller.api;

import controller.CharacterState;
import controller.characters.Character;
import messages.EmptyMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class InteractionAPI {
	private Character character;

	public InteractionAPI(Character character) {
		this.character = character;
	}

	public void useInteractive(int besideCellId, int elemId, int skillInstanceUid, boolean withMapChangement) {
		if(!this.character.mvt.moveTo(besideCellId, false))
			return;

		this.character.waitState(CharacterState.IS_FREE);

		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.serialize(elemId, skillInstanceUid, this.character.instance.id);
		this.character.instance.outPush(IURM);
		this.character.instance.log.p("Interactive used.");
		if(withMapChangement)
			this.character.updateState(CharacterState.IS_LOADED, false);
	}

	public void emptyInventoryInBank() {
		this.character.waitState(CharacterState.IS_FREE);

		// on parle au banquier
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.character.infos.currentMap.id, this.character.instance.id);
		this.character.instance.outPush(NGARM);

		// on attend la question
		this.character.waitState(CharacterState.DIALOG_DISPLAYED);

		// on sélectionne la réponse
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(259);
		this.character.instance.outPush(NDRM);

		// on attend l'affichage de l'inventaire
		this.character.waitState(CharacterState.IN_EXCHANGE);

		// on transfère tous les objets de l'inventaire
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.character.instance.outPush(EM);

		// on attend la confirmation du transfert
		this.character.waitState(CharacterState.BANK_TRANSFER);

		// on ferme l'inventaire
		EM = new EmptyMessage("LeaveDialogRequestMessage");
		this.character.instance.outPush(EM);
	}
}