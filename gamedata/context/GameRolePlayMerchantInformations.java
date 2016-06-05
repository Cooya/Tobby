package gamedata.context;

import gamedata.ProtocolTypeManager;

import utilities.ByteArray;

public class GameRolePlayMerchantInformations extends GameRolePlayNamedActorInformations {
	public int sellType = 0;
	public HumanOption[] options;

	public GameRolePlayMerchantInformations(ByteArray buffer) {
		super(buffer);
		this.sellType = buffer.readByte();
		int nb = buffer.readShort();
		this.options = new HumanOption[nb];
		for(int i = 0; i < nb; ++i)
			this.options[i] = (HumanOption) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}