package roleplay.d2p.ankama;

import java.util.Vector;

import roleplay.d2p.Cell;
import roleplay.pathfinding.Pathfinder;
import utilities.ByteArray;

public class Map {
	public static final int WIDTH = 14;
	public static final int HEIGHT = 40; // normalement 20
	private static final String decryptionKey = "649ae451ca33ec53bbcbcc33becf15f4";
	public static final int CELLS_COUNT = 560;
    public Class<Map> mapClass;
    public int mapVersion;
    public boolean encrypted;
    public int encryptionVersion;
    public int groundCRC;
    public double zoomScale = 1;
    public int zoomOffsetX;
    public int zoomOffsetY;
    public int groundCacheCurrentlyUsed = 0;
    public int id;
    public int relativeId;
    public int mapType;
    public int backgroundsCount;
    public Vector<Fixture> backgroundFixtures;
    public int foregroundsCount;
    public Vector<Fixture> foregroundFixtures;
    public int subareaId;
    public int shadowBonusOnEntities;
    public int backgroundColor;
    public int backgroundRed;
    public int backgroundGreen;
    public int backgroundBlue;
    public int topNeighbourId;
    public int bottomNeighbourId;
    public int leftNeighbourId;
    public int rightNeighbourId;
    public boolean useLowPassFilter;
    public boolean useReverb;
    public int presetId;
    public int cellsCount;
    public int layersCount;
    public boolean isUsingNewMovementSystem = false;
    public Vector<Layer> layers;
    public Vector<Cell> cells;
    public Vector<Integer> topArrowCell;
    public Vector<Integer> leftArrowCell;
    public Vector<Integer> bottomArrowCell;
    public Vector<Integer> rightArrowCell;
    private boolean _parsed;
    private boolean _failed;
    //private Vector<> _gfxList;
    //private Vector<> _gfxCount;
    
    public Map(ByteArray raw) {
    	this.mapClass = Map.class;
		this.topArrowCell = new Vector<Integer>();
		this.bottomArrowCell = new Vector<Integer>();
		this.leftArrowCell = new Vector<Integer>();
		this.rightArrowCell = new Vector<Integer>();
    	fromRaw(raw);
	}
    
    public boolean getParsed() {
    	return this._parsed;
    }
    
    public boolean getFailed() {
    	return this._failed;
    }
    
    public void fromRaw(ByteArray raw) {
    	raw.readByte(); // 77
    	this.mapVersion = raw.readByte();
    	this.id = raw.readInt();
    	if(this.mapVersion >= 7) {
    		this.encrypted = raw.readBoolean();
    		this.encryptionVersion = raw.readByte();
    		int dataLen = raw.readInt();
    		if(this.encrypted) {
    			if(decryptionKey == null)
                    throw new Error("Map decryption key is empty.");
    			byte[] encryptedData = raw.readBytes(dataLen);
    			int keySize = decryptionKey.length();
    			for(int i = 0; i < dataLen; ++i)
    				encryptedData[i] = (byte) (encryptedData[i] ^ decryptionKey.charAt(i % keySize));
    			raw.setArray(encryptedData);
    		}
    	} 	
		this.relativeId = raw.readInt();
		this.mapType = raw.readByte();
		this.subareaId = raw.readInt();
		this.topNeighbourId = raw.readInt();
		this.bottomNeighbourId = raw.readInt();
		this.leftNeighbourId = raw.readInt();
		this.rightNeighbourId = raw.readInt();
		this.shadowBonusOnEntities = raw.readInt();
		if(this.mapVersion >= 3) {
            this.backgroundRed = raw.readByte();
            this.backgroundGreen = raw.readByte();
            this.backgroundBlue = raw.readByte();
            this.backgroundColor = ((((this.backgroundRed & 0xFF) << 16) | ((this.backgroundGreen & 0xFF) << 8)) | (this.backgroundBlue & 0xFF));
		}
		if(this.mapVersion >= 4) {
            this.zoomScale = (raw.readShort() / 100);
            this.zoomOffsetX = raw.readShort();
            this.zoomOffsetY = raw.readShort();
            if(this.zoomScale < 1) {
            	this.zoomScale = 1;
            	this.zoomOffsetX = (this.zoomOffsetY = 0);
            }
            this.useLowPassFilter = (raw.readByte() == 1);
            this.useReverb = (raw.readByte() == 1);
            if(this.useReverb)
            	this.presetId = raw.readInt();
            else
            	this.presetId = -1;
            Fixture bg;
            this.backgroundsCount = raw.readByte();
            this.backgroundFixtures = new Vector<Fixture>();
            for(int i = 0; i < this.backgroundsCount; ++i) {
            	bg = new Fixture(this);
            	bg.fromRaw(raw);
            	this.backgroundFixtures.add(bg);
            }
            this.foregroundsCount = raw.readByte();
            this.foregroundFixtures = new Vector<Fixture>();
            for(int i = 0; i < this.foregroundsCount; ++i) {
            	bg = new Fixture(this);
            	bg.fromRaw(raw);
            	this.foregroundFixtures.add(bg);
            }
            this.cellsCount = CELLS_COUNT;
            raw.readInt();
            this.groundCRC = raw.readInt();
            this.layersCount = raw.readByte();
            this.layers = new Vector<Layer>();
            Layer layer;
            for(int i = 0; i < layersCount; ++i) {
            	layer = new Layer(this);
            	layer.fromRaw(raw, this.mapVersion);
            	this.layers.add(layer);
            }
            this.cells = new Vector<Cell>();
            Cell cd;
            int _oldMvtSystem = -1;
            for(int i = 0; i < cellsCount; ++i) {
            	cd = new Cell(this, i);
            	cd.fromRaw(raw);
            	if(_oldMvtSystem == -1)
            		_oldMvtSystem = cd.moveZone;
            	if(cd.moveZone != _oldMvtSystem)
            		this.isUsingNewMovementSystem = true;
            	this.cells.add(cd);
            }
            this._parsed = true;
		}
    }
    
    public int getNeighbourMapFromDirection(int direction) {
    	switch(direction) {
    		case Pathfinder.LEFT : return this.leftNeighbourId;
    		case Pathfinder.RIGHT : return this.rightNeighbourId;
    		case Pathfinder.UP : return this.topNeighbourId;
    		case Pathfinder.DOWN : return this.bottomNeighbourId;
    		default : throw new Error("Invalid direction for find neighbour map id");
    	}
    }
    
    /*
    public static int getMapIdFromCoords(int worldId, int x, int y) {
    	int var1 = 2 << 12;
    	int var2 = 2 << 8;
    	if(worldId > var1 || x > var2 || y > var2)
    		return -1;
    	int var3 = worldId & 4095;
    	int var4 = Math.abs(x) & 0xFF;
    	if(x < 0)
    		var4 = var4 | 0x0100;
    	int var5 = Math.abs(y) & 0xFF;
    	if(y < 0)
    		var5 = var5 | 0x0100;
    	return (var3 << 18) | (var4 << 9) | var5;
    }
    */
}