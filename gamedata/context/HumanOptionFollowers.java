package gamedata.context;

import utilities.ByteArray;

public class HumanOptionFollowers extends HumanOption {
	public IndexedEntityLook[] followingCharactersLook;
	
	public HumanOptionFollowers(ByteArray buffer) {
		super(buffer);
		int nb = buffer.readShort();
		this.followingCharactersLook = new IndexedEntityLook[nb];
		for(int i = 0; i < nb; ++i)
			this.followingCharactersLook[i] = new IndexedEntityLook(buffer);
	}
}