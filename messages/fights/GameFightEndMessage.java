package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.FightResultListEntry;
import gamedata.fight.NamedPartyTeamWithOutcome;

import java.util.Vector;

import messages.Message;

public class GameFightEndMessage extends Message{
	public int duration = 0;   
	public int ageBonus = 0;
	public int lootShareLimitMalus = 0;
	public Vector<FightResultListEntry> results;
	public Vector<NamedPartyTeamWithOutcome> namedPartyTeamsOutcomes;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.results = new Vector<FightResultListEntry>();
		this.namedPartyTeamsOutcomes = new Vector<NamedPartyTeamWithOutcome>();
		this.duration = this.content.readInt();
		this.ageBonus = this.content.readShort();
		this.lootShareLimitMalus = this.content.readShort();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.results.add((FightResultListEntry) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			this.namedPartyTeamsOutcomes.add(new NamedPartyTeamWithOutcome(this.content));
	}
}