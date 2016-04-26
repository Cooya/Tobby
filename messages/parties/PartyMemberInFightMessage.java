package messages.parties;

import gamedata.maps.MapCoordinatesExtended;

public class PartyMemberInFightMessage extends AbstractPartyMessage {
    public int reason = 0;
    public double memberId = 0;
    public int memberAccountId = 0;
    public String memberName = "";
    public int fightId = 0;
    public MapCoordinatesExtended fightMap;
    public int timeBeforeFightStart = 0;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
	
    @Override
	public void deserialize() {
		super.deserialize();
		this.reason = this.content.readByte();
		this.memberId = this.content.readVarLong();
		this.memberAccountId = this.content.readInt();
		this.memberName = this.content.readUTF();
		this.fightId = this.content.readInt();
        this.fightMap = new MapCoordinatesExtended(this.content);
        this.timeBeforeFightStart = this.content.readVarShort();
	}
}