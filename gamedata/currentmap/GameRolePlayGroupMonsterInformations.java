package gamedata.currentmap;

import gamedata.ProtocolTypeManager;
import utilities.BooleanByteWrapper;
import utilities.ByteArray;

public class GameRolePlayGroupMonsterInformations extends GameRolePlayActorInformations {
    public GroupMonsterStaticInformations staticInfos;
    public double creationTime = 0;
    public int ageBonusRate = 0;
    public int lootShare = 0;
    public int alignmentSide = 0;
    public boolean keyRingBonus = false;
    public boolean hasHardcoreDrop = false;
    public boolean hasAVARewardToken = false;

	public GameRolePlayGroupMonsterInformations(ByteArray buffer) {
		super(buffer);
		int nb = buffer.readByte();
        this.keyRingBonus = BooleanByteWrapper.getFlag(nb, 0);
        this.hasHardcoreDrop = BooleanByteWrapper.getFlag(nb, 1);
        this.hasAVARewardToken = BooleanByteWrapper.getFlag(nb, 2);
        this.staticInfos = (GroupMonsterStaticInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
        this.creationTime = buffer.readDouble();
        this.ageBonusRate = buffer.readInt();
        this.lootShare = buffer.readByte();
        this.alignmentSide = buffer.readByte();
	}
}