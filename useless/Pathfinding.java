package useless;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

public class Pathfinding {
    private static int _minX;
    private static int _maxX;
    private static int _minY;
    private static int _maxY;
    //private static Pathfinding _self;
    private Hashtable<Integer, Hashtable<Integer, CellInfo>> _mapStatus;
    private Vector<int[]> _openList;
    private MovementPath _movPath;
    private int _nHVCost = 10;
    private int _nDCost = 15;
    private int _nHeuristicCost = 10;
    private boolean _bAllowDiagCornering = false;
    private boolean _bAllowTroughEntity;
    private boolean _bIsFighting;
    //private var _callBackFunction:Function;
    //private Vector<> _argsFunction;
    //private boolean _enterFrameIsActive = false;
    private DataMapProvider _map;
    //private MapPoint _start;
    //private MapPoint _end;
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
    //private int _currentTime;
    //private int _maxTime = 30;
    private int _previousCellId;
    
    public static void init(int minX, int maxX, int minY, int maxY) {
        _minX = minX;
        _maxX = maxX;
        _minY = minY;
        _maxY = maxY;
    }
    
    public static MovementPath findPath(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b1, boolean b2, boolean b3) {
    	return new Pathfinding().processFindPath(dmp, src, dest, b1, b2, b3);
    }
    
