package gamedata.maps;

import utilities.ByteArray;

public class MapCoordinatesExtended extends MapCoordinatesAndId {
	public int subAreaId = 0;

	public MapCoordinatesExtended(ByteArray buffer) {
		super(buffer);
		this.subAreaId = buffer.readVarShort();
	}
}