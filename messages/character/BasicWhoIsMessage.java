package messages.character;

import gamedata.ProtocolTypeManager;
import gamedata.context.AbstractSocialGroupInfos;

import utilities.BooleanByteWrapper;
import messages.NetworkMessage;

public class BasicWhoIsMessage extends NetworkMessage {
	public boolean self = false;
	public int position = -1;
	public String accountNickname = "";
	public int accountId = 0;
	public String playerName = "";
	public double playerId = 0;
	public int areaId = 0;
	public int serverId = 0;
	public AbstractSocialGroupInfos[] socialGroups;
	public boolean verbose = false;
	public int playerState = 99;
	
	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		int b = this.content.readByte();
		this.self = BooleanByteWrapper.getFlag(b, 0);
		this.verbose = BooleanByteWrapper.getFlag(b, 1);
		this.position = this.content.readByte();
		this.accountNickname = this.content.readUTF();
		this.accountId = this.content.readInt();
		this.playerName = this.content.readUTF();
		this.playerId = this.content.readVarLong();
		this.areaId = this.content.readShort();
		this.serverId = this.content.readShort();
		int nb = this.content.readShort();
		this.socialGroups = new AbstractSocialGroupInfos[nb];
		for(int i = 0; i < nb; ++i)
			this.socialGroups[i] = (AbstractSocialGroupInfos) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
		this.playerState = this.content.readByte();
	}
}