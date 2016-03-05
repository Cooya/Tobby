package gamedata.d2p;

import gamedata.d2p.ankama.Map;
import utilities.ByteArray;

public class Cell {
	public static final int WIDTH = 86;
	public static final int HALF_WIDTH = 43;
	public static final double HALF_HEIGHT = 21.5;
	public int id;
	public int speed;
	public int mapChangeData;
	public int moveZone;
	private int _losmov = 3;
	private int _floor;
	private Map _map;
	private int _arrow = 0;
	private boolean _mov;
	private boolean _los;
	private boolean _nonWalkableDuringFight;
	private boolean _red;
	private boolean _blue;
	private boolean _farmCell;
	private boolean _visible;
	private boolean _nonWalkableDuringRP;
	
	public double x;
	public double y;
	
	public Cell(Map map, int id) {
		this._map = map;
		this.id = id;
		/*
		int tmp = id % Map.WIDTH;
		this.x = (id / Map.WIDTH) % 2 == 0 ? HALF_WIDTH + WIDTH * tmp : WIDTH * (tmp + 1); 
		this.y = HALF_HEIGHT * (id / Map.WIDTH + 1);
		*/
		int tmp = id % (Map.WIDTH * 2);
		if(tmp < Map.WIDTH)
			this.x = tmp * 2;
		else
			this.x = (tmp % Map.WIDTH) * 2 + 1; 
		this.y = id / (Map.WIDTH * 2);
	}
	
	public Map getMap() {
		return this._map;
	}
	
	public boolean getMov() {
		return this._mov;
	}
	
	public boolean getLos() {
		return this._los;
	}
	
	public boolean getNonWalkableDuringFight() {
		return this._nonWalkableDuringFight;
	}
	
	public boolean getRed() {
		return this._red;
	}
	
	public boolean getBlue() {
		return this._blue;
	}
	
	public boolean getFarmCell() {
		return this._farmCell;
	}
	
	public boolean getVisible() {
		return this._visible;
	}
	
	public boolean getNonWalkableDuringRP() {
		return this._nonWalkableDuringRP;
	}
	
	public int getFloor() {
		return this._floor;
	}
	
	public boolean getUseTopArrow() {
		return (this._arrow & 1) != 0;
	}
	
	public boolean getUseBottomArrow() {
		return (this._arrow & 2) != 0;
	}
	
	public boolean getUseRightArrow() {
		return (this._arrow & 4) != 0;
	}
	
	public boolean getUseLeftArrow() {
		return (this._arrow & 8) != 0;
	}
	
	public void fromRaw(ByteArray raw) {
		this._floor = raw.readByte() * 10;
		if(this._floor == -1280)
			return;
		this._losmov = raw.readByte();
		this.speed = raw.readByte();
		this.mapChangeData = raw.readByte();
		if(this._map.mapVersion > 5)
			this.moveZone = raw.readByte();
		if(this._map.mapVersion > 7) {
			this._arrow = 15 & raw.readByte();
			if(this.getUseTopArrow())
				this._map.topArrowCell.add(this.id);
			if(this.getUseBottomArrow())
				this._map.bottomArrowCell.add(this.id);
			if(this.getUseLeftArrow())
				this._map.leftArrowCell.add(this.id);
			if(this.getUseRightArrow())
				this._map.rightArrowCell.add(this.id);
		}
		this._los = ((this._losmov & 2) >> 1) == 1;
		this._mov = (this._losmov & 1) == 1;
		this._visible = ((this._losmov & 64) >> 6) == 1;
		this._farmCell = ((this._losmov & 32) >> 5) == 1;
	    this._blue = ((this._losmov & 16) >> 4) == 1;
	    this._red = ((this._losmov & 8) >> 3) == 1;
	    this._nonWalkableDuringRP = ((this._losmov & 128) >> 7) == 1;
	    this._nonWalkableDuringFight = ((this._losmov & 4) >> 2) == 1;	
	}
	
	public boolean equals(Cell cell) {
		return this.id == cell.id;
		//return this.x == cell.x && this.y == cell.y;
	}
	
	public boolean isAccessibleDuringRP() {
		return !this._nonWalkableDuringRP && this._floor == 0 && this._mov;	
	}
	
	public boolean allowsChangementMap() {
		return this.mapChangeData != 0;
	}
	
	public String toString() {
		return "[x = " + this.x + ", y = " + this.y + ", id = " + this.id + "]";
	}
	
	public static double distanceBetween(Cell cell1, Cell cell2) {
		return Math.sqrt(Math.pow(cell1.x - cell2.x, 2) + Math.pow(cell1.y - cell2.y, 2));
	}
}