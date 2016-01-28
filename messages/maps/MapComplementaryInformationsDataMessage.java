package messages.maps;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class MapComplementaryInformationsDataMessage extends Message {
    public int subAreaId = 0;
    public int mapId = 0;
    public Vector<HouseInformations> houses;
    public Vector<GameRolePlayCharacterInformations> actors;
    public Vector<InteractiveElement> interactiveElements;
    public Vector<StatedElement> statedElements;
    public Vector<MapObstacle> obstacles;
    public Vector<FightCommonInformations> fights;

	public MapComplementaryInformationsDataMessage(Message msg) {
		super(msg);
		houses = new Vector<HouseInformations>();
		actors = new Vector<GameRolePlayCharacterInformations>();
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
		for(int i = 0; i < nb; ++i) {
			buffer.readShort();
			houses.add(new HouseInformations(buffer));
		}
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			buffer.readShort();
			actors.add(new GameRolePlayCharacterInformations(buffer));
		}
		nb = buffer.readShort();
		for(int i = 0; i < nb; ++i) {
			buffer.readShort();
			interactiveElements.add(new InteractiveElement(buffer));
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
}
