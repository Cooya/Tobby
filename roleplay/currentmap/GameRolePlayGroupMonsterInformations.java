package roleplay.currentmap;

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
        int protocolId = buffer.readShort();
        if(protocolId == 140)
        	this.staticInfos = new GroupMonsterStaticInformations(buffer);
        else
        	throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
        this.creationTime = buffer.readDouble();
        this.ageBonusRate = buffer.readInt();
        this.lootShare = buffer.readByte();
        this.alignmentSide = buffer.readByte();
	}
}