package gamedata.context;

import utilities.ByteArray;

public class GameRolePlayNpcInformations extends GameRolePlayActorInformations {
    public int npcId = 0;
    public boolean sex = false;
    public int specialArtworkId = 0;
	
	public GameRolePlayNpcInformations(ByteArray buffer) {
		super(buffer);
		this.npcId = buffer.readVarShort();
		this.sex = buffer.readBoolean();
		this.specialArtworkId = buffer.readVarShort();
	}
}
