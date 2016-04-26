package messages.fights;

import messages.Message;

public class GameFightOptionStateUpdateMessage extends Message {
    public int fightId = 0;
    public int teamId = 2;
    public int option = 3;
    public boolean state = false;
	
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
	public void deserialize() {
		this.fightId = this.content.readShort();
		this.teamId = this.content.readByte();
		this.option = this.content.readByte();
		this.state = this.content.readBoolean();
	}
}