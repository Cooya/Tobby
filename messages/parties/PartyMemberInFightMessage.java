package messages.parties;

import utilities.ByteArray;
import gamedata.maps.MapCoordinatesExtended;
import messages.Message;

public class PartyMemberInFightMessage extends AbstractPartyMessage {
    public int reason = 0;
    public double memberId = 0;
    public int memberAccountId = 0;
    public String memberName = "";
    public int fightId = 0;
    public MapCoordinatesExtended fightMap;
    public int timeBeforeFightStart = 0;
	
	public PartyMemberInFightMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
		this.reason = buffer.readByte();
		this.memberId = buffer.readVarLong();
		this.memberAccountId = buffer.readInt();
		this.memberName = buffer.readUTF();
		this.fightId = buffer.readInt();
        this.fightMap = new MapCoordinatesExtended(buffer);
        this.timeBeforeFightStart = buffer.readVarShort();
	}
}