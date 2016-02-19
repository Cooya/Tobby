package game.d2o;

import java.util.Hashtable;

public class GameData {
    //private static final double CACHE_SIZE_RATIO = 0.1;
    //private static Hashtable<String, Hashtable<Integer, WeakReference>> _directObjectCaches = new Hashtable<String, Hashtable<Integer, WeakReference>>();
    //private static Hashtable<String, Cache> _objectCaches = new Hashtable<String, Cache>();
    //private static Hashtable<String, SoftReference> _objectsCaches = new Hashtable<String, SoftReference>();
    private static Hashtable<String, Hashtable<Integer, Integer>> _overrides = new Hashtable<String, Hashtable<Integer, Integer>>();
    
    public static void addOverride(String str, int i1, int i2) {
    	if(!_overrides.containsKey(str))
    		_overrides.put(str, new Hashtable<Integer, Integer>());
    	_overrides.get(str).put(i1, i2);
    }
    
    public static Object getObject(String str, int i) {
    	//WeakReference wr;
    	Object o = null;
    	if(_overrides.containsKey(str) && _overrides.get(str).containsKey(i))
    		i = _overrides.get(str).get(i);
    	/*
    	if(!_directObjectCaches.containsKey(str))
    		_directObjectCaches.put(str, new Hashtable<Integer, WeakReference>());
    	else {
    		wr = _directObjectCaches.get(str).get(i);
    		if(wr != null) {
    			o = wr.getObject();
    			if(o != null)
    				return o;
    		}
    	}
    	if(!_objectCaches.containsKey(str))
    		_objectCaches.put(str, new Cache(GameDataFileAccessor.getInstance().getCount(str) * CACHE_SIZE_RATIO), new LruGarbageCollector());
    	else {
    		o = _objectCaches.get(str).peek(i);
    		if(o != null)
    			return o;
    	}
    	*/
    	o = GameDataFileAccessor.getInstance().getObject(str, i);
    	//_directObjectCaches.get(str).put(i, new WeakReference(o));
    	//_objectCaches.get(str).store(i, o);
    	return o;
    }
    
    public static Object[] getObjects(String str) {
    	Object[] array;
    	/*
    	if(_objectsCaches.containsKey(str)) {
    		array = _objectsCaches.get(str).getObject();
    		if(array != null)
    			return array;
    	}
    	*/
    	array = GameDataFileAccessor.getInstance().getObjects(str);
    	//_objectsCaches.put(str, new SoftReference(array));
    	return array;
    }
}