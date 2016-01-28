package roleplay.movement.ankama;

import java.awt.Point;
import java.util.Vector;

public class MapPoint {
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	public static final int MAP_WIDTH = 14;
	public static final int MAP_HEIGHT = 20;
	public static Vector<Point> CELLPOS = new Vector<Point>();
	private static final Point  VECTOR_RIGHT = new Point(1, 1);
	private static final Point VECTOR_DOWN_RIGHT = new Point(1, 0);
	private static final Point VECTOR_DOWN = new Point(1, -1);
	private static final Point VECTOR_DOWN_LEFT = new Point(0, -1);
	private static final Point VECTOR_LEFT = new Point(-1, -1);
	private static final Point VECTOR_UP_LEFT = new Point(-1, 0);
	private static final Point VECTOR_UP = new Point(-1, 1);
	private static final Point VECTOR_UP_RIGHT = new Point(0, 1);
	private static boolean _bInit = false;
	private int _nCellId;
	private int _nX;
	private int _nY;

	public static MapPoint fromCellId(int cellId) {
		MapPoint mp = new MapPoint();
		mp._nCellId = cellId;
		mp.setFromCellId();
		return mp;
	}

	public static MapPoint fromCoords(int x, int y) {
		MapPoint mp = new MapPoint();
		mp._nX = x;
		mp._nY = y;
		mp.setFromCoords();
		return mp;
	}

	public static int getOrientationsDistance(int i1, int i2) {
		return Math.min(Math.abs(i2 - i1), Math.abs(8 - i2 + i1));
	}

	public static boolean isInMap(int i1, int i2) {
		return i1 + i2 >= 0 && i1 - i2 >= 0 && i1 - i2 < MAP_HEIGHT * 2 && i1 + i2 < MAP_WIDTH * 2;
	}

	private static void init() {
		_bInit = true;
		int i1 = 0;
		int i2 = 0;
		int i3 = 0;
		for(int i = 0; i < MAP_HEIGHT; ++i) {
			for(int j = 0; j < MAP_WIDTH; ++j) {
				CELLPOS.add(i3, new Point(i1 + j, i2 + j));
				i3++;
			}
			i1++;
			for(int j = 0; j < MAP_WIDTH; ++j) {
				CELLPOS.add(i3, new Point(i1 + j, i2 + j));
				i3++;
			}
			i2--;
		}
	}

	public int getCellId() {
		return this._nCellId;
	}

	public void setCellId(int i) {
		this._nCellId = i;
		this.setFromCellId();
	}

	public int getX() {
		return this._nX;
	}

	public void setX(int i) {
		this._nX = i;
		this.setFromCoords();
	}

	public int getY() {
		return this._nY;
	}

	public void setY(int i) {
		this._nY = i;
		this.setFromCoords();
	}

	public Point getCoordinates() {
		return new Point(this._nX, this._nY);
	}

	public int distanceTo(MapPoint mp) {
		return (int) Math.sqrt(Math.pow(mp._nX - this._nX, 2) + Math.pow(mp._nY - this._nY, 2));
	}

	public int distanceToCell(MapPoint mp) {
		return Math.abs(this._nX - mp._nX) + Math.abs(this._nY - mp._nY);
	}

	public int orientationTo(MapPoint mp) {
		if(this._nX == mp._nX && this._nY == mp._nY)
			return 1;
		Point p = new Point();
		p.x = mp._nX > this._nX ? 1 : mp._nX < this._nX ? -1 : 0;
		p.y = mp._nY > this._nY ? 1 : mp._nY < this._nY ? -1 : 0;
		int nb = 0;
		if(p.x == VECTOR_RIGHT.x && p.y == VECTOR_RIGHT.y)
			nb = RIGHT;
		else if(p.x == VECTOR_DOWN_RIGHT.x && p.y == VECTOR_DOWN_RIGHT.y)
			nb = DOWN_RIGHT;
		else if(p.x == VECTOR_DOWN.x && p.y == VECTOR_DOWN.y)
			nb = DOWN;
		else if(p.x == VECTOR_DOWN_LEFT.x && p.y == VECTOR_DOWN_LEFT.y)
			nb = DOWN_LEFT;
		else if(p.x == VECTOR_LEFT.x && p.y == VECTOR_LEFT.y)
			nb = LEFT;
		else if(p.x == VECTOR_UP_LEFT.x && p.y == VECTOR_UP_LEFT.y)
			nb = UP_LEFT;
		else if(p.x == VECTOR_UP.x && p.y == VECTOR_UP.y)
			nb = UP;
		else if(p.x == VECTOR_UP_RIGHT.x && p.y == VECTOR_UP_RIGHT.y)
			nb = UP_RIGHT;
		return nb;
	}

