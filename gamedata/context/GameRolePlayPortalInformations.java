package gamedata.context;

import gamedata.ProtocolTypeManager;
import utilities.ByteArray;

public class GameRolePlayPortalInformations extends GameRolePlayActorInformations {
	public PortalInformation portal;

	public GameRolePlayPortalInformations(ByteArray buffer) {
		super(buffer);
		this.portal = (PortalInformation) ProtocolTypeManager.getInstance(buffer.readShort(), buffer);
	}
}