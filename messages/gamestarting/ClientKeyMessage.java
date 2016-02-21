package messages.gamestarting;

import game.InterClientKeyManager;
import utilities.ByteArray;
import main.Emulation;
import messages.Message;

public class ClientKeyMessage extends Message {
	
	public ClientKeyMessage() {
		super();
	}
	
	public void serialize(InterClientKeyManager ICKM, int instanceId) {
		ByteArray buffer = new ByteArray();
		buffer.writeUTF(ICKM.getFlashKey());
		
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}
