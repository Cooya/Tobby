package gamedata.character;

import utilities.ByteArray;

public class CharacterBasicMinimalInformations extends AbstractCharacterInformation {
	public String name = "";

	public CharacterBasicMinimalInformations(ByteArray buffer) {
		super(buffer);
		this.name = buffer.readUTF();
	}
}