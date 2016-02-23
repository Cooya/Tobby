package gamedata.character;

import utilities.ByteArray;

public class CharacterSpellModification {
    public int modificationType = 0;
    public int spellId = 0;
    public CharacterBaseCharacteristic value;
    
    public CharacterSpellModification(ByteArray buffer) {
    	this.modificationType = buffer.readByte();
    	this.spellId = buffer.readVarShort();
    	this.value = new CharacterBaseCharacteristic(buffer);
    }
}