package messages.connection;

import utilities.BooleanByteWrapper;
import messages.NetworkMessage;

public class IdentificationSuccessMessage extends NetworkMessage {
	public String login = "";
	public String nickname = "";
	public int accountId = 0;
	public int communityId = 0;
	public boolean hasRights = false;
	public String secretQuestion = "";
	public double accountCreation = 0;
	public double subscriptionElapsedDuration = 0;
	public double subscriptionEndDate = 0;
	public boolean wasAlreadyConnected = false;
	public int havenbagAvailableRoom = 0;

	@Override
	public void serialize() {
		int b = 0;
		b = BooleanByteWrapper.setFlag(b, 0, this.hasRights);
		b = BooleanByteWrapper.setFlag(b, 1, this.wasAlreadyConnected);
		this.content.writeByte(b);
		this.content.writeUTF(this.login);
		this.content.writeUTF(this.nickname);
		this.content.writeInt(this.accountId);
		this.content.writeByte(this.communityId);
		this.content.writeUTF(this.secretQuestion);
		this.content.writeDouble(this.accountCreation);
		this.content.writeDouble(this.subscriptionElapsedDuration);
		this.content.writeDouble(this.subscriptionEndDate);
		this.content.writeByte(this.havenbagAvailableRoom);
	}

	@Override
	public void deserialize() {
		int b = this.content.readByte();
		this.hasRights = BooleanByteWrapper.getFlag(b, 0);
		this.wasAlreadyConnected = BooleanByteWrapper.getFlag(b, 1);
		this.login = this.content.readUTF();
		this.nickname = this.content.readUTF();
		this.accountId = this.content.readInt();
		this.communityId = this.content.readByte();
		this.secretQuestion = this.content.readUTF();
		this.accountCreation = this.content.readDouble();
		this.subscriptionElapsedDuration = this.content.readDouble();
		this.subscriptionEndDate = this.content.readDouble();
		this.havenbagAvailableRoom = this.content.readByte();
	}
}