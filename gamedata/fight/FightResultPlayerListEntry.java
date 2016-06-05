package gamedata.fight;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class FightResultPlayerListEntry extends FightResultFighterListEntry {
	public int level = 0;
	public FightResultAdditionalData[] additional;

	public FightResultPlayerListEntry(ByteArray buffer) {
		super(buffer);
		this.level = buffer.readByte();
		int nb = buffer.readShort();
		this.additional = new FightResultAdditionalData[nb];
		for(int i = 0; i < nb; ++i)
			this.additional[i] = (FightResultAdditionalData) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}