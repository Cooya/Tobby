package messages;

import main.ByteArray;
import main.Encryption;

public class IdentificationMessage extends Message {
	private static final short id = 4;
	private static final String login = "maxlebgdu93";
	private static final String password = "represente";
	
	// HCM
	private char[] salt;
	private int keySize;
	private byte[] key;
	
	public IdentificationMessage(byte[] content) {
		super(id, (short) 0, 0, null);
		deserializeHCM(content);
		serializeIM();
	}
	
	private void deserializeHCM(byte[] content) {
		ByteArray array = new ByteArray(content);
		this.salt = array.readUTF();
		this.keySize = array.readVarInt();
		this.key = array.readBytes(keySize);
	}
	
	private void serializeIM() {
		byte[] credentials = Encryption.encrypt(this.key, login.toCharArray(), password.toCharArray(), this.salt);	
		ByteArray buffer = new ByteArray();
		buffer.writeByte((byte) 1);
		writeVersion(buffer, 2, 32, 4, 100752, 1, 1);
		buffer.writeUTF("fr".toCharArray());
		buffer.writeVarInt(credentials.length);
		buffer.writeBytes(credentials);
		buffer.writeShort((short) 0);
		buffer.writeByte((byte) 0);
		buffer.writeByte((byte) 0);
		buffer.writeByte((byte) 0);
		buffer.writeByte((byte) 0);
		buffer.writeByte((byte) 0);
		
		this.size = buffer.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		this.content = buffer.bytes();
	}
		
	static void writeVersion(ByteArray array, int major, int minor, int release, int buildType, int install, int technology) {
		array.writeByte((byte) major);
		array.writeByte((byte) minor);
		array.writeByte((byte) release);
		array.writeByte((byte) buildType);
		array.writeByte((byte) install);
		array.writeByte((byte) technology);
	}
}
