package utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import main.FatalError;

public class BiMap<T1, T2> implements BiStruct<T1, T2> {
	private Map<T1, T2> map1;
	private Map<T2, T1> map2;
	private Class<T1> t1;
	private Class<T2> t2;
	
	public BiMap(Class<T1> t1Class, Class<T2> t2Class) {
		map1 = new HashMap<T1, T2>();
		map2 = new HashMap<T2, T1>();	
		t1 = t1Class;
		t2 = t2Class;
	}
	
	@Override
	public void put(T1 t1, T2 t2) {
		try {
			if(t1 == null || t2 == null)
				throw new Exception("None objet of BiMap can be null.");
			map1.put(t1, t2);
			map2.put(t2, t1);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object get(Object o) {
		Class<?> c = o.getClass();
		if(c == t1)
			return map1.get(o);
		else if(c == t2)
			return map2.get(o);
		else
			throw new FatalError("Invalid key type.");
	}
	
	@Override
	public Collection<T1> keys() {
		return this.map1.keySet();
	}
	
	@Override
	public Collection<T2> values() {
		return this.map2.keySet();
	}
	
	@Override
	public int size() {
		return this.map1.size();
	}
}