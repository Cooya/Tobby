package messages.character;

import gamedata.ProtocolTypeManager;
import gamedata.context.AbstractSocialGroupInfos;

import java.util.Vector;

import utilities.BooleanByteWrapper;
import utilities.ByteArray;
import messages.Message;

public class BasicWhoIsMessage extends Message {
    public boolean self = false;
    public int position = -1;
    public String accountNickname = "";
    public int accountId = 0;
    public String playerName = "";
    public double playerId = 0;
    public int areaId = 0;
    public Vector<AbstractSocialGroupInfos> socialGroups;
    public boolean verbose = false;
    public int playerState = 99;

	public BasicWhoIsMessage(Message msg) {
		super(msg);
		this.socialGroups = new Vector<AbstractSocialGroupInfos>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int b = buffer.readByte();
		this.self = BooleanByteWrapper.getFlag(b, 0);
		this.verbose = BooleanByteWrapper.getFlag(b, 1);
		this.position = buffer.readByte();
		this.accountNickname = buffer.readUTF();
		this.accountId = buffer.readInt();
		this.playerName = buffer.readUTF();
		this.playerId = buffer.readVarLong();
		this.areaId = buffer.readShort();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.socialGroups.add((AbstractSocialGroupInfos) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
		this.playerState = buffer.readByte();
	}
}