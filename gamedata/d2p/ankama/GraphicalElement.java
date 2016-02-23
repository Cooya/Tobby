package gamedata.d2p.ankama;

import java.awt.Point;

import utilities.ByteArray;

public class GraphicalElement extends BasicElement {
	private static final int CELL_HALF_WIDTH = 43;
	private static final double CELL_HALF_HEIGHT = 21.5;
	public int elementId;
	public ColorMultiplicator hue;
	public ColorMultiplicator shadow;
	public ColorMultiplicator finalTeint;
	public Point offset;
	public Point pixelOffset;
	public int altitude;
	public int identifier;
	
	public GraphicalElement(Cell cell) {
		super(cell);
	}
	
	public int getElementType() {
		return GRAPHICAL;
	}
	
	public ColorMultiplicator getColorMultiplicator() {
		return this.finalTeint;
	}
	
	public void calculateFinalTeint() {
		double d1 = this.hue.red + this.shadow.red;
		double d2 = this.hue.green + this.shadow.green;
		double d3 = this.hue.blue + this.shadow.blue;
		d1 = ColorMultiplicator.clamp(((d1 + 128) * 2), 0, 0x0200);
		d2 = ColorMultiplicator.clamp(((d2 + 128) * 2), 0, 0x0200);
		d3 = ColorMultiplicator.clamp(((d3 + 128) * 2), 0, 0x0200);
		this.finalTeint = new ColorMultiplicator((int) d1, (int) d2, (int) d3, true);
	}
	
	public void fromRaw(ByteArray raw, int mapVersion) {
		this.elementId = raw.readInt();
		this.hue = new ColorMultiplicator(raw.readByte(), raw.readByte(), raw.readByte(), false);
		this.shadow = new ColorMultiplicator(raw.readByte(), raw.readByte(), raw.readByte(), false);
        this.offset = new Point();
        this.pixelOffset = new Point();
        if (mapVersion <= 4) {
            this.offset.x = raw.readByte();
            this.offset.y = raw.readByte();
            this.pixelOffset.x = (this.offset.x * CELL_HALF_WIDTH);
            this.pixelOffset.y = (int) (this.offset.y * CELL_HALF_HEIGHT);
        }
        else {
            this.pixelOffset.x = raw.readShort();
            this.pixelOffset.y = raw.readShort();
            this.offset.x = (this.pixelOffset.x / CELL_HALF_WIDTH);
            this.offset.y = (int) (this.pixelOffset.y / CELL_HALF_HEIGHT);
        }
        this.altitude = raw.readByte();
        this.identifier = raw.readInt();
        calculateFinalTeint();
	}
}
