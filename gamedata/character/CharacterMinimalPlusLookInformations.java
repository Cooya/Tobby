package gamedata.character;

import gamedata.currentmap.EntityLook;
import utilities.ByteArray;

public class CharacterMinimalPlusLookInformations extends CharacterMinimalInformations {
    public EntityLook entityLook;
    
    public CharacterMinimalPlusLookInformations(ByteArray buffer)
    {
    	 super(buffer);
         this.entityLook = new EntityLook(buffer);
    	
    }	
	
}
