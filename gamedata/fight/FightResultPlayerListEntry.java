package gamedata.fight;

import gamedata.ProtocolTypeManager;

import java.util.Vector;

import utilities.ByteArray;

public class FightResultPlayerListEntry extends FightResultFighterListEntry {
	public int level = 0;
	public Vector<FightResultAdditionalData> additional;
	
	public FightResultPlayerListEntry(ByteArray buffer) {
		super(buffer);
		this.additional = new Vector<FightResultAdditionalData>();
		this.level = buffer.readByte();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.additional.add((FightResultAdditionalData) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}