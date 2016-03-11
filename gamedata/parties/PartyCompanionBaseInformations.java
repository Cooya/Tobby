package gamedata.parties;

import utilities.ByteArray;
import gamedata.context.EntityLook;

public class PartyCompanionBaseInformations {
	public int indexId = 0;
	public int companionGenericId = 0;
	public EntityLook entityLook;
	
	public PartyCompanionBaseInformations(ByteArray buffer) {
		this.indexId = buffer.readByte();
		this.companionGenericId = buffer.readByte();
		this.entityLook = new EntityLook(buffer);
	}
}