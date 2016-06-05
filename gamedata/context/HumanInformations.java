package gamedata.context;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class HumanInformations {
	public ActorRestrictionsInformations restrictions;
	public boolean sex = false;
	public HumanOption[] options;

	public HumanInformations(ByteArray buffer) {
		this.restrictions = new ActorRestrictionsInformations(buffer);
		this.sex = buffer.readBoolean();
		int nb = buffer.readShort();
		this.options = new HumanOption[nb];
		for(int i = 0; i < nb; ++i)
			this.options[i] = (HumanOption) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}