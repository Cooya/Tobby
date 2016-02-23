package gamedata.currentmap;

import utilities.ByteArray;

public class ActorOrientation {
	public double id = 0;
	public int direction = 1;
	
	public ActorOrientation(ByteArray buffer) {
		this.id = buffer.readDouble();
		this.direction = buffer.readByte();
	}
}