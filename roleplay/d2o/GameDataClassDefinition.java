package roleplay.d2o;

import java.util.Vector;

import utilities.ByteArray;

public class GameDataClassDefinition {
	private Class<?> _class;
	private Vector<GameDataField> _fields;
	
	public GameDataClassDefinition(String str1, String str2) {
		try {
			this._class = Class.forName(getClass().getPackage().getName() + "." + str2);
		} catch(Exception e) {
			e.printStackTrace();
		}
        this._fields = new Vector<GameDataField>();
	}
	
	public Vector<GameDataField> getFields() {
		return this._fields;
	}
	
	public Object read(String str, ByteArray array) {
		Object o = null;
		try {
			o = this._class.newInstance();
			for(GameDataField field : this._fields)
				o.getClass().getField(field.name).set(o, field.readData.invoke(str, array));
			if(o instanceof IPostInit)
				((IPostInit) o).postInit();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public void addField(String str, ByteArray array) {
		GameDataField field = new GameDataField(str);
		field.readType(array);
		this._fields.add(field);
	}
}