package useless;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import movement.Cell;
import movement.CellData;

public class DataMapProvider {
	private static final int TOLERANCE_ELEVATION = 11;
	private static DataMapProvider _self  ;
	private static Class _playerClass;
	public boolean isInFight;
	public Vector<Integer> obstaclesCells;
	private Hashtable<Integer,Boolean> _updatedCell;
	private Hashtable<Integer,Integer> _specialEffects;
	
	public DataMapProvider() {
		this.obstaclesCells = new Vector<Integer>();
		this._updatedCell = new Hashtable<Integer, Boolean>();
		this._specialEffects = new Hashtable<Integer, Integer>();
	}

	public static DataMapProvider getInstance() {
		if(_self == null)
			throw new Error("Init function wasn\'t call");
		return _self;
	}

	public static void init(Class c) {
		_playerClass = c;
		if(_self == null)
			_self = new DataMapProvider();
	}

	public boolean pointLos(int param1, int param2, boolean param3)
	{
		ArrayList<E> _loc6_ = null;
		var _loc7_:IObstacle = null;
		int _loc4_ = MapPoint.fromCoords(param1,param2).cellId;
		boolean _loc5_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[_loc4_]).los;
		if(this._updatedCell[_loc4_] != null)
		{
			_loc5_ = this._updatedCell[_loc4_];
		}
		if(!param3)
		{
			_loc6_ = EntitiesManager.getInstance().getEntitiesOnCell(_loc4_,IObstacle);
			if(_loc6_.size())
			{
				for each(_loc7_ in _loc6_)
				{
					if(!IObstacle(_loc7_).canSeeThrough())
					{
						return false;
					}
				}
			}
		}
		return _loc5_;
	}

	public boolean farmCell(int param1, int param2) 
	{
		int _loc3_ = MapPoint.fromCoords(param1,param2).getCellId();
		return CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[_loc3_]).farmCell;
	}

	public boolean isChangeZone(int param1, int param2) 
	{
		CellData _loc3_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[param1]);
		CellData _loc4_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[param2]);
		int _loc5_ = Math.abs(Math.abs(_loc3_.getfloor()) - Math.abs(_loc4_.getfloor()));
		if(_loc3_.moveZone != _loc4_.moveZone && _loc5_ == 0)
		{
			return true;
		}
		return false;
	}

	public boolean pointMov(int param1, int param2, boolean param3 ,int param4 , int param5 ) 
	{
		boolean _loc6_ = false;
		int _loc7_ = 0;
		CellData _loc8_ = null;
		boolean _loc9_ = false;
		CellData _loc10_ = null;
		int _loc11_ = 0;
		ArrayList _loc12_ = null;
		var _loc13_:IObstacle = null;
		if(MapPoint.isInMap(param1,param2))
		{
			_loc6_ = MapDisplayManager.getInstance().getDataMapContainer().dataMap.isUsingNewMovementSystem;
			_loc7_ = MapPoint.fromCoords(param1,param2).getCellId();
			_loc8_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[_loc7_]);
			_loc9_ = _loc8_.getmov() && (!this.isInFight || !_loc8_.getnonWalkableDuringFight());
			if(this._updatedCell.get(_loc7_)!= null)
			{
				_loc9_ =  this._updatedCell.get(_loc7_);
			}
			if(_loc9_ && _loc6_ && param4 != -1 && param4 != _loc7_)
			{
				_loc10_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[param4]);
				_loc11_ = Math.abs(Math.abs(_loc8_.getfloor()) - Math.abs(_loc10_.getfloor()));
				if(_loc10_.moveZone != _loc8_.moveZone && _loc11_ > 0 || _loc10_.moveZone == _loc8_.moveZone && _loc8_.moveZone == 0 && _loc11_ > TOLERANCE_ELEVATION)
				{
					_loc9_ = false;
				}
			}
			if(!param3)
			{
				_loc12_ = EntitiesManager.getInstance().getEntitiesOnCell(_loc7_,IObstacle);
				if(_loc12_.size()!=0)
				{
					for each(_loc13_ in _loc12_)
					{
						if(!(param5 == _loc7_ && _loc13_.canWalkTo()))
						{
							if(!_loc13_.canWalkThrough())
							{
								return false;
							}
						}
					}
				}
				else if(this.obstaclesCells.indexOf(_loc7_) != -1 && _loc7_ != param5)
				{
					return false;
				}
			}
		}
		else
		{
			_loc9_ = false;
		}
		return _loc9_;
	}

	public boolean pointCanStop(int param1, int param2,boolean param3 ) 
	{
		int _loc4_ = MapPoint.fromCoords(param1,param2).getCellId();
		CellData _loc5_ = CellData(MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[_loc4_]);
		return this.pointMov(param1,param2,param3) && (this.isInFight || !_loc5_.getNonWalkableDuringRP());
	}

	public int pointWeight(int param1,int param2,boolean param3)
	{
		var _loc6_:IEntity = null;
		int _loc4_ = 1;
		int _loc5_ = this.getCellSpeed(MapPoint.fromCoords(param1,param2).getCellId());
		if(param3) {
			if(_loc5_ >= 0)
			{
				_loc4_ = _loc4_ + (5 - _loc5_);
			}
			else
			{
				_loc4_ = _loc4_ + (11 + Math.abs(_loc5_));
			}
			_loc6_ = EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1,param2),_playerClass);
			if(_loc6_ && !_loc6_["allowMovementThrough"])
			{
				_loc4_ = 20;
			}
		}
		else {
			if(EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1,param2),_playerClass) != null)
			{
				_loc4_ = _loc4_ + 0.3;
			}
			if(EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1 + 1,param2),_playerClass) != null)
			{
				_loc4_ = _loc4_ + 0.3;
			}
			if(EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1,param2 + 1),_playerClass) != null)
			{
				_loc4_ = _loc4_ + 0.3;
			}
			if(EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1 - 1,param2),_playerClass) != null)
			{
				_loc4_ = _loc4_ + 0.3;
			}
			if(EntitiesManager.getInstance().getEntityOnCell(Cell.cellIdByXY(param1,param2 - 1),_playerClass) != null)
			{
				_loc4_ = _loc4_ + 0.3;
			}
			if((this.pointSpecialEffects(param1,param2) & 2) == 2)
			{
				_loc4_ = _loc4_ + 0.2;
			}
		}
		return _loc4_;
	}

	public int getCellSpeed(int param1) {
		return (MapDisplayManager.getInstance().getDataMapContainer().dataMap.cells[param1] as CellData).speed;
	}

	public int pointSpecialEffects(int param1,int param2) {
		int _loc3_ = MapPoint.fromCoords(param1,param2).getCellId();
		if(this._specialEffects.get(_loc3_) != null)
		{
			return this._specialEffects.get(_loc3_);
		}
		return 0;
	}

	public int getWidth() {
		return MAP_HEIGHT + MAP_WIDTH - 2;
	}

	public int getHeight() {
		return MAP_HEIGHT + MAP_WIDTH - 1;
	}

	public boolean hasEntity(int param1,int param2) {
		var _loc4_:IObstacle = null;
		var _loc3_:Array = EntitiesManager.getInstance().getEntitiesOnCell(MapPoint.fromCoords(param1,param2).cellId,IObstacle);
		if(_loc3_.length)
		{
			for each(_loc4_ in _loc3_)
			{
				if(!IObstacle(_loc4_).canWalkTo())
				{
					return true;
				}
			}
		}
		return false;
	}

	public void updateCellMovLov(int param1, boolean param2) {
		this._updatedCell.put(param1, param2);
	}

	public void resetUpdatedCell() {
		this._updatedCell = new Hashtable<Integer, Boolean>();
	}

	public void setSpecialEffects(int param1, int param2) {
		this._specialEffects.put(param1, param2);
	}

	public void resetSpecialEffects() {
		this._specialEffects = new Hashtable<Integer, Integer>();
	}
}