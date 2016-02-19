package game.currentmap;

import java.util.Vector;

import utilities.ByteArray;

public class HumanOptionFollowers extends HumanOption {
	public Vector<IndexedEntityLook> followingCharactersLook;
	
	public HumanOptionFollowers(ByteArray buffer) {
		super(buffer);
		this.followingCharactersLook = new Vector<IndexedEntityLook>();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.followingCharactersLook.add(new IndexedEntityLook(buffer));
	}
}