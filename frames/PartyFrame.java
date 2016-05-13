package frames;

import main.Controller;
import messages.parties.PartyAcceptInvitationMessage;
import messages.parties.PartyDeletedMessage;
import messages.parties.PartyInvitationMessage;
import messages.parties.PartyJoinMessage;
import messages.parties.PartyLeaveMessage;
import messages.parties.PartyMemberInFightMessage;
import messages.parties.PartyMemberRemoveMessage;
import messages.parties.PartyNewMemberMessage;
import messages.parties.PartyUpdateMessage;
import controller.CharacterState;
import controller.characters.Character;

public class PartyFrame extends Frame {
	
	public PartyFrame(Character character) {
		super(character);
	}
	
	protected void process(PartyInvitationMessage PIM) {
		this.character.log.p("Party invitation received.");
		if(Controller.getInstance().isWorkmate(PIM.fromId)) {
			PartyAcceptInvitationMessage PAIM = new PartyAcceptInvitationMessage();
			PAIM.partyId = PIM.partyId;
			this.character.net.send(PAIM);
			this.character.log.p("Party invitation acceptation sent.");
		}
	}
	
	protected void process(PartyJoinMessage PJM) {
		this.character.partyManager.partyJoined(PJM.partyId, PJM.members);
		this.character.updateState(CharacterState.IN_PARTY, true);
	}
	
	protected void process(PartyNewMemberMessage PNMM) {
		this.character.partyManager.addPartyMember(PNMM.memberInformations);
		this.character.updateState(CharacterState.NEW_PARTY_MEMBER, true);
	}
	
	protected void process(PartyMemberRemoveMessage PMRM) {
		this.character.partyManager.removePartyMember(PMRM.leavingPlayerId);
	}
	
	protected void process(PartyMemberInFightMessage PMIFM) {
		this.character.partyManager.setFightId(PMIFM.fightId);
		this.character.updateState(CharacterState.FIGHT_LAUNCHED, true);
	}
	
	protected void process(PartyUpdateMessage PUM) {
		this.character.partyManager.updatePartyMember(PUM.memberInformations);
	}
	
	protected void process(PartyLeaveMessage PLM) {
		this.character.partyManager.partyLeft();
		this.character.updateState(CharacterState.IN_PARTY, false);
	}
	
	protected void process(PartyDeletedMessage PDM) {
		this.character.partyManager.partyLeft();
		this.character.updateState(CharacterState.IN_PARTY, false);
	}
}