	public int advancedOrientationTo(MapPoint mp, boolean b) {
		if(mp == null)
			return 0;
		int i1 = mp._nX - this._nX;
		int i2 = this._nY - mp._nY;
		int i3 = (int) (Math.acos(i1 / Math.sqrt(Math.pow(i1, 2) + Math.pow(i2, 2))) * 180 / Math.PI * (mp._nY > this._nY ? -1 : 1));
		if(b)
			i3 = Math.round(i3 / 90) * 2 + 1;
		else
			i3 = Math.round(i3 / 45) + 1;
		if(i3 < 0)
			i3 += 8;
		return i3;
	}

	/*
	
	public MapPoint getNearestFreeCell(DataMapProvider dmp, boolean b) {
		MapPoint mp = null;
		for(int i = 0; i < 8; ++i) {
			mp = getNearestFreeCellInDirection(i, dmp, false, b, false, null);
			if(mp != null) break;
		}
		return mp;
	}

	public MapPoint getNearestCellInDirection(int i) {
		MapPoint mp = null;
		switch(i) {
			case 0: mp = MapPoint.fromCoords(this._nX + 1, this._nY + 1); break;
			case 1: mp = MapPoint.fromCoords(this._nX + 1, this._nY); break;
			case 2: mp = MapPoint.fromCoords(this._nX + 1, this._nY - 1); break;
			case 3: mp = MapPoint.fromCoords(this._nX, this._nY - 1); break;
			case 4: mp = MapPoint.fromCoords(this._nX - 1, this._nY - 1); break;
			case 5: mp = MapPoint.fromCoords(this._nX - 1, this._nY); break;
			case 6: mp = MapPoint.fromCoords(this._nX - 1, this._nY + 1); break;
			case 7: mp = MapPoint.fromCoords(this._nX, this._nY + 1); break;
		}
		if(mp != null && MapPoint.isInMap(mp._nX, mp._nY))
			return mp;
		return null;
	}

	public MapPoint getNearestFreeCellInDirection(int i, DataMapProvider dmp, boolean b1, boolean b2, boolean b3, Vector<Point> vector) {
		if(vector == null)
			vector = new Vector<Point>();
		Vector<MapPoint> vector1 = new Vector<MapPoint>();
		Vector<Integer> vector2 = new Vector<Integer>();
		MapPoint mp;
		int i2;
		for(int j = 0; j < 8; ++j) {
			mp = getNearestCellInDirection(j);
			if(mp != null && vector.indexOf(mp.getCellId()) == -1) {
				i2 = dmp.getCellSpeed(mp.getCellId());
				if(!dmp.pointMov(mp._nX, mp._nX, b2, this._nCellId, -1))
					i2 = -100;
				vector2.add(getOrientationsDistance(j, i) + (b3 ? 0 : (i2 >= 0) ? (5 - i2) : (11 + Math.abs(i2))));
			}
			else
				vector2.add(1000);
			vector1.add(mp);
		}
		int i0 = vector2.get(0);
		int i3;
		int i4 = -1;
		for(int j = 0; j < 8; ++j) {
			i3 = vector2.get(j);
			if(i3 < i0 && vector1.get(j) != null) {
				i0 = i3;
				i4 = j;
			}
		}
		mp = vector1.get(i4);
		if(mp == null && b1 && dmp.pointMov(this._nX, this._nY, b2, this._nCellId, -1))
			return this;
		return mp;
	}
	
	*/

	public MapPoint pointSymetry(MapPoint mp) {
		int i1 = 2 * mp._nX - this._nX;
		int i2 = 2 * mp._nY - this._nY;
		if(isInMap(i1, i2))
			return MapPoint.fromCoords(i1, i2);
		return null;
	}

	public boolean equals(MapPoint mp) {
		return mp._nCellId == this._nCellId;
	}

	public String toString() {
		return "[MapPoint(x:" + this._nX + ", y:" + this._nY + ", id:" + this._nCellId + ")]";
	}

	private void setFromCoords() {
		if(!_bInit)
			init();
		this._nCellId = (this._nX - this._nY) * MAP_WIDTH + this._nY + (this._nX - this._nY) / 2;
	}

	private void setFromCellId() {
		if(!_bInit)
			init();
		Point p = CELLPOS.get(this._nCellId);
		if (p == null)
			throw new Error("Cell identifier out of bound.");
		this._nX = p.x;
		this._nY = p.y;
	}
}