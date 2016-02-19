package game.currentmap;

import utilities.ByteArray;

public class MonsterInGroupInformations extends MonsterInGroupLightInformations {
	public EntityLook look;

	public MonsterInGroupInformations(ByteArray buffer) {
		super(buffer);
		this.look = new EntityLook(buffer);
	}
}
