package messages.fights;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class GameFightPlacementPossiblePositionsMessage extends Message {
	public Vector<Integer> positionsForChallengers;
    public Vector<Integer> positionsForDefenders;
    public int teamNumber = 2;
    
    public GameFightPlacementPossiblePositionsMessage(Message msg) {
    	super(msg);
    	this.positionsForChallengers = new Vector<Integer>();
    	this.positionsForDefenders = new Vector<Integer>();
    	deserialize();
    }
    
    private void deserialize() {
    	ByteArray buffer = new ByteArray(this.content);
    	int nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.positionsForChallengers.add(buffer.readVarShort());
    	nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.positionsForDefenders.add(buffer.readVarShort());
    	this.teamNumber = buffer.readByte();
    }
}