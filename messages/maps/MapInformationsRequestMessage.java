package messages.maps;

import utilities.ByteArray;
import messages.Message;

public class MapInformationsRequestMessage extends Message {

	public MapInformationsRequestMessage() {
		super();
	}

	public void serialize(CurrentMapMessage CMM) {
		ByteArray buffer = new ByteArray();
		buffer.writeInt(CMM.getId());
		
		completeInfos(buffer);
	}
}
