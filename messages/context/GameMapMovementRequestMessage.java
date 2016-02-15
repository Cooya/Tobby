package messages.context;

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
	
	public void serialize(Vector<Integer> keyMovements, int mapId, int instanceId) {
		this.keyMovements = keyMovements;
		this.mapId = mapId;
		
		ByteArray buffer = new ByteArray();
		buffer.writeShort((short) this.keyMovements.size());
		for(int i : this.keyMovements)
			buffer.writeShort((short) i);
		buffer.writeInt(mapId);
		
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}
}