package messages.fights;

import gamedata.context.FightTeamInformations;
import messages.Message;

public class GameFightUpdateTeamMessage extends Message {
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