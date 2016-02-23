package gamedata.currentmap;

import java.util.Vector;

import utilities.ByteArray;

public class GroupMonsterStaticInformations {
    public MonsterInGroupLightInformations mainCreatureLightInfos;
    public Vector<MonsterInGroupInformations> underlings;

	public GroupMonsterStaticInformations(ByteArray buffer) {
		this.underlings = new Vector<MonsterInGroupInformations>();
		
		this.mainCreatureLightInfos = new MonsterInGroupLightInformations(buffer);
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.underlings.add(new MonsterInGroupInformations(buffer));
	}
}