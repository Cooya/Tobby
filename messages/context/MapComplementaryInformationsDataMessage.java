package messages.context;

import gamedata.ProtocolTypeManager;
import gamedata.context.FightCommonInformations;
import gamedata.context.GameRolePlayActorInformations;
import gamedata.context.HouseInformations;
import gamedata.context.InteractiveElement;
import gamedata.context.MapObstacle;
import gamedata.context.StatedElement;

import messages.NetworkMessage;

public class MapComplementaryInformationsDataMessage extends NetworkMessage {
    public int subAreaId = 0;
    public int mapId = 0;
    public HouseInformations[] houses;
    public GameRolePlayActorInformations[] actors;
    public InteractiveElement[] interactiveElements;
    public StatedElement[] statedElements;
    public MapObstacle[] obstacles;
    public FightCommonInformations[] fights;
    
    @Override
	public void serialize() {
		
	}

    @Override
	public void deserialize() {
		this.subAreaId = this.content.readVarShort();
		this.mapId = this.content.readInt();
		int nb = this.content.readShort();
		this.houses = new HouseInformations[nb];
		for(int i = 0; i < nb; ++i)
			houses[i] = (HouseInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
		nb = this.content.readShort();
		this.actors = new GameRolePlayActorInformations[nb];
		for(int i = 0; i < nb; ++i)
			actors[i] = (GameRolePlayActorInformations) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
		nb = this.content.readShort();
		this.interactiveElements = new InteractiveElement[nb];
		for(int i = 0; i < nb; ++i)
			interactiveElements[i] = (InteractiveElement) ProtocolTypeManager.getInstance(this.content.readShort(), this.content);
		nb = this.content.readShort();
		this.statedElements = new StatedElement[nb];
		for(int i = 0; i < nb; ++i)
			statedElements[i] = new StatedElement(this.content);
		nb = this.content.readShort();
		this.obstacles = new MapObstacle[nb];
		for(int i = 0; i < nb; ++i)
			obstacles[i] = new MapObstacle(this.content);
		nb = this.content.readShort();
		this.fights = new FightCommonInformations[nb];
		for(int i = 0; i < nb; ++i)
			fights[i] = new FightCommonInformations(this.content);
	}
}