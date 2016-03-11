package messages.fights;

import gamedata.context.FightTeamInformations;
import utilities.ByteArray;
import messages.Message;

public class GameFightUpdateTeamMessage extends Message {
	public int fightId = 0;
	public FightTeamInformations team;
	
	public GameFightUpdateTeamMessage(Message msg) {
		super(msg);
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.fightId = buffer.readShort();
		this.team = new FightTeamInformations(buffer);
	}
}