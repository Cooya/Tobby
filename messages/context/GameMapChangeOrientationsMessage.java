package messages.context;

import gamedata.currentmap.ActorOrientation;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class GameMapChangeOrientationsMessage extends Message {
	public Vector<ActorOrientation> orientations;

	public GameMapChangeOrientationsMessage(Message msg) {
		super(msg);
		this.orientations = new Vector<ActorOrientation>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.orientations.add(new ActorOrientation(buffer));
	}
}