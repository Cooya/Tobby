package roleplay.d2o;

import java.util.Hashtable;

import utilities.ByteArray;

// les flux sont remplacés par des tableaux d'octets
// les URI sont remplacées par des strings
public class GameDataFileAccessor {
	private static final String ANKAMA_SIGNED_FILE_HEADER = "AKSF";
	private static final String d2oPath = "Ressources/Antibot/data/common/";
    private static Hashtable<String, ByteArray> _streams = new Hashtable<String, ByteArray>();
    private static Hashtable<String, Integer> _streamStartIndex = new Hashtable<String, Integer>();
    private static Hashtable<String, Hashtable<Integer, Integer>> _indexes = new Hashtable<String, Hashtable<Integer,Integer>>();
    private static Hashtable<String, Hashtable<Integer, GameDataClassDefinition>> _classes = new Hashtable<String, Hashtable<Integer, GameDataClassDefinition>>();
    private static Hashtable<String, Integer> _counter = new Hashtable<String, Integer>();
    private static Hashtable<String, GameDataProcess> _gameDataProcessor = new Hashtable<String, GameDataProcess>();
    
    public static void init(String d2oModule) {
    	ByteArray buffer;
    	if(!_streams.containsKey(d2oModule)) {
    		buffer = ByteArray.fileToByteArray(d2oPath + d2oModule + ".d2o");
    		_streams.put(d2oModule, buffer);
    		_streamStartIndex.put(d2oModule, 7);
    	}
    	else {
    		buffer = _streams.get(d2oModule);
    		buffer.setPos(0);
    	}
    	initFromIDataInput(buffer, d2oModule);
    }
    
    public static void initFromIDataInput(ByteArray buffer, String d2oModule) {
    	Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
    	_indexes.put(d2oModule, ht);
    	int val = 0;
    	if(!buffer.readUTFBytes(3).equals("D2O")) {
    		buffer.setPos(0);
    		if(!buffer.readUTF().equals(ANKAMA_SIGNED_FILE_HEADER))
    			throw new Error("Malformated game data file.");
    		buffer.readShort();
    		buffer.incPos(buffer.readInt());
    		val = buffer.getPos();
    		_streamStartIndex.put(d2oModule, val + 7);
    		if(buffer.readUTFBytes(3) != "D2O")
    			throw new Error("Malformated game data file.");
    	}
    	buffer.setPos(val + buffer.readInt());
    	int nb = buffer.readInt();
    	val = 0;
    	for(int i = 0; i < nb; i += 8) {
    		ht.put(buffer.readInt(), buffer.readInt());
    		val++;
    	}
    	_counter.put(d2oModule, val);
    	Hashtable<Integer, GameDataClassDefinition> ht2 = new Hashtable<Integer, GameDataClassDefinition>();
    	_classes.put(d2oModule, ht2);
    	nb = buffer.readInt();
    	for(int i = 0; i < nb; ++i)
    		readClassDefinition(buffer.readInt(), buffer, ht2);
    	if(!buffer.endOfArray())
    		_gameDataProcessor.put(d2oModule, new GameDataProcess(buffer));
    }
    
    public static GameDataProcess getDataProcessor(String str) {
    	return _gameDataProcessor.get(str);
    }
    
    public static GameDataClassDefinition getClassDefinition(String str, int i) {
    	return _classes.get(str).get(i);
    }
    
    public static int getCount(String str) {
    	return _counter.get(str);
    }
    
    public static Object getObject(String str, int i) {
    	if(!_indexes.contains(str))
    		return null;
    	if(!_indexes.get(str).contains(i))
    		return null;
    	int pos = _indexes.get(str).get(i);
    	_streams.get(str).setPos(pos);
    	pos = _streams.get(str).readInt();
    	return _classes.get(str).get(pos).read(str, _streams.get(str));
    }
    
    public static Object[] getObjects(String str) {
    	if(!_counter.contains(str))
    		return null;
    	int nb = _counter.get(str);
    	Hashtable<Integer, GameDataClassDefinition> ht = _classes.get(str);
    	ByteArray array = _streams.get(str);
    	array.setPos(_streamStartIndex.get(str));
    	Object[] objArray = new Object[nb];
    	for(int i = 0; i < nb; ++i)
    		objArray[i] = ht.get(array.readInt()).read(str, array);
    	return objArray;
    }
    
    public void close() {
    	_streams.clear();
    	_indexes.clear();
    	_classes.clear();
    }
    
    private static void readClassDefinition(int i, ByteArray buffer, Hashtable<Integer, GameDataClassDefinition> ht) {
    	String str1 = buffer.readUTF();
    	String str2 = buffer.readUTF();
    	GameDataClassDefinition GDCD = new GameDataClassDefinition(str2, str1);
    	int nb = buffer.readInt();
    	for(int j = 0; j < nb; ++j)
    		GDCD.addField(buffer.readUTF(), buffer);
    	ht.put(i, GDCD);
    }
}
