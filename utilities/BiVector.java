package utilities;

import java.util.Collection;
import java.util.Vector;

import main.FatalError;

public class BiVector<T1, T2> implements BiStruct<T1, T2> {
	private Vector<T1> vect1;
	private Vector<T2> vect2;
	private Class<T1> t1;
	private Class<T2> t2;
	
	public BiVector(Class<T1> t1Class, Class<T2> t2Class) {
		this.vect1 = new Vector<T1>();
		this.vect2 = new Vector<T2>();
		t1 = t1Class;
		t2 = t2Class;
	}

	@Override
	public void put(T1 t1, T2 t2) {
		if(t1 == null) {
			this.vect1.add(t1);
			this.vect2.add(t2);
		}
		else {
			int vect1Size = this.vect1.size();
			for(int i = 0; i < vect1Size; ++i)
				if(this.vect1.get(i) == t1) {
					this.vect2.setElementAt(t2, i);
					return;
				}
			this.vect1.add(t1);
			this.vect2.add(t2);
		}
	}

	@Override
	public Object get(Object o) {
		Class<?> c = o.getClass();
		if(c == t1) {
			int index = this.vect1.indexOf(o);
			if(index != -1)
				return this.vect2.get(index);
		}
		else if(c == t2) {
			int index = this.vect2.indexOf(o);
			if(index != -1)
				return this.vect1.get(index);
		}
		else
			throw new FatalError("Invalid key type.");
		return null;
	}

	@Override
	public Collection<T1> keys() {
		return this.vect1;
	}

	@Override
	public Collection<T2> values() {
		return this.vect2;
	}

	@Override
	public int size() {
		return this.vect1.size();
	}
}