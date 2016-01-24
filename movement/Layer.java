package movement;

import java.util.Vector;

import utilities.ByteArray;

public class Layer {
	public static final int LAYER_GROUND = 0;
    public static final int LAYER_ADDITIONAL_GROUND = 1;
    public static final int LAYER_DECOR = 2;
    public static final int LAYER_ADDITIONAL_DECOR = 3;
    public int layerId;
    public int refCell = 0;
    public int cellsCount;
    public Vector<Cell> cells;
    private Map _map;

    public Layer(Map map) {
        this._map = map;
    }
    
    public Map getMap() {
        return this._map;
    }
    
    public void fromRaw(ByteArray raw, int mapVersion) {
        this.layerId = raw.readInt();
        this.cellsCount = raw.readShort();
        this.cells = new Vector<Cell>();
        Cell c;
        for(int i = 0; i < this.cellsCount; ++i) {
            c = new Cell(this);
            c.fromRaw(raw, mapVersion);
            this.cells.add(c);
        }
    }
}
