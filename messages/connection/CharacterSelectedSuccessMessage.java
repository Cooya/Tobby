package messages.connection;

import gamedata.character.CharacterBaseInformations;
import messages.Message;

public class CharacterSelectedSuccessMessage extends Message {
    public CharacterBaseInformations infos;
    public boolean isCollectingStats = false;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	this.infos = new CharacterBaseInformations(this.content);
    	this.isCollectingStats = this.content.readBoolean();
    }
}