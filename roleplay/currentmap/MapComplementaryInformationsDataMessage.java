package roleplay.currentmap;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class MapComplementaryInformationsDataMessage extends Message {
    public int subAreaId = 0;
    public int mapId = 0;
    public Vector<HouseInformations> houses;
    public Vector<GameRolePlayActorInformations> actors;
    public Vector<InteractiveElement> interactiveElements;
    public Vector<StatedElement> statedElements;
    public Vector<MapObstacle> obstacles;
    public Vector<FightCommonInformations> fights;

	public MapComplementaryInformationsDataMessage(Message msg) {
		super(msg);
		houses = new Vector<HouseInformations>();
		actors = new Vector<GameRolePlayActorInformations>();
		interactiveElements = new Vector<InteractiveElement>();
		statedElements = new Vector<StatedElement>();
		obstacles = new Vector<MapObstacle>();
		fights = new Vector<FightCommonInformations>();
		deserialize();
	}

	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		this.subAreaId = buffer.readVarShort();
		this.mapId = buffer.readInt();
		int protocolId;
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			protocolId = buffer.readShort();
			if(protocolId == 111)
				houses.add(new HouseInformations(buffer));
			else if(protocolId == 112)
				houses.add(new HouseInformationsExtended(buffer));
			else
				throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
		}
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			protocolId = buffer.readShort();
			if(protocolId == 36)
				actors.add(new GameRolePlayCharacterInformations(buffer));
			else if(protocolId == 156)
				actors.add(new GameRolePlayNpcInformations(buffer));
			else if(protocolId == 160)
				actors.add(new GameRolePlayGroupMonsterInformations(buffer));
			else if(protocolId == 383)
				actors.add(new GameRolePlayNpcWithQuestInformations(buffer));
			else
				throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
		}
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			protocolId = buffer.readShort();
			if(protocolId == 80)
				interactiveElements.add(new InteractiveElement(buffer));
			else if(protocolId == 398)
				interactiveElements.add(new InteractiveElementWithAgeBonus(buffer));
			else
				throw new Error("Invalid or unhandled protocol id : " + protocolId + ".");
		}
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			statedElements.add(new StatedElement(buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			obstacles.add(new MapObstacle(buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			fights.add(new FightCommonInformations(buffer));
	}
	
	public EntityDispositionInformations getCharacterDisposition(double characterId) {
		for(GameRolePlayActorInformations actor : actors) {
			if(actor.contextualId == characterId)
				return actor.disposition;
		}
		return null;	
	}
	
	public String getCharacterName(double characterId) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == characterId)
				return ((GameRolePlayNamedActorInformations) actor).name;
		return null;
	}
}