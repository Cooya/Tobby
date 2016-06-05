package gamedata.context;

import utilities.ByteArray;

public class EntityLook {
	public int bonesId = 0;
	public int[] skins;
	public int[] indexedColors;
	public int[] scales;
	public SubEntity[] subentities;

	public EntityLook(ByteArray buffer) {
		this.bonesId = buffer.readVarShort();
		int nb = buffer.readShort();
		this.skins = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.skins[i] = buffer.readVarShort();
		nb = buffer.readShort();
		this.indexedColors = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.indexedColors[i] = buffer.readInt();
		nb = buffer.readShort();
		this.scales = new int[nb];
		for(int i = 0; i < nb; ++i)
			this.scales[i] = buffer.readVarShort();
		nb = buffer.readShort();
		this.subentities = new SubEntity[nb];
		for(int i = 0; i < nb; ++i)
			this.subentities[i] = new SubEntity(buffer);
	}
}