    public MovementPath processFindPath(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b1, boolean b2, boolean b3) {
    	this._movPath = new MovementPath();
        this._movPath.setStart(src);
        this._movPath.setEnd(dest);
        this._bAllowDiagCornering = b1;
        this._bAllowTroughEntity = b2;
        this._bIsFighting = b3;
        if(dmp.getHeight() == 0 || dmp.getWidth() == 0 || src == null)
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
    	int nb = 0;
    	for(int i = 0; i < this._openList.size(); ++i) {
            d = this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).heuristic + this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).movementCost;
            if(d <= limit) {
            	limit = d;
            	nb = i;
            }
    	}
    	return nb;
    }
    
    private void closeSquare(int i1, int i2) {
    	int openListSize = this._openList.size();
    	for(int i = 0; i < openListSize; ++i)
    		if(this._openList.get(i)[0] == i1)
    			if(this._openList.get(i)[1] == i2) {
    				this._openList.remove(i);
    				break;
    			}
    	CellInfo ci = this._mapStatus.get(i1).get(i2);
    	ci.opened = false;
    	ci.closed = true;
    }
    
    private void openSquare(int i1, int i2, Vector<Object> vector, int i3, double d, boolean b) {
    	if(!b) {
    		int openListSize = this._openList.size();
    		int[] array;
    		for(int i = 0; i < openListSize; ++i) {
    			array = this._openList.get(i);
    			if(array[0] == i1 && array[1] == i2) {
    				b = true;
    				break;
    			}
    		}
    	}
    	if(!b) {
    		int[] array = {i1, i2};
    		this._openList.add(array);
    		if(!this._mapStatus.containsKey(i1))
    			this._mapStatus.put(i1, new Hashtable<Integer, CellInfo>());
    		this._mapStatus.get(i1).put(i2, new CellInfo(d, null, true, false));
    	}
    	CellInfo ci = this._mapStatus.get(i1).get(i2);
    	ci.parent = vector;
    	ci.movementCost = i3;
    }
    
    private void movementPathFromArray(Vector<MapPoint> vector) {
    	int vectorSize = vector.size();
    	PathElement pe;
    	for(int i = 0; i < vectorSize; ++i) {
    		pe = new PathElement(null, 0);
    		pe.getStep().setX(vector.get(i).getX());
    		pe.getStep().setY(vector.get(i).getY());
    		pe.setOrientation(vector.get(i).orientationTo(vector.get(i + 1)));
    		this._movPath.addPoint(pe);
    	}
    	this._movPath.compress();
    	this._movPath.fill();
    }
    
    private void initFindPath() {
    	pathFrame();
    }
    
    private void pathFrame() {
    	if(this._openList.size() > 0 && !this.isClosed(this._endY, this._endX)) {
    		int i1 = nearerSquare();
    		this._nowY = this._openList.get(i1)[0];
    		this._nowX = this._openList.get(i1)[1];
    		this._previousCellId = MapPoint.fromCoords(this._nowX, this._nowY).getCellId();
    		closeSquare(this._nowY, this._nowX);
    		for(int i = this._nowY - 1; i < this._nowY + 2; ++i) {
    			for(int j = this._nowX - 1; j < this._nowX + 2; ++j) {
    				if(i >= _minY && i < _maxY && j >= _minX && j < _maxX && !(i == this._nowY) && j == this._nowX && this._allowDiag || i == this._nowY || j == this._nowX && this._bAllowDiagCornering || i == this._nowY || j == this._nowX || this._map.pointMov(this._nowX, i, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId()))
    					if(!(!this._map.pointMov(this._nowX, i, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId()) && !this._map.pointMov(j, this._nowY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId()) && !this._bIsFighting && this._allowDiag))
    						if(this._map.pointMov(j, i, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId()))
    							if(!isClosed(i, j)) {
    								double d;
    								if(j == this._endX && i == this._endY)
    									d = 1;
    								else
    									d = this._map.pointWeight(j, i, this._bAllowTroughEntity);
    								int i2 = (int) (this._mapStatus.get(this._nowY).get(this._nowX).movementCost + ((i == this._nowY || j == this._nowX) ? this._nHVCost : this._nDCost) * d);
    								if(this._bAllowTroughEntity) {
    									boolean b1 = (j + i) == (this._endX + this._endY);
    									boolean b2 = (j + i) == (this._startX + this._startY);
    									boolean b3 = (j - i) == (this._endX - this._endY);
    									boolean b4 = (j - i) == (this._startX - this._startY);
    									MapPoint mp = MapPoint.fromCoords(j, i);
    									if((!b1 && !b3) || (!b2 &&  !b4)) {
    										i2 += mp.distanceToCell(this._endPoint);
    										i2 += mp.distanceToCell(this._startPoint);
    									}
    									if(j == this._endX || i == this._endY)
    										i2 -= 3;
    									if((b1 || b3) || ((j + i) == (this._nowX + this._nowY)) || ((j - i) == (this._nowX - this._nowY)))
    										i2 -= 2;
    									if(j == this._startX || i == this._startY)
    										i2 -= 3;
    									if(b2 && b4)
    										i2 -= 2;
    									int i3 = mp.distanceToCell(this._endPoint);
    									if(i3 < this._distanceToEnd) {
    										this._endPointAux = mp;
    										this._endAuxX = j;
    										this._endAuxY = i;
    										this._distanceToEnd = i3;
    									}
    								}
    								if(isOpened(i, j)) {
    									if(i2 < this._mapStatus.get(i).get(j).movementCost) {
    										Vector<Object> vector = new Vector<Object>();
    										vector.add(this._nowY);
    										vector.add(this._nowX);
    										this.openSquare(i, j, vector, i2, 0, true);
    									}
    								}
    								else {
    									double d2 = this._nHeuristicCost * Math.sqrt((this._endY - i) * (this._endY - i) + (this._endX - j) * (this._endX - j));
    									Vector<Object> vector = new Vector<Object>();
										vector.add(this._nowY);
										vector.add(this._nowX);
    									openSquare(i, j, vector, i2, d2, false);
    								}
    							}	
    			}
    		}
    	}
    	else
    		endPathFrame();
    }
    
    private void endPathFrame() {
    	//this._enterFrameIsActive = false;
    	boolean b = isClosed(this._endY, this._endX);
    	if(!b) {
    		this._endY = this._endAuxY;
    		this._endX = this._endAuxX;
    		this._endPoint = this._endPointAux;
    		b = true;
    		this._movPath.replaceEnd(this._endPoint);
    	}
    	this._previousCellId = -1;
    	if(b) {
    		Vector<MapPoint> vector1 = new Vector<MapPoint>();
            this._nowY = this._endY;
            this._nowX = this._endX;
            while (((!((this._nowY == this._startY))) || (!((this._nowX == this._startX))))) {
                vector1.add(MapPoint.fromCoords(this._nowX, this._nowY));
                this._nowY = (int) this._mapStatus.get(this._nowY).get(this._nowX).parent.get(0);
                this._nowX = (int) this._mapStatus.get(this._nowY).get(this._nowX).parent.get(1);
            }
            vector1.add(this._startPoint);
            if(this._allowDiag) {
            	Vector<MapPoint> vector2 = new Vector<MapPoint>();
            	int vector1Size = vector1.size();
            	for(int i = 0; i < vector1Size; ++i) {
            		vector2.add(vector1.get(i));
            		this._previousCellId = vector1.get(i).getCellId();
            		if(vector1.get(i + 2) != null && vector1.get(i).distanceToCell(vector1.get(i + 2)) == 1);
            		else {
            			MapPoint mp;
            			int i1;
            			int i2;
            			int i3;
            			int i4;
            			int i5;
            			int i6;
            			if(vector1.get(i + 3) != null && vector1.get(i).distanceToCell(vector1.get(i + 3)) == 2) {
            				i1 = vector1.get(i).getX();
            				i2 = vector1.get(i).getY();
            				i3 = vector1.get(i + 3).getX();
            				i4 = vector1.get(i + 3).getY();
            				i5 = i1 + Math.round((i3 - i1) / 2);
            				i6 = i2 + Math.round((i4 - i2) / 2);
            				if(this._map.pointMov(i5, i6, true, this._previousCellId, this._endPoint.getCellId()) && this._map.pointWeight(i5, i6, true) < 2) {
            					mp = MapPoint.fromCoords(i5, i6);
            					vector2.add(mp);
            					this._previousCellId = mp.getCellId();
            					i += 2;
            				}
            			}
            			else
            				if(vector1.get(i + 2) != null && vector1.get(i).distanceToCell(vector1.get(i + 2)) == 2) {
            					i1 = vector1.get(i).getX();
            					i2 = vector1.get(i).getY();
                                i3 = vector1.get(i + 2).getX();
                                i4 = vector1.get(i + 2).getY();
                                i5 = vector1.get(i + 1).getX();
                                i6 = vector1.get(i + 1).getY();
                                if((i1 + i2) == (i3 + i4) && (i1 - i2) != (i5 - i6) && !this._map.isChangeZone(MapPoint.fromCoords(i1, i2).getCellId(), MapPoint.fromCoords(i5, i6).getCellId()) && !this._map.isChangeZone(MapPoint.fromCoords(i5, i6).getCellId(), MapPoint.fromCoords(i3, i4).getCellId()));
                                else
                                    if((i1 - i2) == (i3 - i4) && (i1 - i2) != (i5 - i6) && !this._map.isChangeZone(MapPoint.fromCoords(i1, i2).getCellId(), MapPoint.fromCoords(i5, i6).getCellId()) && !this._map.isChangeZone(MapPoint.fromCoords(i5, i6).getCellId(), MapPoint.fromCoords(i3, i4).getCellId()));
                                    else
                                    	if(i1 == i3 && i1 != i5 && this._map.pointWeight(i1, i6, true) < 2 && this._map.pointMov(i1, i6, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId())) {
                                    		mp = MapPoint.fromCoords(i1, i6);
                                    		vector2.add(mp);
                                    		this._previousCellId = mp.getCellId();
                                    	}
                                    	else
                                    		if(i2 == i4 && i2 != i6 && this._map.pointWeight(i5, i2, true) < 2 && this._map.pointMov(i5, i2, this._bAllowTroughEntity, this._previousCellId, this._endPoint.getCellId())) {
                                    			mp = MapPoint.fromCoords(i5, i2);
                                    			vector2.add(mp);
                                    			this._previousCellId = mp.getCellId();

                                    		}
            				}
            		}	
            	}
            	vector1 = vector2;
            	if(vector1.size() == 1)
            		vector1 = new Vector<MapPoint>();
            	Collections.reverse(vector1);
            	movementPathFromArray(vector1);
            }
    	}
    }
    
    private void findPathInternal(DataMapProvider dmp, MapPoint src, MapPoint dest, boolean b) {
    	this._map = dmp;
    	//this._start = src;
    	//this._end = dest;
    	this._allowDiag = b;
        this._startPoint = MapPoint.fromCoords(src.getX(), src.getY());
        this._endPoint = MapPoint.fromCoords(dest.getX(), dest.getY());
        this._startX = src.getX();
        this._startY = src.getY();
        this._endX = dest.getX();
        this._endY = dest.getY();
        this._endPointAux = this._startPoint;
        this._endAuxX = this._startX;
        this._endAuxY = this._startY;
        this._distanceToEnd = this._startPoint.distanceToCell(this._endPoint);
        this._mapStatus = new Hashtable<Integer, Hashtable<Integer, CellInfo>>();
        for(int i = _minY; i < _maxY; ++i) {
        	this._mapStatus.put(i, new Hashtable<Integer, CellInfo>());
        	for(int j = _minX; i < _maxX; ++j)
        		this._mapStatus.get(i).put(j, new CellInfo(0, new Vector<Object>(), false, false));
        }
        this._openList = new Vector<int[]>();
        openSquare(this._startY, this._startX, null, 0, 0, false);
        initFindPath();
    }
    
    /*
    private String tracePath(Vector<Object> vector) {
    	String str = "";
    	MapPoint mp;
    	int vectorSize = vector.size();
    	for(int i = 0; i < vectorSize; ++i) {
    		mp = (MapPoint) vector.get(i);
    		str = " " + mp.getCellId();
    	}
    	return str;
    }
    
    private int nearObstacle(int i1, int i2, DataMapProvider dmp) {
    	int nb = 0;
    	for(int i = -2; i < 2; ++i)
    		for(int j = -2; j < 2; ++i)
    			if(!dmp.pointMov(i1 + i, i2 + j, true, this._previousCellId, this._endPoint.getCellId()))
    				nb = Math.min(nb, MapPoint.fromCoords(i1, i2).distanceToCell(MapPoint.fromCoords(i1 + i, i2 + j)));
    	return nb;
    }
    */
}