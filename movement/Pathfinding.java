package movement;

import java.util.Hashtable;
import java.util.Vector;

public class Pathfinding {
        private static int _minX;
        private static int _maxX;
        private static int _minY;
        private static int _maxY;
        private static Pathfinding _self;
        private Hashtable<Integer, Hashtable<Integer, CellInfo>> _mapStatus;
        private Vector<Object> _openList;
        private MovementPath _movPath;
        private int _nHVCost = 10;
        private int _nDCost = 15;
        private int _nHeuristicCost = 10;
        private boolean _bAllowDiagCornering = false;
        private boolean _bAllowTroughEntity;
        private boolean _bIsFighting;
        //private var _callBackFunction:Function;
        //private Vector<> _argsFunction;
        private boolean _enterFrameIsActive = false;
        private DataMapProvider _map;
        private MapPoint _start;
        private MapPoint _end;
        private boolean _allowDiag;
        private int _endX;
        private int _endY;
        private MapPoint _endPoint;
        private MapPoint _startPoint;
        private int _startX;
        private int _startY;
        private MapPoint _endPointAux;
        private int _endAuxX;
        private int _endAuxY;
        private int _distanceToEnd;
        private int _nowY;
        private int _nowX;
        private int _currentTime;
        private int _maxTime = 30;
        private int _previousCellId;
        
        public static void init(int minX, int maxX, int minY, int maxY) {
            _minX = minX;
            _maxX = maxX;
            _minY = minY;
            _maxY = maxY;
        }
        
