package controller.modules;

import controller.CharacterState;
import controller.characters.Character;
import messages.UnhandledMessage;
import messages.interactions.InteractiveUseRequestMessage;
import messages.interactions.NpcDialogReplyMessage;
import messages.interactions.NpcGenericActionRequestMessage;

public class InteractionAPI {
	private Character character;

	public InteractionAPI(Character character) {
		this.character = character;
	}

	// interagit avec un élément interactif
	// withMapChangement = true -> attente du changement de map
	// withMapChangement = false -> pas d'attente
	public void useInteractive(int besideCellId, int elemId, boolean withMapChangement) {
		this.character.mvt.moveTo(besideCellId);

		InteractiveUseRequestMessage IURM = new InteractiveUseRequestMessage();
		IURM.elemId = elemId;
		IURM.skillInstanceUid = this.character.roleplayContext.getInteractiveSkillInstanceUid(elemId);
		this.character.net.send(IURM);
		this.character.log.p("Interactive used.");
		if(withMapChangement) {
			this.character.updateState(CharacterState.IS_LOADED, false);
			this.character.waitState(CharacterState.IS_LOADED);
		}
	}
	
	// parle à un pnj
	// withDialog = true -> attente de la question
	// withDialog = false -> attente de l'ouverture de l'échange
	public void talkToNpc(int npcId, int npcActionId, boolean withDialog) {
		NpcGenericActionRequestMessage msg = new NpcGenericActionRequestMessage();
		msg.npcId = (int) this.character.roleplayContext.getNpcContextualId(npcId);
		msg.npcActionId = npcActionId;
		msg.npcMapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(msg);
		
		if(withDialog) // dialogue avec question
			this.character.waitState(CharacterState.DIALOG_DISPLAYED);
		else // dialogue avec échange
			this.character.waitState(CharacterState.IN_EXCHANGE);
	}
	
	// sélectionne une réponse lors d'une dialogue avec un pnj (pas d'attente)
	public void answerToNpc(int replyId) {
		NpcDialogReplyMessage msg = new NpcDialogReplyMessage();
		msg.replyId = replyId;
		this.character.net.send(msg);
	}
	
	// ouvre le coffre de la banque d'Astrub
	public void openBankStorage() {
		// on parle au banquier
		talkToNpc(522, 3, true);

		// on sélectionne la réponse
		answerToNpc(259);
		
		// on attend l'affichage de l'inventaire
		this.character.waitState(CharacterState.IN_EXCHANGE);
		// on attend aussi d'avoir récupérer le contenu de l'inventaire
		this.character.waitState(CharacterState.EXCHANGE_ACTION_RESPONSE);
	}
	
	// ouvre le magasin de la taverne
	public void openTavernShop() {
		// on parle au tavernier
		talkToNpc(464, 1, false);
	}
	
	// ouvre la vente de ressources à l'hdv ressources
	public void openBidHouse() {
		// on parle au vendeur de ressources
		this.character.interaction.talkToNpc(546, 5, false);
	}
	
	// ferme un stockage d'objet (banque, pnj ou hdv)
	public void closeStorage() {
		// on ferme l'inventaire
		this.character.net.send(new UnhandledMessage("LeaveDialogRequestMessage"));
				
		// on attend que l'inventaire se ferme
		this.character.waitState(CharacterState.NOT_IN_EXCHANGE);
	}
}