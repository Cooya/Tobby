package messages.fights;

import gamedata.ProtocolTypeManager;
import gamedata.fight.FightResultListEntry;
import gamedata.fight.NamedPartyTeamWithOutcome;

import messages.NetworkMessage;

public class GameFightEndMessage extends NetworkMessage{
	public int duration = 0;   
	public int ageBonus = 0;
	public int lootShareLimitMalus = 0;
	public FightResultListEntry[] results;
	public NamedPartyTeamWithOutcome[] namedPartyTeamsOutcomes;

	@Override
	public void serialize() {
		
	}

	@Override
	public void deserialize() {
		this.duration = this.content.readInt();
		this.ageBonus = this.content.readShort();
		this.lootShareLimitMalus = this.content.readShort();
		int nb = this.content.readShort();
		this.results = new FightResultListEntry[nb];
		for(int i = 0; i < nb; ++i)
			this.results[i] = (FightResultListEntry) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
		nb = this.content.readShort();
		this.namedPartyTeamsOutcomes = new NamedPartyTeamWithOutcome[nb];
		for(int i = 0; i < nb; ++i)
			this.namedPartyTeamsOutcomes[i] = new NamedPartyTeamWithOutcome(this.content);
	}
}