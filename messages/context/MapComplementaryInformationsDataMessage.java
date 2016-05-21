package messages.context;

import gamedata.ProtocolTypeManager;
import gamedata.context.FightCommonInformations;
import gamedata.context.GameRolePlayActorInformations;
import gamedata.context.HouseInformations;
import gamedata.context.InteractiveElement;
import gamedata.context.MapObstacle;
import gamedata.context.StatedElement;

import java.util.Vector;

import messages.NetworkMessage;

public class MapComplementaryInformationsDataMessage extends NetworkMessage {
    public int subAreaId = 0;
    public int mapId = 0;
    public Vector<HouseInformations> houses;
    public Vector<GameRolePlayActorInformations> actors;
    public Vector<InteractiveElement> interactiveElements;
    public Vector<StatedElement> statedElements;
    public Vector<MapObstacle> obstacles;
    public Vector<FightCommonInformations> fights;
    
    @Override
	public void serialize() {
		// not implemented yet
	}

    @Override
	public void deserialize() {
		this.houses = new Vector<HouseInformations>();
		this.actors = new Vector<GameRolePlayActorInformations>();
		this.interactiveElements = new Vector<InteractiveElement>();
		this.statedElements = new Vector<StatedElement>();
		this.obstacles = new Vector<MapObstacle>();
		this.fights = new Vector<FightCommonInformations>();
		this.subAreaId = this.content.readVarShort();
		this.mapId = this.content.readInt();
		int nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			houses.add((HouseInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			actors.add((GameRolePlayActorInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			interactiveElements.add((InteractiveElement) ProtocolTypeManager.getInstance(this.content.readShort(), this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			statedElements.add(new StatedElement(this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			obstacles.add(new MapObstacle(this.content));
		nb = this.content.readShort();
		for(int i = 0; i < nb; ++i)
			fights.add(new FightCommonInformations(this.content));
	}
}