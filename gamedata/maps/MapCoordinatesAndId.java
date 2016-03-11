package gamedata.maps;

import utilities.ByteArray;

public class MapCoordinatesAndId extends MapCoordinates {
	public int mapId = 0;

	public MapCoordinatesAndId(ByteArray buffer) {
		super(buffer);
		this.mapId = buffer.readInt();
	}
}