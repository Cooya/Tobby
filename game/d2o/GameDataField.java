package game.d2o;

import java.lang.reflect.Method;
import java.util.Vector;

import utilities.ByteArray;

public class GameDataField {
    private static final int NULL_IDENTIFIER = -1431655766;
    public String name;
    public Method readData;
    private Vector<Method> _innerReadMethods;
    private Vector<String> _innerTypeNames;
    
    public GameDataField(String name) {
    	this.name = name;
    }
    
    public void readType(ByteArray array) {
    	try {
			this.readData = getReadMethod(array.readInt(), array);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private Method getReadMethod(int type, ByteArray array) throws Exception {
    	switch(type) {
    		case(GameDataTypeEnum.INT) : return getClass().getDeclaredMethod("readInteger", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.BOOLEAN) : return getClass().getDeclaredMethod("readBoolean", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.STRING) : return getClass().getDeclaredMethod("readString", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.NUMBER) : return getClass().getDeclaredMethod("readNumber", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.I18N) : return getClass().getDeclaredMethod("readI18n", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.UINT) : return getClass().getDeclaredMethod("readUnsignedInteger", String.class, ByteArray.class, Integer.class);
    		case(GameDataTypeEnum.VECTOR) : 
    			if(this._innerReadMethods == null) {
    				this._innerReadMethods = new Vector<Method>();
                    this._innerTypeNames = new Vector<String>();
    			}
    			this._innerTypeNames.add(array.readUTF());
    			this._innerReadMethods.add(0, getReadMethod(array.readInt(), array));
    			return getClass().getDeclaredMethod("readVector", String.class, ByteArray.class, Integer.class);
    		default :
    			if(type > 0)
    				return getClass().getDeclaredMethod("readObject", String.class, ByteArray.class, Integer.class);
    			throw new Error("Unknown type '" + type + "'.");
    	}
    }
    
    @SuppressWarnings("unused")
	private Object readVector(String str, ByteArray array, Integer index) throws Exception {
    	int nb = array.readInt();
    	String str2 = this._innerTypeNames.get(index); // debug utile
    	Vector<Object> o = new Vector<Object>();
    	this._innerReadMethods.get(index).setAccessible(true);
    	for(int j = 0; j < nb; ++j)
    		o.add(this._innerReadMethods.get(index).invoke(this, str,  array, index + 1));
    	return o;
    }
    
    @SuppressWarnings("unused")
	private Object readObject(String str, ByteArray array, Integer index) {
    	int nb = array.readInt();
    	if(nb == NULL_IDENTIFIER)
    		return null;
    	GameDataClassDefinition classDef = GameDataFileAccessor.getInstance().getClassDefinition(str, nb);
    	return classDef.read(str, array);
    }
    
    @SuppressWarnings("unused")
	private Object readInteger(String str, ByteArray array, Integer index) {
    	return array.readInt();
    }
    
    @SuppressWarnings("unused")
	private Object readBoolean(String str, ByteArray array, Integer index) {
    	return array.readBoolean();
    }
    
    @SuppressWarnings("unused")
	private Object readString(String str, ByteArray array, Integer index) {
    	String str2 = array.readUTF();
    	if(str2.equals("null"))
    		return null;
    	return str2;
    }
    
    @SuppressWarnings("unused")
	private Object readNumber(String str, ByteArray array, Integer index) {
    	return array.readDouble();
    }
    
    @SuppressWarnings("unused")
	private Object readI18n(String str, ByteArray array, Integer index) {
    	return array.readInt();
    }
    
    @SuppressWarnings("unused")
	private Object readUnsignedInteger(String str, ByteArray array, Integer index) {
    	return array.readInt();
    }
}
