package controller.informations;

import gamedata.parties.PartyMemberInformations;

import java.util.Vector;

import messages.parties.PartyLeaveRequestMessage;
import controller.CharacterState;
import controller.characters.Captain;
import controller.characters.Character;

public class PartyManager {
	private Character character;
	private int partyId;
	private Vector<PartyMemberInformations> partyMembers;
	private int partyLevel;
	private int fightId;
	
	public PartyManager(Character character) {
		this.character = character;
	}
	
	public int getFightId() {
		return this.fightId;
	}
	
	public void setFightId(int fightId) {
		this.fightId = fightId;
	}
	
	public void incPartyLevel() {
		if(partyMembers != null) {
			this.partyLevel++;
			if(this.character instanceof Captain)
				((Captain) this.character).updateFightArea(this.partyLevel);
		}
	}
	
	public void partyJoined(int partyId, Vector<PartyMemberInformations> members) {
		this.partyId = partyId;
		this.partyMembers = members;
		for(PartyMemberInformations partyMember : this.partyMembers)
			this.partyLevel += partyMember.level;
		this.character.log.p("Party joined.");
		
		if(this.character instanceof Captain)
			((Captain) this.character).updateFightArea(this.partyLevel);
	}
	
	public void partyLeft() {
		this.partyId = 0;
		this.partyMembers = null;
		this.partyLevel = 0;
		this.character.log.p("Party left.");
		
		if(this.character instanceof Captain)
			((Captain) this.character).updateFightArea(this.character.infos.getLevel());
	}
	
	public void addPartyMember(PartyMemberInformations newMember) {
		this.partyMembers.add(newMember);
		this.partyLevel += newMember.level;
		this.character.log.p("Member " + newMember.name + " joined the party.");
		
		if(this.character instanceof Captain)
			((Captain) this.character).updateFightArea(this.partyLevel);
	}
	
	public void removePartyMember(double leavingMemberId) {
		for(PartyMemberInformations partyMember : this.partyMembers)
			if(partyMember.id == leavingMemberId) {
				this.partyLevel -= partyMember.level;
				this.character.log.p("Member " + partyMember.name + " left the party.");
				this.partyMembers.remove(partyMember);
				if(this.character instanceof Captain)
					((Captain) this.character).updateFightArea(this.partyLevel);
				break;
			}
	}
	
	public void updatePartyMember(PartyMemberInformations member) {
		int size = this.partyMembers.size();
		PartyMemberInformations partyMember;
		for(int i = 0; i < size; ++i) {
			partyMember = this.partyMembers.get(i);
			if(partyMember.id == member.id) {
				this.partyLevel += (member.level - partyMember.level);
				this.partyMembers.setElementAt(member, i);
				if(this.character instanceof Captain)
					((Captain) this.character).updateFightArea(this.partyLevel);
				break;
			}
		}
	}
	
	public void leaveParty() {
		PartyLeaveRequestMessage PLRM = new PartyLeaveRequestMessage();
		PLRM.partyId = this.partyId;
		this.character.net.send(PLRM);
		this.character.log.p("Leaving party request sent.");
		this.character.waitState(CharacterState.NOT_IN_PARTY);
	}
}