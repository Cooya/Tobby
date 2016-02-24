package gamedata.character;

import utilities.ByteArray;

public class CharacterMinimalInformations extends AbstractCharacterInformation {
	public int level = 0;
	public String name = "";

	public CharacterMinimalInformations(ByteArray buffer) {
		super(buffer);
		this.level = buffer.readByte();
		this.name = buffer.readUTF();
	}
}