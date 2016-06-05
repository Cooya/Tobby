package gamedata.context;

import utilities.ByteArray;

public class GroupMonsterStaticInformations {
	public MonsterInGroupLightInformations mainCreatureLightInfos;
	public MonsterInGroupInformations[] underlings;

	public GroupMonsterStaticInformations(ByteArray buffer) {
		this.mainCreatureLightInfos = new MonsterInGroupLightInformations(buffer);
		int nb = buffer.readShort();
		this.underlings = new MonsterInGroupInformations[nb];
		for(int i = 0; i < nb; ++i)
			this.underlings[i] = new MonsterInGroupInformations(buffer);
	}
}