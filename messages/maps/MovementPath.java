package messages.maps;

import java.util.ArrayList;
import java.util.Vector;

public class MovementPath
{

	public static int MAX_PATH_LENGTH = 100;

	protected MapPoint _oStart;

	protected MapPoint _oEnd;

	protected ArrayList<PathElement> _aPath;

	public MovementPath()
	{
		super();
		this._oEnd = new MapPoint();
		this._oStart = new MapPoint();
		this._aPath = new ArrayList<PathElement>();
	}

	public MapPoint getstart()
	{
		return this._oStart;
	}

	public void setstart(MapPoint param1 )
	{
		this._oStart = param1;
	}

	public MapPoint getend() 
	{
		return this._oEnd;
	}

	public void setend(MapPoint param1 ) 
	{
		this._oEnd = param1;
	}

	public ArrayList<PathElement> getpath()
	{
		return this._aPath;
	}

	public void setpath(ArrayList<PathElement> param1)
	{
		this._aPath = param1;
	}

	public int getlength()
	{
		return this._aPath.size();
	}

	public void fillFromCellIds(Vector<Integer> param1) 
	{
		int _loc2_ = 0;
		while(_loc2_ < param1.size())
		{
			this._aPath.add(new PathElement(MapPoint.fromCellId(param1.get(_loc2_)),_loc2_));
			_loc2_++;
		}
		_loc2_ = 0;
		while(_loc2_ < param1.size()- 1)
		{
			this._aPath.get(_loc2_)._nOrientation = (this._aPath.get(_loc2_)).getstep().orientationTo(this._aPath.get(_loc2_+1).getstep());
			_loc2_++;
		}
		if(this._aPath.get(0)!=null)
		{
			this._oStart = this._aPath.get(0).getstep();
			this._oEnd = this._aPath.get(this._aPath.size()-1).getstep();
		}
	}

	public void addPoint(PathElement param1)
	{
		this._aPath.add(param1);
	}

	public PathElement getPointAtIndex(int param1)
	{
		return this._aPath.get(param1);
	}

	public void deletePoint(int param1, int param2)
	{
		param2=1;
		if(param2 == 0)
		{
			this._aPath.remove(param1);
		}
		else
		{
			for(int i=0;i<param2;i++)
				this._aPath.remove(param1+i);
		}
	}

	public String toString()
	{
		String _loc1_ = "\ndepart : [" + this._oStart.getx() + ", " + this._oStart.gety() + "]";
	_loc1_ = _loc1_ + ("\narrivée : [" + this._oEnd.getx() + ", " + this._oEnd.gety() + "]\nchemin :");
	int _loc2_ = 0;
	while(_loc2_ < this._aPath.size())
	{
		_loc1_ = _loc1_ + ("[" + this._aPath.get(_loc2_).getstep().getx() + ", " + this._aPath.get(_loc2_).getstep().gety() + ", " + this._aPath.get(_loc2_)._nOrientation + "]  ");
		_loc2_++;
	}
	return _loc1_;
	}

	public void compress() 
	{
		int _loc1_ = 0;
		if(this._aPath.size() > 0)
		{
			_loc1_ = this._aPath.size() - 1;
			while(_loc1_ > 0)
			{
				if(this._aPath.get(_loc1_)._nOrientation == this._aPath.get(_loc1_-1)._nOrientation)
				{
					this.deletePoint(_loc1_,0);
					_loc1_--;
				}
				else
				{
					_loc1_--;
				}
			}
		}
	}

	public void fill() 
	{
		int  _loc1_ = 0;
		PathElement _loc2_ = null;
		PathElement _loc3_ = null;
		if(this._aPath.size() > 0)
		{
			_loc1_ = 0;
			_loc2_ = new PathElement();
			_loc2_._nOrientation = 0;
			_loc2_._oStep = this._oEnd;
			this._aPath.add(_loc2_);
			while(_loc1_ < this._aPath.size() - 1)
			{
				if(Math.abs(this._aPath.get(_loc1_).getstep().getx() - this._aPath.get(_loc1_+1).getstep().getx()) > 1 || Math.abs(this._aPath.get(_loc1_).getstep().gety() - this._aPath.get(_loc1_+1).getstep().gety()) > 1)
				{
					_loc3_ = new PathElement();
					_loc3_._nOrientation = this._aPath.get(_loc1_)._nOrientation;
					switch(_loc3_._nOrientation)
					{
					case DirectionsEnum.RIGHT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx()+ 1,this._aPath.get(_loc1_).getstep().gety() + 1);
						break;
					case DirectionsEnum.DOWN_RIGHT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx() + 1,this._aPath.get(_loc1_).getstep().gety());
						break;
					case DirectionsEnum.DOWN:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx() + 1,this._aPath.get(_loc1_).getstep().gety() - 1);
						break;
					case DirectionsEnum.DOWN_LEFT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx(),this._aPath.get(_loc1_).getstep().gety() - 1);
						break;
					case DirectionsEnum.LEFT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx() - 1,this._aPath.get(_loc1_).getstep().gety() - 1);
						break;
					case DirectionsEnum.UP_LEFT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx()- 1,this._aPath.get(_loc1_).getstep().gety());
						break;
					case DirectionsEnum.UP:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx() - 1,this._aPath.get(_loc1_).getstep().gety() + 1);
						break;
					case DirectionsEnum.UP_RIGHT:
						_loc3_._oStep = MapPoint.fromCoords(this._aPath.get(_loc1_).getstep().getx(),this._aPath.get(_loc1_).getstep().gety() + 1);
						break;
					}
					this._aPath.add(_loc1_+1, _loc3_);
					_loc1_++;
				}
				else
				{
					_loc1_++;
				}
				if(_loc1_ > MAX_PATH_LENGTH)
				{
					throw new Error("Path too long. Maybe an orientation problem?");
				}
			}
		}
		this._aPath.remove(_aPath.size()-1);
	}

	public Vector<Integer> getCells()
	{
		MapPoint _loc3_  = null;
		Vector<Integer> _loc1_ = new Vector<Integer>();
		int _loc2_ = 0;
		while(_loc2_ < this._aPath.size())
		{
			_loc3_ = this._aPath.get(_loc2_).getstep();
			_loc1_.add(_loc3_.getcellId());
			_loc2_++;
		}
		_loc1_.addElement(this._oEnd.getcellId());
		return _loc1_;
	}

	public void replaceEnd(MapPoint param1 )
	{
		this._oEnd = param1;
	}
}

