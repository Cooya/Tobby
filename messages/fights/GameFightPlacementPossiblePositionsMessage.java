package messages.fights;

import java.util.Vector;

import messages.Message;

public class GameFightPlacementPossiblePositionsMessage extends Message {
	public Vector<Integer> positionsForChallengers;
	public Vector<Integer> positionsForDefenders;
	public int teamNumber = 2;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.positionsForChallengers = new Vector<Integer>();
    	this.positionsForDefenders = new Vector<Integer>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.positionsForChallengers.add(this.content.readVarShort());
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.positionsForDefenders.add(this.content.readVarShort());
		this.teamNumber = this.content.readByte();
	}
}