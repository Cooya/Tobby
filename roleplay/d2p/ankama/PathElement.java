package roleplay.d2p.ankama;


public class PathElement {
	private MapPoint _oStep;
	private int _nOrientation;

	public PathElement(MapPoint mp, int i) {
		if(mp == null)
			this._oStep = new MapPoint();
		else
			this._oStep = mp;
		this._nOrientation = i;
	}

	public int getOrientation() {
		return this._nOrientation;
	}

	public void setOrientation(int i) {
		this._nOrientation = i;
	}

	public MapPoint getStep() {
		return this._oStep;
	}

	public void setStep(MapPoint mp) {
		this._oStep = mp;
	}

	public int getCellId() {
		return this._oStep.getCellId();
	}

	public String toString() {
		return "[PathElement(cellId:" + this.getCellId() + ", x:" + this._oStep.getX() + ", y:" + this._oStep.getY() + ", orientation:" + this._nOrientation + ")]";
	}
}
