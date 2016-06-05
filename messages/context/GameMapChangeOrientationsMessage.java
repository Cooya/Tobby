package messages.context;

import gamedata.context.ActorOrientation;

import messages.NetworkMessage;

public class GameMapChangeOrientationsMessage extends NetworkMessage {
	public ActorOrientation[] orientations;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.orientations = new ActorOrientation[nb];
		for(int i = 0; i < nb; ++i)
			this.orientations[i] = new ActorOrientation(this.content);
	}
}