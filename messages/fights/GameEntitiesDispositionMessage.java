package messages.fights;

import gamedata.context.IdentifiedEntityDispositionInformations;

import messages.NetworkMessage;

public class GameEntitiesDispositionMessage extends NetworkMessage {
	public IdentifiedEntityDispositionInformations[] dispositions;
	
	@Override
	public void serialize() {
		
	}
	
	@Override
	public void deserialize() {
		int nb = this.content.readShort();
		this.dispositions = new IdentifiedEntityDispositionInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.dispositions[i] = new IdentifiedEntityDispositionInformations(this.content);
	}
}