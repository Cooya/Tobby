package messages.currentmap;

import java.util.Vector;

import main.Emulation;
import messages.Message;
import utilities.ByteArray;

public class GameMapMovementRequestMessage extends Message {
    public Vector<Integer> keyMovements;
    public int mapId = 0;
    
	public GameMapMovementRequestMessage() {
		super();
	}
	
	public void serialize(Vector<Integer> keyMovements, int mapId) {
		this.keyMovements = keyMovements;
		this.mapId = mapId;
		
		ByteArray buffer = new ByteArray();
		buffer.writeShort((short) this.keyMovements.size());
		for(int i : this.keyMovements)
			buffer.writeShort((short) i);
		buffer.writeInt(mapId);
		
		Emulation.hashMessage(buffer);
		completeInfos(buffer);
	}
}