        public static MovementPath findPath(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b1, boolean b2, boolean b3) {
        	return (new (Pathfinding)().processFindPath(dmp, src, dest, b1, b2, b3);
        }
        
        public MovementPath processFindPath(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b1, boolean b2, boolean b3) {
        	this._movPath = new MovementPath();
            this._movPath.setStart(src);
            this._movPath.setEnd(dest);
            this._bAllowDiagCornering = b1;
            this._bAllowTroughEntity = b2;
            this._bIsFighting = b3;
            if(dmp.height == 0 || dmp.width == 0 || src == null)
            	return this._movPath;
            findPathInternal(dmp, src, dest, b1);
            return null;
        }
        
        private boolean isOpened(int i1, int i2) {
        	return this._mapStatus.get(i1).get(i2).opened;
        }
        
        private boolean isClosed(int i1, int i2) {
        	CellInfo ci = this._mapStatus.get(i1).get(i2);
        	if(ci == null || !ci.closed)
        		return false;
        	return ci.closed;
        }
        
        private int nearerSquare() {
        	double d;
        	double limit = 9999999;
        	int nb;
        	for(int i = 0; i < this._openList.size(); ++i) {
        		// to do
        		d = (this._mapStatus[this._openList[_local_4][0]][this._openList[_local_4][1]].heuristic + this._mapStatus[this._openList[_local_4][0]][this._openList[_local_4][1]].movementCost);
                d = (this._mapStatus[this._openList[_local_4][0]][this._openList[_local_4][1]].heuristic + this._mapStatus[this._openList[_local_4][0]][this._openList[_local_4][1]].movementCost);
                //
                if(d <= limit) {
                	limit = d;
                	nb = i;
                }
        	}
        	return nb;
        }
        
        // to do
        private function closeSquare(_arg_1:int, _arg_2:int):void
        {
            var _local_3:uint = this._openList.length;
            var _local_4:int = -1;
            while (++_local_4 < _local_3)
            {
                if (this._openList[_local_4][0] == _arg_1)
                {
                    if (this._openList[_local_4][1] == _arg_2)
                    {
                        this._openList.splice(_local_4, 1);
                        break;
                    };
                };
            };
            var _local_5:CellInfo = this._mapStatus[_arg_1][_arg_2];
            _local_5.opened = false;
            _local_5.closed = true;
        }
        //
        
        
        
        
        
        private void findPathInternal(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b) {
        	this._map = dmp;
        	this._start = src;
        	this._end = dest;
        	this._allowDiag = b;
            this._startPoint = MapPoint.fromCoords(src.x, src.y);
            this._endPoint = MapPoint.fromCoords(dest.x, dest.y);
            this._startX = src.x;
            this._startY = src.y;
            this._endX = dest.x;
            this._endY = dest.y;
            this._endPointAux = this._startPoint;
            this._endAuxX = this._startX;
            this._endAuxY = this._startY;
            this._distanceToEnd = this._startPoint.distanceToCell(this._endPoint);
            this._mapStatus = new Hashtable<Integer, Hashtable<Integer, CellInfo>>();
            for(int i = _minY; i < _maxY; ++i) {
            	this._mapStatus.put(i, new Hashtable<Integer, CelldInfo>());
            	for(int j = _minX; i < _maxX; ++j)
            		this._mapStatus.get(i).put(j, new CellInfo(0, new Vector<Object>(), false, false));
            }
            this._openList = new Vector<Object>();
            openSquare(this._startY, this._startX, null, 0, null, false);
            initFindPath();
        }
        

/*
        private function openSquare(_arg_1:int, _arg_2:int, _arg_3:Array, _arg_4:uint, _arg_5:Number, _arg_6:Boolean):void
        {
            var _local_8:int;
            var _local_9:int;
            if (!_arg_6)
            {
                _local_8 = this._openList.length;
                _local_9 = -1;
                while (++_local_9 < _local_8)
                {
                    if ((((this._openList[_local_9][0] == _arg_1)) && ((this._openList[_local_9][1] == _arg_2))))
                    {
                        _arg_6 = true;
                        break;
                    };
                };
            };
            if (!_arg_6)
            {
                this._openList.push([_arg_1, _arg_2]);
                this._mapStatus[_arg_1][_arg_2] = new CellInfo(_arg_5, null, true, false);
            };
            var _local_7:CellInfo = this._mapStatus[_arg_1][_arg_2];
            _local_7.parent = _arg_3;
            _local_7.movementCost = _arg_4;
        }
        private function movementPathFromArray(_arg_1:Array):void
        {
            var _local_3:PathElement;
            var _local_2:uint;
            while (_local_2 < (_arg_1.length - 1))
            {
                _local_3 = new PathElement();
                _local_3.step.x = _arg_1[_local_2].x;
                _local_3.step.y = _arg_1[_local_2].y;
                _local_3.orientation = _arg_1[_local_2].orientationTo(_arg_1[(_local_2 + 1)]);
                this._movPath.addPoint(_local_3);
                _local_2++;
            };
            this._movPath.compress();
            this._movPath.fill();
        }
        private function initFindPath():void
        {
            this._currentTime = 0;
            if (this._callBackFunction == null)
            {
                this._maxTime = 2000000;
                this.pathFrame(null);
            }
            else
            {
                if (!this._enterFrameIsActive)
                {
                    this._enterFrameIsActive = true;
                    EnterFrameDispatcher.addEventListener(this.pathFrame, "pathFrame");
                };
                this._maxTime = 20;
            };
        }
        private function pathFrame(_arg_1:Event):void
        {
            var _local_2:int;
            var _local_3:int;
            var _local_4:int;
            var _local_5:int;
            var _local_6:Number;
            var _local_7:int;
            var _local_8:Boolean;
            var _local_9:Boolean;
            var _local_10:Boolean;
            var _local_11:Boolean;
            var _local_12:MapPoint;
            var _local_13:int;
            var _local_14:Number;
            if (this._currentTime == 0)
            {
                this._currentTime = getTimer();
            };
            if ((((this._openList.length > 0)) && (!(this.isClosed(this._endY, this._endX)))))
            {
                _local_2 = this.nearerSquare();
                this._nowY = this._openList[_local_2][0];
                this._nowX = this._openList[_local_2][1];
                this._previousCellId = MapPoint.fromCoords(this._nowX, this._nowY).cellId;
                this.closeSquare(this._nowY, this._nowX);
                _local_3 = (this._nowY - 1);
                while (_local_3 < (this._nowY + 2))
                {
                    _local_5 = (this._nowX - 1);
                    while (_local_5 < (this._nowX + 2))
                    {
                        if ((((((((((((_local_3 >= _minY)) && ((_local_3 < _maxY)))) && ((_local_5 >= _minX)))) && ((_local_5 < _maxX)))) && (!((((_local_3 == this._nowY)) && ((_local_5 == this._nowX))))))) && (((((this._allowDiag) || ((_local_3 == this._nowY)))) || ((((_local_5 == this._nowX)) && (((((((this._bAllowDiagCornering) || ((_local_3 == this._nowY)))) || ((_local_5 == this._nowX)))) || (((this._map.pointMov(this._nowX, _local_3, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId)) || (this._map.pointMov(_local_5, this._nowY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))))))))))))
                        {
                            if (!((((((!(this._map.pointMov(this._nowX, _local_3, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))) && (!(this._map.pointMov(_local_5, this._nowY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))))) && (!(this._bIsFighting)))) && (this._allowDiag)))
                            {
                                if (this._map.pointMov(_local_5, _local_3, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))
                                {
                                    if (!this.isClosed(_local_3, _local_5))
                                    {
                                        if ((((_local_5 == this._endX)) && ((_local_3 == this._endY))))
                                        {
                                            _local_6 = 1;
                                        }
                                        else
                                        {
                                            _local_6 = this._map.pointWeight(_local_5, _local_3, this._bAllowTroughEntity);
                                        };
                                        _local_7 = (this._mapStatus[this._nowY][this._nowX].movementCost + ((((((_local_3 == this._nowY)) || ((_local_5 == this._nowX)))) ? this._nHVCost : this._nDCost) * _local_6));
                                        if (this._bAllowTroughEntity)
                                        {
                                            _local_8 = ((_local_5 + _local_3) == (this._endX + this._endY));
                                            _local_9 = ((_local_5 + _local_3) == (this._startX + this._startY));
                                            _local_10 = ((_local_5 - _local_3) == (this._endX - this._endY));
                                            _local_11 = ((_local_5 - _local_3) == (this._startX - this._startY));
                                            _local_12 = MapPoint.fromCoords(_local_5, _local_3);
                                            if (((((!(_local_8)) && (!(_local_10)))) || (((!(_local_9)) && (!(_local_11))))))
                                            {
                                                _local_7 = (_local_7 + _local_12.distanceToCell(this._endPoint));
                                                _local_7 = (_local_7 + _local_12.distanceToCell(this._startPoint));
                                            };
                                            if ((((_local_5 == this._endX)) || ((_local_3 == this._endY))))
                                            {
                                                _local_7 = (_local_7 - 3);
                                            };
                                            if (((((((_local_8) || (_local_10))) || (((_local_5 + _local_3) == (this._nowX + this._nowY))))) || (((_local_5 - _local_3) == (this._nowX - this._nowY)))))
                                            {
                                                _local_7 = (_local_7 - 2);
                                            };
                                            if ((((_local_5 == this._startX)) || ((_local_3 == this._startY))))
                                            {
                                                _local_7 = (_local_7 - 3);
                                            };
                                            if (((_local_9) || (_local_11)))
                                            {
                                                _local_7 = (_local_7 - 2);
                                            };
                                            _local_13 = _local_12.distanceToCell(this._endPoint);
                                            if (_local_13 < this._distanceToEnd)
                                            {
                                                this._endPointAux = _local_12;
                                                this._endAuxX = _local_5;
                                                this._endAuxY = _local_3;
                                                this._distanceToEnd = _local_13;
                                            };
                                        };
                                        if (this.isOpened(_local_3, _local_5))
                                        {
                                            if (_local_7 < this._mapStatus[_local_3][_local_5].movementCost)
                                            {
                                                this.openSquare(_local_3, _local_5, [this._nowY, this._nowX], _local_7, undefined, true);
                                            };
                                        }
                                        else
                                        {
                                            _local_14 = (this._nHeuristicCost * Math.sqrt((((this._endY - _local_3) * (this._endY - _local_3)) + ((this._endX - _local_5) * (this._endX - _local_5)))));
                                            this.openSquare(_local_3, _local_5, [this._nowY, this._nowX], _local_7, _local_14, false);
                                        };
                                    };
                                };
                            };
                        };
                        _local_5++;
                    };
                    _local_3++;
                };
                _local_4 = getTimer();
                if ((_local_4 - this._currentTime) < this._maxTime)
                {
                    this.pathFrame(null);
                }
                else
                {
                    this._currentTime = 0;
                };
            }
            else
            {
                this.endPathFrame();
            };
        }
        private function endPathFrame():void
        {
            var _local_2:Array;
            var _local_3:int;
            var _local_4:int;
            var _local_5:MapPoint;
            var _local_6:Array;
            var _local_7:uint;
            var _local_8:int;
            var _local_9:int;
            var _local_10:int;
            var _local_11:int;
            var _local_12:int;
            var _local_13:int;
            this._enterFrameIsActive = false;
            EnterFrameDispatcher.removeEventListener(this.pathFrame);
            var _local_1:Boolean = this.isClosed(this._endY, this._endX);
            if (!_local_1)
            {
                this._endY = this._endAuxY;
                this._endX = this._endAuxX;
                this._endPoint = this._endPointAux;
                _local_1 = true;
                this._movPath.replaceEnd(this._endPoint);
            };
            this._previousCellId = -1;
            if (_local_1)
            {
                _local_2 = new Array();
                this._nowY = this._endY;
                this._nowX = this._endX;
                while (((!((this._nowY == this._startY))) || (!((this._nowX == this._startX)))))
                {
                    _local_2.push(MapPoint.fromCoords(this._nowX, this._nowY));
                    _local_3 = this._mapStatus[this._nowY][this._nowX].parent[0];
                    _local_4 = this._mapStatus[this._nowY][this._nowX].parent[1];
                    this._nowY = _local_3;
                    this._nowX = _local_4;
                };
                _local_2.push(this._startPoint);
                if (this._allowDiag)
                {
                    _local_6 = new Array();
                    _local_7 = 0;
                    while (_local_7 < _local_2.length)
                    {
                        _local_6.push(_local_2[_local_7]);
                        this._previousCellId = _local_2[_local_7].cellId;
                        if (((((((_local_2[(_local_7 + 2)]) && ((MapPoint(_local_2[_local_7]).distanceToCell(_local_2[(_local_7 + 2)]) == 1)))) && (!(this._map.isChangeZone(_local_2[_local_7].cellId, _local_2[(_local_7 + 1)].cellId))))) && (!(this._map.isChangeZone(_local_2[(_local_7 + 1)].cellId, _local_2[(_local_7 + 2)].cellId)))))
                        {
                            _local_7++;
                        }
                        else
                        {
                            if (((_local_2[(_local_7 + 3)]) && ((MapPoint(_local_2[_local_7]).distanceToCell(_local_2[(_local_7 + 3)]) == 2))))
                            {
                                _local_8 = _local_2[_local_7].x;
                                _local_9 = _local_2[_local_7].y;
                                _local_10 = _local_2[(_local_7 + 3)].x;
                                _local_11 = _local_2[(_local_7 + 3)].y;
                                _local_12 = (_local_8 + Math.round(((_local_10 - _local_8) / 2)));
                                _local_13 = (_local_9 + Math.round(((_local_11 - _local_9) / 2)));
                                if (((this._map.pointMov(_local_12, _local_13, true, this._previousCellId, this._endPoint.cellId)) && ((this._map.pointWeight(_local_12, _local_13) < 2))))
                                {
                                    _local_5 = MapPoint.fromCoords(_local_12, _local_13);
                                    _local_6.push(_local_5);
                                    this._previousCellId = _local_5.cellId;
                                    _local_7 = (++_local_7 + 1);
                                };
                            }
                            else
                            {
                                if (((_local_2[(_local_7 + 2)]) && ((MapPoint(_local_2[_local_7]).distanceToCell(_local_2[(_local_7 + 2)]) == 2))))
                                {
                                    _local_8 = _local_2[_local_7].x;
                                    _local_9 = _local_2[_local_7].y;
                                    _local_10 = _local_2[(_local_7 + 2)].x;
                                    _local_11 = _local_2[(_local_7 + 2)].y;
                                    _local_12 = _local_2[(_local_7 + 1)].x;
                                    _local_13 = _local_2[(_local_7 + 1)].y;
                                    if (((((((((_local_8 + _local_9) == (_local_10 + _local_11))) && (!(((_local_8 - _local_9) == (_local_12 - _local_13)))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(_local_8, _local_9).cellId, MapPoint.fromCoords(_local_12, _local_13).cellId))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(_local_12, _local_13).cellId, MapPoint.fromCoords(_local_10, _local_11).cellId)))))
                                    {
                                        _local_7++;
                                    }
                                    else
                                    {
                                        if (((((((((_local_8 - _local_9) == (_local_10 - _local_11))) && (!(((_local_8 - _local_9) == (_local_12 - _local_13)))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(_local_8, _local_9).cellId, MapPoint.fromCoords(_local_12, _local_13).cellId))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(_local_12, _local_13).cellId, MapPoint.fromCoords(_local_10, _local_11).cellId)))))
                                        {
                                            _local_7++;
                                        }
                                        else
                                        {
                                            if ((((((((_local_8 == _local_10)) && (!((_local_8 == _local_12))))) && ((this._map.pointWeight(_local_8, _local_13) < 2)))) && (this._map.pointMov(_local_8, _local_13, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))))
                                            {
                                                _local_5 = MapPoint.fromCoords(_local_8, _local_13);
                                                _local_6.push(_local_5);
                                                this._previousCellId = _local_5.cellId;
                                                _local_7++;
                                            }
                                            else
                                            {
                                                if ((((((((_local_9 == _local_11)) && (!((_local_9 == _local_13))))) && ((this._map.pointWeight(_local_12, _local_9) < 2)))) && (this._map.pointMov(_local_12, _local_9, this._bAllowTroughEntity, this._previousCellId, this._endPoint.cellId))))
                                                {
                                                    _local_5 = MapPoint.fromCoords(_local_12, _local_9);
                                                    _local_6.push(_local_5);
                                                    this._previousCellId = _local_5.cellId;
                                                    _local_7++;
                                                };
                                            };
                                        };
                                    };
                                };
                            };
                        };
                        _local_7++;
                    };
                    _local_2 = _local_6;
                };
                if (_local_2.length == 1)
                {
                    _local_2 = new Array();
                };
                _local_2.reverse();
                this.movementPathFromArray(_local_2);
            };
            if (this._callBackFunction != null)
            {
                if (this._argsFunction)
                {
                    this._callBackFunction(this._movPath, this._argsFunction);
                }
                else
                {
                    this._callBackFunction(this._movPath);
                };
            };
        }
        private function findPathInternal(_arg_1:IDataMapProvider, _arg_2:MapPoint, _arg_3:MapPoint, _arg_4:Boolean):void
        {
            var _local_6:uint;
            this._map = _arg_1;
            this._start = _arg_2;
            this._end = _arg_3;
            this._allowDiag = _arg_4;
            this._endPoint = MapPoint.fromCoords(_arg_3.x, _arg_3.y);
            this._startPoint = MapPoint.fromCoords(_arg_2.x, _arg_2.y);
            this._endX = _arg_3.x;
            this._endY = _arg_3.y;
            this._startX = _arg_2.x;
            this._startY = _arg_2.y;
            this._endPointAux = this._startPoint;
            this._endAuxX = this._startX;
            this._endAuxY = this._startY;
            this._distanceToEnd = this._startPoint.distanceToCell(this._endPoint);
            this._mapStatus = new Array();
            var _local_5:int = _minY;
            while (_local_5 < _maxY)
            {
                this._mapStatus[_local_5] = new Array();
                _local_6 = _minX;
                while (_local_6 <= _maxX)
                {
                    this._mapStatus[_local_5][_local_6] = new CellInfo(0, new Array(), false, false);
                    _local_6++;
                };
                _local_5++;
            };
            this._openList = new Array();
            this.openSquare(this._startY, this._startX, undefined, 0, undefined, false);
            this.initFindPath();
        }
        private function tracePath(_arg_1:Array):void
        {
            var _local_3:MapPoint;
            var _local_2:String = new String("");
            var _local_4:uint;
            while (_local_4 < _arg_1.length)
            {
                _local_3 = (_arg_1[_local_4] as MapPoint);
                _local_2 = _local_2.concat((" " + _local_3.cellId));
                _local_4++;
            };
        }
        private function nearObstacle(_arg_1:int, _arg_2:int, _arg_3:IDataMapProvider):int
        {
            var _local_7:int;
            var _local_4:int = 2;
            var _local_5:int = 42;
            var _local_6:int = -(_local_4);
            while (_local_6 < _local_4)
            {
                _local_7 = -(_local_4);
                while (_local_7 < _local_4)
                {
                    if (!_arg_3.pointMov((_arg_1 + _local_6), (_arg_2 + _local_7), true, this._previousCellId, this._endPoint.cellId))
                    {
                        _local_5 = Math.min(_local_5, MapPoint(MapPoint.fromCoords(_arg_1, _arg_2)).distanceToCell(MapPoint.fromCoords((_arg_1 + _local_6), (_arg_2 + _local_7))));
                    };
                    _local_7++;
                };
                _local_6++;
            };
            return (_local_5);
        }
*/
}
