package messages.fights;

import gamedata.context.IdentifiedEntityDispositionInformations;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class GameEntitiesDispositionMessage extends Message {
	public Vector<IdentifiedEntityDispositionInformations> dispositions;
	
	public GameEntitiesDispositionMessage(Message msg) {
		super(msg);
		this.dispositions = new Vector<IdentifiedEntityDispositionInformations>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.dispositions.add(new IdentifiedEntityDispositionInformations(buffer));
	}
}