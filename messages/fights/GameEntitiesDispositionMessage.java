package messages.fights;

import gamedata.context.IdentifiedEntityDispositionInformations;

import java.util.Vector;

import messages.Message;

public class GameEntitiesDispositionMessage extends Message {
	public Vector<IdentifiedEntityDispositionInformations> dispositions;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.dispositions = new Vector<IdentifiedEntityDispositionInformations>();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.dispositions.add(new IdentifiedEntityDispositionInformations(this.content));
	}
}