package messages.fights;

import messages.NetworkMessage;

public class GameFightPlacementPossiblePositionsMessage extends NetworkMessage {
	public int[] positionsForChallengers;
	public int[] positionsForDefenders;
	public int teamNumber = 2;

	@Override
	public void serialize() {

	}

	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.positionsForChallengers = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.positionsForChallengers[i] = this.content.readVarShort();
		nb = this.content.readShort();
		this.positionsForDefenders = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.positionsForDefenders[i] = this.content.readVarShort();
		this.teamNumber = this.content.readByte();
	}
}