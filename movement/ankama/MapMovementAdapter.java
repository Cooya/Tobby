package movement.ankama;

import java.util.Vector;

public class MapMovementAdapter {
	
	public static Vector<Integer> getServerMovement(MovementPath mp) {
		mp.compress();
		int nb;
		Vector<Integer> result = new Vector<Integer>();
		PathElement pe;
		int mpLength = mp.getPath().size();
		for(int i = 0; i < mpLength; ++i) {
			pe = mp.getPath().get(i);
			nb = ((pe.getOrientation() & 7) << 12) | (pe.getStep().getCellId() & 4095);
			result.add(nb);
		}
		// to check
		return result;
	}
	
	/*
    public static function getServerMovement(_arg_1:MovementPath):Vector.<uint>
    {
        var _local_5:PathElement;
        var _local_6:int;
        var _local_7:int;
        var _local_8:String;
        var _local_9:uint;
        _arg_1.compress();
        var _local_2:Vector.<uint> = new Vector.<uint>();
        var _local_3:uint;
        var _local_4:uint;
        for each (_local_5 in _arg_1.path)
        {
            _local_3 = _local_5.orientation;
            _local_7 = (((_local_3 & 7) << 12) | (_local_5.step.cellId & 4095));
            _local_2.push(_local_7);
            _local_4++;
        };
        _local_6 = (((_local_3 & 7) << 12) | (_arg_1.end.cellId & 4095));
        _local_2.push(_local_6);
        if (DEBUG_ADAPTER)
        {
            _local_8 = "";
            for each (_local_9 in _local_2)
            {
                _local_8 = (_local_8 + ((_local_9 & 4095) + " > "));
            };
            _log.debug(("Sending path : " + _local_8));
        };
        return (_local_2);
    }
    */
}
