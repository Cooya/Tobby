package messages.maps;

public class PathElement
{

	public MapPoint _oStep ;

	public int _nOrientation;

	public  PathElement(MapPoint param1, int param2)
	{
		super();
		param1=null;
		param2=0;
		if(param1==null)
		{
			this._oStep = new MapPoint();
		}
		else
		{
			this._oStep = param1;
		}
		this._nOrientation = param2;
	}

	public PathElement() {
		this._oStep = new MapPoint();
		this._nOrientation = 0;
	}

	public int getorientation()
	{
		return this._nOrientation;
	}

	public void setorientation(int param1) 
	{
		this._nOrientation = param1;
	}

	public MapPoint getstep()
	{
		return this._oStep;
	}

	public void setstep(MapPoint param1) 
	{
		this._oStep = param1;
	}

	public int getcellId() 
	{
		return this._oStep.getcellId();
	}

	public String toString()
	{
		return "[PathElement(cellId:" + this.getcellId() + ", x:" + this._oStep.getx() + ", y:" + this._oStep.gety() + ", orientation:" + this._nOrientation + ")]";
	}
}
