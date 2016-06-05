package messages.connection;

import gamedata.connection.VersionExtended;

import messages.NetworkMessage;
import utilities.BooleanByteWrapper;

public class IdentificationMessage extends NetworkMessage {
	public VersionExtended version;
	public String lang = "";
	public byte[] credentials; // normalement c'est un vecteur d'int
	public int serverId = 0;
	public boolean autoconnect = false;
	public boolean useCertificate = false;
	public boolean useLoginToken = false;
	public double sessionOptionalSalt = 0;
	public int[] failedAttempts;
	
	@Override
	public void serialize() {
		int b = 0;
		b = BooleanByteWrapper.setFlag(b, 0, this.autoconnect);
		b = BooleanByteWrapper.setFlag(b, 1, this.useCertificate);
		b = BooleanByteWrapper.setFlag(b, 2, this.useLoginToken);
		this.content.writeByte(b);
		this.version.serialize(this.content);
		this.content.writeUTF(this.lang);
		this.content.writeVarInt(this.credentials.length);
		this.content.writeBytes(this.credentials);
		this.content.writeShort(this.serverId);
		this.content.writeVarLong(this.sessionOptionalSalt);
		if(this.failedAttempts == null)
			this.content.writeShort(0);
		else {
			this.content.writeShort(this.failedAttempts.length);
			for(int i : this.failedAttempts)
				this.content.writeVarShort(i);
		}
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}