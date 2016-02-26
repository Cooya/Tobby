package gamedata.d2p.ankama;

import main.FatalError;
import utilities.ByteArray;

public class BasicElement {
    public static final int GRAPHICAL = 2;
    public static final int SOUND = 33;
	private Cell _cell;
	
	public BasicElement(Cell cell) {
		this._cell = cell;
	}
	
	public static BasicElement getElementFromType(int i, Cell cell) {
		if(i == GRAPHICAL)
			return new GraphicalElement(cell);
		else if(i == SOUND)
			return new SoundElement(cell);
		else
			throw new FatalError("Unknown element.");
	}
	
	public Cell getCell() {
		return this._cell;
	}
	
	public int getElementType() {
		return -1;
	}

	public void fromRaw(ByteArray raw, int i) {
		throw new FatalError("This method must be overrided");
	}
}