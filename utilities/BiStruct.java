package utilities;

import java.util.Collection;

public interface BiStruct<T1, T2> {
	void put(T1 t1, T2 t2);
	Object get(Object o);
	Collection<T1> keys();
	Collection<T2> values();
	int size();
}