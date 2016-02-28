package gamedata.character;

import utilities.ByteArray;

public class SpellItem{
    
    public int position = 0;
    
    public int spellId = 0;
    
    public int spellLevel = 0;
    
    public SpellItem(ByteArray buffer)
    {
       this.position = buffer.readByte();
       this.spellId = buffer.readInt();
       this.spellLevel = buffer.readByte();
    }

}
