package messages.fights;

import gamedata.context.FightTeamInformations;
import messages.NetworkMessage;

public class GameFightUpdateTeamMessage extends NetworkMessage {
	public int fightId = 0;
	public FightTeamInformations team;
	
	@Override
	public void serialize() {
		// not implemented yet
	}
	
	@Override
	public void deserialize() {
		this.fightId = this.content.readShort();
		this.team = new FightTeamInformations(this.content);
	}
}