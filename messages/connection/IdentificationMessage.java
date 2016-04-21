package messages.connection;

import messages.Message;
import utilities.ByteArray;
import utilities.Encryption;

public class IdentificationMessage extends Message {
	
	public IdentificationMessage() {
		super();
	}
	
	public void serialize(HelloConnectMessage HCM, String login, String password) {
		byte[] decryptedPublicKey = Encryption.decryptReceivedKey(ByteArray.toBytes(HCM.getKey()));
		byte[] credentials = Encryption.encryptCredentials(decryptedPublicKey, login, password, HCM.getSalt());
		ByteArray buffer = new ByteArray();
		buffer.writeByte(0);
		writeVersion(buffer, 2, 34, 0, 103829, 1, 0, 1, 1);
		buffer.writeUTF("fr");
		buffer.writeVarInt(credentials.length);
		
		buffer.writeBytes(credentials);
		buffer.writeShort(0); // sélection du serveur automatique
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		buffer.writeByte(0);
		
		completeInfos(buffer);
	}
		
	static void writeVersion(ByteArray array, int major, int minor, int release, int revision, int patch, int buildType, int install, int technology) {
		array.writeByte(major);
		array.writeByte(minor);
		array.writeByte(release);
		array.writeInt(revision);
		array.writeByte(patch);
		array.writeByte(buildType);
		array.writeByte(install);
		array.writeByte(technology);
	}
}