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
	
	// fonction à améliorer
	public void emptyInventoryInBank() {
		this.character.waitState(CharacterState.IS_FREE);
		
		NpcGenericActionRequestMessage NGARM = new NpcGenericActionRequestMessage();
		NGARM.serialize(-10001, 3, this.character.infos.currentMap.id, this.character.instance.id); // on parle au banquier
		this.character.instance.outPush(NGARM);
		
		try {
			Thread.sleep(1000); // on attend la question
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		NpcDialogReplyMessage NDRM = new NpcDialogReplyMessage();
		NDRM.serialize(259); // on sélectionne la réponse
		this.character.instance.outPush(NDRM);
		
		try {
			Thread.sleep(2000); // on attend l'affichage de l'inventaire
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		EmptyMessage EM = new EmptyMessage("ExchangeObjectTransfertAllFromInvMessage");
		this.character.instance.outPush(EM); // on transfère tous les objets de l'inventaire
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		EM = new EmptyMessage("LeaveDialogRequestMessage");
		this.character.instance.outPush(EM); // on ferme l'inventaire
		
		try {
			Thread.sleep(1000); // on attend que l'inventaire se ferme
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}
}