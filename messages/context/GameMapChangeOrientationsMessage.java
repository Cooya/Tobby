package messages.context;

import gamedata.context.ActorOrientation;

import java.util.Vector;

import messages.Message;

public class GameMapChangeOrientationsMessage extends Message {
	public Vector<ActorOrientation> orientations;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.orientations = new Vector<ActorOrientation>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.orientations.add(new ActorOrientation(this.content));
	}
}