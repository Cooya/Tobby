package roleplay.currentmap;

import utilities.ByteArray;

public class MonsterInGroupLightInformations {
    public int creatureGenericId = 0;
    public int grade = 0;

	public MonsterInGroupLightInformations(ByteArray buffer) {
        this.creatureGenericId = buffer.readInt();
        this.grade = buffer.readByte();
	}
}