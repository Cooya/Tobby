package messages.context;

import gamedata.ProtocolTypeManager;
import gamedata.currentmap.FightCommonInformations;
import gamedata.currentmap.GameRolePlayActorInformations;
import gamedata.currentmap.HouseInformations;
import gamedata.currentmap.InteractiveElement;
import gamedata.currentmap.MapObstacle;
import gamedata.currentmap.StatedElement;

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
		
		int nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			houses.add((HouseInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			actors.add((GameRolePlayActorInformations) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i)
			interactiveElements.add((InteractiveElement) ProtocolTypeManager.getInstance(buffer.readShort(), buffer));
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
}