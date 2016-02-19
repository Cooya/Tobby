package game.d2p.ankama;

import java.awt.Point;
import java.util.Vector;

public class CellIdConverter {
	private static final int MAP_WIDTH = 14;
	private static final int MAP_HEIGHT = 20;
    public static Vector<Point> CELLPOS = new Vector<Point>();
    private static boolean _bInit = false;
    
    private static void init() {
    	_bInit = true;
    	int i1 = 0;
    	int i2 = 0;
    	for(int i = 0; i < MAP_HEIGHT; ++i) {
    		for(int j = 0; j < MAP_WIDTH; ++j)
    			CELLPOS.add(new Point(i1 + j, i2 + j));
    		i1++;
    		for(int j = 0; j < MAP_WIDTH; ++j)
    			CELLPOS.add(new Point(i1 + j, i2 + j));
    		i2--;
    	}
    }
    
    public static int coordToCellId(int i1, int i2) {
    	if(!_bInit)
    		init();
    	return (((i1 - i2) * MAP_WIDTH) + i2) + ((i1 - i2) / 2);
    }

    public static Point cellIdToCoord(int i) {
    	if(!_bInit)
    		init();
    	if(i >= CELLPOS.size())
    		return null;
    	return CELLPOS.elementAt(i);
    }
}
