package gamedata.character;

import utilities.ByteArray;

public class CharacterBaseInformations extends CharacterMinimalPlusLookInformations {
	public int breed = 0;
	public boolean sex = false;

	public CharacterBaseInformations(ByteArray buffer) {
		super(buffer);
		this.breed = buffer.readByte();
		this.sex = buffer.readBoolean();
	}
}