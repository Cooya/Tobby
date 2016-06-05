package gamedata.d2p.ankama;

import java.awt.Point;

import utilities.ByteArray;

public class Cell {
	private static final int MAP_WIDTH = 14;
	private static final int CELL_WIDTH = 86;
	private static final int CELL_HALF_WIDTH = 43;
	private static final double CELL_HALF_HEIGHT = 21.5;
	private static Point _cellCoords;
    public int cellId;
    public int elementsCount;
    public BasicElement[] elements;
    private Layer _layer;
    
    public Cell(Layer layer) {
    	this._layer = layer;
    }
    
    public synchronized static Point cellCords(int i) {
    	if(_cellCoords == null)
    		_cellCoords = new Point();
    	_cellCoords.x = i % MAP_WIDTH;
    	_cellCoords.y = (int) Math.floor(i / MAP_WIDTH);
    	return _cellCoords;
    }
    
    public static int cellId(Point p) {
    	return CellIdConverter.coordToCellId(p.x, p.y);
    }
    
    public static int cellIdByXY(int x, int y) {
    	return CellIdConverter.coordToCellId(x, y);
    }
    
    public static Point cellPixelCoords(int i) {
    	Point p = cellCords(i);
    	p.x = p.x * CELL_WIDTH + ((p.y * 2) == 1 ? CELL_HALF_WIDTH : 0);
    	p.y = (int) (p.y * CELL_HALF_HEIGHT);
    	return p;
    }
    
    public Layer getLayer() {
    	return this._layer;
    }
    
    public Point getCoords() {
    	return CellIdConverter.cellIdToCoord(this.cellId);
    }
    
    public Point getPixelCoords() {
    	return cellPixelCoords(this.cellId);
    }
    
    public void fromRaw(ByteArray raw, int mapVersion) {
    	this.cellId = raw.readShort();
    	this.elementsCount = raw.readShort();
    	this.elements = new BasicElement[this.elementsCount];
    	BasicElement be;
    	for(int i = 0; i < this.elementsCount; ++i) {
    		be = BasicElement.getElementFromType(raw.readByte(), this);
    		be.fromRaw(raw, mapVersion);
    		this.elements[i] = be;
    	}
    }
}
