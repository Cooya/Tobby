package messages;

import main.ByteArray;
import main.Encryption;

public class IdentificationMessage extends Message {
	public static final short id = 4;
	public static final String login = "maxlebgdu93";
	public static final String password = "represente";
	
	public IdentificationMessage(byte[] content) {
		super(id, (short) 0, 0, null);
		ByteArray array = new ByteArray(content);
		char[] salt = array.readUTF();
		int size = array.readVarInt();
		byte[] key = new byte[size];
		int counter = 0;
		while(counter < size)
			key[counter++] = array.readByte();
		byte[] credentials = Encryption.encrypt(key, login.toCharArray(), password.toCharArray(), salt);	
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
