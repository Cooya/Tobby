package messages.fight;

import gamedata.ProtocolTypeManager;
import gamedata.fight.FightResultListEntry;
import gamedata.fight.NamedPartyTeamWithOutcome;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class GameFightEndMessage extends Message{
	public int duration = 0;   
	public int ageBonus = 0;
	public int lootShareLimitMalus = 0;
	public Vector<FightResultListEntry> results;
	public Vector<NamedPartyTeamWithOutcome> namedPartyTeamsOutcomes;

	public GameFightEndMessage(Message msg) {
		super(msg);
		this.results = new Vector<FightResultListEntry>();
		this.namedPartyTeamsOutcomes = new Vector<NamedPartyTeamWithOutcome>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.duration = buffer.readInt();
		this.ageBonus = buffer.readShort();
		this.lootShareLimitMalus = buffer.readShort();
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.results.add((FightResultListEntry) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			this.namedPartyTeamsOutcomes.add(new NamedPartyTeamWithOutcome(buffer));
	}
}