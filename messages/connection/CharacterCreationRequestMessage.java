package messages.connection;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class CharacterCreationRequestMessage extends Message {
	public String name = "";
	public int breed = 0;
	public boolean sex = false;
	public Vector<Integer> colors;
	public int cosmeticId = 0;
	
	public CharacterCreationRequestMessage() {
		super();
	}
	
	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(this.name);
		buffer.writeByte(this.breed);
		buffer.writeBoolean(this.sex);
		for(int i : this.colors)
			buffer.writeInt(i);
		buffer.writeVarShort(this.cosmeticId);
		super.completeInfos(buffer);
	}
}