package gamedata.character;

import utilities.ByteArray;

public class CharacterMinimalInformations extends CharacterBasicMinimalInformations {
	public int level = 0;

	public CharacterMinimalInformations(ByteArray buffer) {
		super(buffer);
		this.level = buffer.readByte();
	}
}