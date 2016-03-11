package gamedata.context;

import gamedata.ProtocolTypeManager;
import utilities.ByteArray;

public class GameRolePlayTaxCollectorInformations extends GameRolePlayActorInformations {
	public TaxCollectorStaticInformations identification; 
	public int guildLevel = 0;
	public int taxCollectorAttack = 0;

	public GameRolePlayTaxCollectorInformations(ByteArray buffer) {
		super(buffer);
		this.identification = (TaxCollectorStaticInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
		this.guildLevel = buffer.readByte();
		this.taxCollectorAttack = buffer.readInt();
	}
}