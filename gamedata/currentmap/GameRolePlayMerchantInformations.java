package gamedata.currentmap;

import gamedata.ProtocolTypeManager;

import java.util.Vector;

import utilities.ByteArray;

public class GameRolePlayMerchantInformations extends GameRolePlayNamedActorInformations {
	public int sellType = 0;
	public Vector<HumanOption> options;
	
	public GameRolePlayMerchantInformations(ByteArray buffer) {
		super(buffer);
		options = new Vector<HumanOption>();
		this.sellType = buffer.readByte();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			options.add((HumanOption) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
	}
}