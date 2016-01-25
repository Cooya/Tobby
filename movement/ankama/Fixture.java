package movement.ankama;

import java.awt.Point;

import utilities.ByteArray;

public class Fixture {
	public int fixtureId;
	public Point offset;
	public int hue;
	public int redMultiplier;
	public int greenMultiplier;
	public int blueMultiplier;
	public int alpha;
	public int xScale;
	public int yScale;
	public int rotation;
	private Map _map;

	public Fixture(Map map) {
		this._map = map;
	}
	
	public Map getMap() {
		return this._map;
	}
	
	public void fromRaw(ByteArray raw) {
		this.fixtureId = raw.readInt();
        this.offset = new Point();
        this.offset.x = raw.readShort();
        this.offset.y = raw.readShort();
        this.rotation = raw.readShort();
        this.xScale = raw.readShort();
        this.yScale = raw.readShort();
        this.redMultiplier = raw.readByte();
        this.greenMultiplier = raw.readByte();
        this.blueMultiplier = raw.readByte();
        this.hue = ((this.redMultiplier | this.greenMultiplier) | this.blueMultiplier);
        this.alpha = raw.readByte();
	}
}