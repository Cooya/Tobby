package messages.connection;

import game.character.CharacterBaseInformations;
import messages.Message;
import utilities.ByteArray;

public class CharacterSelectedSuccessMessage extends Message {
    public CharacterBaseInformations infos;
    public boolean isCollectingStats = false;
   
    public CharacterSelectedSuccessMessage(Message msg) {
    	super(msg);
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	this.infos = new CharacterBaseInformations(buffer);
    	this.isCollectingStats = buffer.readBoolean();
    }
}