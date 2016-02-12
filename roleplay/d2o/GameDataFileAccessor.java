package roleplay.d2o;

import java.util.Hashtable;

import utilities.ByteArray;

// les flux sont remplacés par des tableaux d'octets
// les URI sont remplacées par des strings
public class GameDataFileAccessor {
	private static final String ANKAMA_SIGNED_FILE_HEADER = "AKSF";
	private static final String d2oPath = "Ressources/Antibot/data/common/";
	private static GameDataFileAccessor _self;
	
    private Hashtable<String, ByteArray> _streams;
    private Hashtable<String, Integer> _streamStartIndex;
    private Hashtable<String, Hashtable<Integer, Integer>> _indexes;
    private Hashtable<String, Hashtable<Integer, GameDataClassDefinition>> _classes;
    private Hashtable<String, Integer> _counter;
    private Hashtable<String, GameDataProcess> _gameDataProcessor;
    
    public GameDataFileAccessor() {
    	if(_self != null)
    		throw new Error("Singleton object already instantiated.");
    }
    
    public static GameDataFileAccessor getInstance() {
    	if(_self == null)
    		_self = new GameDataFileAccessor();
    	return _self;
    }
    
    public void init(String d2oModule) {
    	if (this._streams == null)
            this._streams = new Hashtable<String, ByteArray>();
        if (this._streamStartIndex == null)
            this._streamStartIndex = new Hashtable<String, Integer>();
    	ByteArray buffer;
    	if(!this._streams.containsKey(d2oModule)) {
    		buffer = ByteArray.fileToByteArray(d2oPath + d2oModule + ".d2o");
    		this._streams.put(d2oModule, buffer);
    		this._streamStartIndex.put(d2oModule, 7);
    	}
    	else {
    		buffer = this._streams.get(d2oModule);
    		buffer.setPos(0);
    	}
    	initFromIDataInput(buffer, d2oModule);
    }
    
    public void initFromIDataInput(ByteArray buffer, String d2oModule) {
    	if (this._streams == null)
            this._streams = new Hashtable<String, ByteArray>();
        if (this._indexes == null)
            this._indexes = new Hashtable<String, Hashtable<Integer,Integer>>();
        if (this._classes == null)
            this._classes = new Hashtable<String, Hashtable<Integer,GameDataClassDefinition>>();
        if (this._counter == null)
            this._counter = new Hashtable<String, Integer>();
        if (this._streamStartIndex == null)
            this._streamStartIndex = new Hashtable<String, Integer>();
        if (this._gameDataProcessor == null)
            this._gameDataProcessor = new Hashtable<String, GameDataProcess>();
    	Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
    	this._indexes.put(d2oModule, ht);
    	int val = 0;
    	if(!buffer.readUTFBytes(3).equals("D2O")) {
    		buffer.setPos(0);
    		if(!buffer.readUTF().equals(ANKAMA_SIGNED_FILE_HEADER))
    			throw new Error("Malformated game data file.");
    		buffer.readShort();
    		buffer.incPos(buffer.readInt());
    		val = buffer.getPos();
    		this._streamStartIndex.put(d2oModule, val + 7);
    		if(buffer.readUTFBytes(3) != "D2O")
    			throw new Error("Malformated game data file.");
    	}
    	int k = buffer.readInt();
    	buffer.setPos(val + k);
    	int nb = buffer.readInt();
    	val = 0;
    	for(int i = 0; i < nb; i += 8) {
    		ht.put(buffer.readInt(), buffer.readInt());
    		val++;
    	}
    	this._counter.put(d2oModule, val);
    	Hashtable<Integer, GameDataClassDefinition> ht2 = new Hashtable<Integer, GameDataClassDefinition>();
    	this._classes.put(d2oModule, ht2);
    	nb = buffer.readInt();
    	for(int i = 0; i < nb; ++i)
    		readClassDefinition(buffer.readInt(), buffer, ht2);
    	if(!buffer.endOfArray())
    		this._gameDataProcessor.put(d2oModule, new GameDataProcess(buffer));
    }
    
    public GameDataProcess getDataProcessor(String str) {
    	return this._gameDataProcessor.get(str);
    }
    
    public GameDataClassDefinition getClassDefinition(String str, int i) {
    	return this._classes.get(str).get(i);
    }
    
    public int getCount(String str) {
    	return this._counter.get(str);
    }
    
    public Object getObject(String str, int i) {
    	if(this._indexes == null || !this._indexes.containsKey(str))
    		return null;
    	if(!this._indexes.get(str).containsKey(i))
    		return null;
    	int pos = this._indexes.get(str).get(i);
    	this._streams.get(str).setPos(pos);
    	pos = this._streams.get(str).readInt();
    	return this._classes.get(str).get(pos).read(str, this._streams.get(str));
    }
    
    public Object[] getObjects(String str) {
    	if(this._counter == null || !this._counter.containsKey(str))
    		return null;
    	int nb = this._counter.get(str);
    	Hashtable<Integer, GameDataClassDefinition> ht = this._classes.get(str);
    	ByteArray array = this._streams.get(str);
    	array.setPos(this._streamStartIndex.get(str));
    	Object[] objArray = new Object[nb];
    	for(int i = 0; i < nb; ++i)
    		objArray[i] = ht.get(array.readInt()).read(str, array);
    	return objArray;
    }
    
    public void close() {
    	this._streams.clear();
    	this._indexes.clear();
    	this._classes.clear();
    }
    
    private void readClassDefinition(int i, ByteArray buffer, Hashtable<Integer, GameDataClassDefinition> ht) {
    	String str1 = buffer.readUTF();
    	String str2 = buffer.readUTF();
    	GameDataClassDefinition GDCD = new GameDataClassDefinition(str2, str1);
    	int nb = buffer.readInt();
    	for(int j = 0; j < nb; ++j)
    		GDCD.addField(buffer.readUTF(), buffer);
    	ht.put(i, GDCD);
    }
}