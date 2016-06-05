package gamedata.d2p.ankama;

import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.Cell;

import java.util.Vector;

import main.FatalError;
import utilities.ByteArray;

public class Map {
	public static final int WIDTH = 14;
	public static final int HEIGHT = 20; // 40 pour l'ancienne version du pathfinder
	public static final int RIGHT = 0;
	public static final int DOWN_RIGHT = 1;
	public static final int DOWN = 2;
	public static final int DOWN_LEFT = 3;
	public static final int LEFT = 4;
	public static final int UP_LEFT = 5;
	public static final int UP = 6;
	public static final int UP_RIGHT = 7;
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
    public Fixture[] backgroundFixtures;
    public int foregroundsCount;
    public Fixture[] foregroundFixtures;
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
    public Layer[] layers;
    public Cell[] cells;
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
                    throw new FatalError("Map decryption key is empty.");
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
            Fixture fixture;
            this.backgroundsCount = raw.readByte();
            this.backgroundFixtures = new Fixture[this.backgroundsCount];
            for(int i = 0; i < this.backgroundsCount; ++i) {
            	fixture = new Fixture(this);
            	fixture.fromRaw(raw);
            	this.backgroundFixtures[i] = fixture;
            }
            this.foregroundsCount = raw.readByte();
            this.foregroundFixtures = new Fixture[this.foregroundsCount];
            for(int i = 0; i < this.foregroundsCount; ++i) {
            	fixture = new Fixture(this);
            	fixture.fromRaw(raw);
            	this.foregroundFixtures[i] = fixture;
            }
            this.cellsCount = CELLS_COUNT;
            raw.readInt();
            this.groundCRC = raw.readInt();
            this.layersCount = raw.readByte();
            this.layers = new Layer[this.layersCount];
            Layer layer;
            for(int i = 0; i < layersCount; ++i) {
            	layer = new Layer(this);
            	layer.fromRaw(raw, this.mapVersion);
            	this.layers[i] = layer;
            }
            this.cells = new Cell[this.cellsCount];
            Cell cell;
            int _oldMvtSystem = -1;
            for(int i = 0; i < this.cellsCount; ++i) {
            	cell = new Cell(this, i);
            	cell.fromRaw(raw);
            	if(_oldMvtSystem == -1)
            		_oldMvtSystem = cell.moveZone;
            	if(cell.moveZone != _oldMvtSystem)
            		this.isUsingNewMovementSystem = true;
            	this.cells[i] = cell;
            }
            this._parsed = true;
		}
    }
    
    public int getNeighbourMapFromDirection(int direction) {
    	switch(direction) {
    		case LEFT : return this.leftNeighbourId;
    		case RIGHT : return this.rightNeighbourId;
    		case UP : return this.topNeighbourId;
    		case DOWN : return this.bottomNeighbourId;
    		default : throw new FatalError("Invalid direction for find neighbour map id");
    	}
    }
    
	public static String directionToString(int direction) {
		switch(direction) {
			case RIGHT : return "right";
			case DOWN_RIGHT : return "down and right";
			case DOWN : return "down";
			case DOWN_LEFT : return "down and left";
			case LEFT : return "left";
			case UP_LEFT : return "up and left";
			case UP : return "up";
			case UP_RIGHT : return "up and right";
			default : throw new FatalError("Invalid direction integer.");
		}
	}
    
	@Override
    public String toString() {
		MapPosition mp = MapPosition.getMapPositionById(this.id);
    	return this.id + " [" + mp.posX + ", " + mp.posY + "]";
    }
}