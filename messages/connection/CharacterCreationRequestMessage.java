package messages.connection;

import java.util.Vector;

import messages.NetworkMessage;

public class CharacterCreationRequestMessage extends NetworkMessage {
	public String name = "";
	public int breed = 0;
	public boolean sex = false;
	public Vector<Integer> colors;
	public int cosmeticId = 0;
	
	@Override
	public void serialize() {
		this.content.writeUTF(this.name);
		this.content.writeByte(this.breed);
		this.content.writeBoolean(this.sex);
		for(int i : this.colors)
			this.content.writeInt(i);
		this.content.writeVarShort(this.cosmeticId);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}