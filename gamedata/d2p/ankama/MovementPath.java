package gamedata.d2p.ankama;

import java.util.Vector;

public class MovementPath {
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
	public static final int MAX_PATH_LENGTH = 100;
	protected MapPoint _oStart;
	protected MapPoint _oEnd;
	protected Vector<PathElement> _aPath;

	public MovementPath() {
		this._oEnd = new MapPoint();
		this._oStart = new MapPoint();
		this._aPath = new Vector<PathElement>();
	}

	public MapPoint getStart() {
		return this._oStart;
	}

	public void setStart(MapPoint mp) {
		this._oStart = mp;
	}

	public MapPoint getEnd() {
		return this._oEnd;
	}

	public void setEnd(MapPoint mp) {
		this._oEnd = mp;
	}

	public Vector<PathElement> getPath() {
		return this._aPath;
	}

	public void setPath(Vector<PathElement> vector) {
		this._aPath = vector;
	}

	public int getLength() {
		return this._aPath.size();
	}

	/*
	public void fillFromCellIds(Vector<Integer> vector) {
		int vectorSize = vector.size();
		for(int i = 0; i < vectorSize; ++i)
			this._aPath.add(new PathElement(MapPoint.fromCellId(vector.get(i)), 0));
		for(int i = 0; i < vectorSize - 1; ++i)
			this._aPath.get(i).setOrientation(this._aPath.get(i).getStep().orientationTo(this._aPath.get(i + 1).getStep()));
		if(this._aPath.get(0) != null) {
			this._oStart = this._aPath.get(0).getStep();
			this._oEnd = this._aPath.get(this._aPath.size() - 1).getStep();
		}
	}
	*/

	public void addPoint(PathElement pe) {
		this._aPath.add(pe);
	}

	public PathElement getPointAtIndex(int i) {
		return this._aPath.get(i);
	}

	public void deletePoint(int i1, int i2) {
		if(i2 == 0)
			this._aPath.remove(i1);
		else
			for(int i = 0; i < i2; ++i)
				this._aPath.remove(i1 + i);
	}

	public String toString() {
		String s = "\ndepart : [" + this._oStart.getX() + ", " + this._oStart.getY() + "]";
		s +=  "\narrivée : [" + this._oEnd.getX() + ", " + this._oEnd.getY() + "]\nchemin :";
		int vectorSize = this._aPath.size();
		PathElement pe;
		for(int i = 0; i < vectorSize; ++i) {
			pe = this._aPath.get(i);
			s += "[" + pe.getStep().getX() + ", " + pe.getStep().getY() + ", " + pe.getOrientation() + "]  ";
		}
		return s;
	}
	
	public void compress() {
		int vectorSize = this._aPath.size();
		if(vectorSize > 0) {
			for(int i = vectorSize - 1; i > 0; --i)
				if(this._aPath.get(i).getOrientation() == this._aPath.get(i - 1).getOrientation())
					deletePoint(i, 1);
		}
	}
	
	public void fill() {
		if(this._aPath.size() > 0) {
			PathElement pe = new PathElement(null, 0);
			PathElement pe2;
			pe.setOrientation(0);
			pe.setStep(this._oEnd);
			this._aPath.add(pe);
			int vectorSize = this._aPath.size();
			for(int i = 0; i < vectorSize - 1; ++i) {
				if(i > MAX_PATH_LENGTH)
					throw new Error("Path too long. Maybe an orientation problem ?");
				pe = this._aPath.get(i);
				if((Math.abs(pe.getStep().getX() - this._aPath.get(i + 1).getStep().getX()) > 1) || (Math.abs(pe.getStep().getY() - this._aPath.get(i + 1).getStep().getY()) > 1)) {
					pe2 = new PathElement(null, 0);
					pe2.setOrientation(pe.getOrientation());
					switch(pe2.getOrientation()) {
						case RIGHT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() + 1, pe.getStep().getY() + 1)); break;
						case DOWN_RIGHT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() + 1, pe.getStep().getY())); break;
						case DOWN: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() + 1, pe.getStep().getY() - 1)); break;
						case DOWN_LEFT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX(), pe.getStep().getY() - 1)); break;
						case LEFT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() - 1, pe.getStep().getY() - 1)); break;
						case UP_LEFT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() - 1, pe.getStep().getY())); break;
						case UP: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX() - 1, pe.getStep().getY() + 1)); break;
						case UP_RIGHT: pe2.setStep(MapPoint.fromCoords(pe.getStep().getX(), pe.getStep().getY() + 1)); break;
					}
					this._aPath.add(i + 1, pe2);
				}
			}
			this._aPath.remove(_aPath.size() - 1);
		}
	}

	public Vector<Integer> getCells() {
		MapPoint mp;
		Vector<Integer> vi = new Vector<Integer>();
		int vectorSize = this._aPath.size();
		for(int i = 0; i < vectorSize; ++i) {
			mp = this._aPath.get(i).getStep();
			vi.add(mp.getCellId());
		}
		vi.add(this._oEnd.getCellId());
		return vi;
	}

	public void replaceEnd(MapPoint mp) {
		this._oEnd = mp;
	}
	
	public Vector<Integer> getServerMovement() {
		compress();
		int nb;
		Vector<Integer> result = new Vector<Integer>();
		PathElement pe = null;
		int mpLength = getPath().size();
		for(int i = 0; i < mpLength; ++i) {
			pe = getPath().get(i);
			nb = ((pe.getOrientation() & 7) << 12) | (pe.getStep().getCellId() & 4095);
			result.add(nb);
		}
		if(pe != null) {
			nb = ((pe.getOrientation() & 7) << 12) | (getEnd().getCellId() & 4095);
			result.add(nb);
		}
		return result;
	}
}