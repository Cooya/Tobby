package roleplay.d2o;

import java.util.Collection;
import java.util.Hashtable;

public class WeakReference {
    private Hashtable<Object, Object> dictionary;

    public WeakReference(Object o) {
        this.dictionary = new Hashtable<Object, Object>();
        this.dictionary.put(o, null);
    }
    
    public Object getObject() {
    	Collection<Object> coll = dictionary.values();
    	for(Object o : coll)
    		if(o != null)
    			return o;
    	return null;
    }
    
    public void destroy() {
    	this.dictionary.clear();
    }
}
