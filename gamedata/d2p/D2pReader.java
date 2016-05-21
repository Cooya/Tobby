package gamedata.d2p;

import java.util.Hashtable;
import java.util.zip.Inflater;

import controller.characters.Character;
import main.FatalError;
import main.Main;
import utilities.ByteArray;

public class D2pReader {
	private static final boolean DEBUG = false;
    private static Hashtable<String, Hashtable<String, Object[]>> indexes = new Hashtable<String, Hashtable<String, Object[]>>();
    private static Hashtable<String, Hashtable<String, String>> properties = new Hashtable<String, Hashtable<String, String>>();

    static {
    	readD2pFiles(Main.D2P_PATH);
    }
    
	public synchronized static ByteArray getBinaryMap(int mapId) {
		if(DEBUG)
			Character.log("Retrieving binary data of map id " + mapId + "...");
		Object[] index = indexes.get(Main.D2P_PATH).get(getMapUriFromId(mapId));
		if(index == null) {
			Character.log("Unknown map id : " + mapId + ".");
			return null;
		}
		ByteArray binaryMap = ((ByteArray) index[2]).clonePart((int) index[0], (int) index[1]);
		decompressBinaryMap(binaryMap);
		return binaryMap;
	}
	
	private static void readD2pFiles(String filepath) {
		if(DEBUG)
			Character.log("Reading d2p files...");
		ByteArray buffer;
		int propertiesPos;
		int propertiesSize;
		int indexesPos;
		int indexesSize;
		int slashPos;
		String property;
		String filename;
		String index;
		int pos1;
		int pos2;
		int size;
		boolean nextFile;
		Hashtable<String, Object[]> indexesDico = new Hashtable<String, Object[]>();
		Hashtable<String, String> propertiesDico = new Hashtable<String, String>();
		indexes.put(filepath, indexesDico);
		properties.put(filepath, propertiesDico);
		
		while(true) {
			//Log.p("Reading d2p file " + filepath + "...");
			buffer = ByteArray.fileToByteArray(filepath);
			nextFile = false;
			if(buffer.readByte() != 2 || buffer.readByte() != 1)
				throw new FatalError("Invalid d2p file header.");
			buffer.setPos(buffer.getSize() - 24);
			pos1 = buffer.readInt();
			buffer.readInt();
			indexesPos = buffer.readInt();
			indexesSize = buffer.readInt();
			propertiesPos = buffer.readInt();
			propertiesSize = buffer.readInt();
			
			buffer.setPos(propertiesPos);
			for(int i = 0; i < propertiesSize; ++i) {
				property = buffer.readUTF();
				filename = buffer.readUTF();
				
				propertiesDico.put(property, filename);
				if(property.equals("link")) {
					slashPos = filepath.lastIndexOf("/");
					if(slashPos != -1)
						filepath = filepath.substring(0, slashPos) + "/" + filename;
					else
						filepath = filename;
					nextFile = true;
				}
			}
			
			buffer.setPos(indexesPos);
			for(int i = 0; i < indexesSize; ++i) {
				index = buffer.readUTF();
				pos2 = buffer.readInt();
				size = buffer.readInt();
				Object[] array = {pos1 + pos2, size, buffer};
				indexesDico.put(index, array);
			}
			
			if(!nextFile)
				break;
		}
	}
	
    private static String getMapUriFromId(int mapId) {
    	return mapId % 10 + "/" + mapId + ".dlm";
    }
	
	private static void decompressBinaryMap(ByteArray binaryMap) {
		if(binaryMap.readByte() == 77) {
			binaryMap.setPos(0);
			return;
		}
		if(DEBUG)
			Character.log("Decompressing binary data...");
		Inflater inflater = new Inflater();
		inflater.setInput(binaryMap.bytes());
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		binaryMap.flushArray();
		@SuppressWarnings("unused")
		int bytesCounter = 0;
		int bufferSize = 0;
		while(!inflater.finished()) {
			try {
				bufferSize = inflater.inflate(buffer);
				bytesCounter += bufferSize;
			} catch(Exception e) {
				e.printStackTrace();
			}
			binaryMap.writeBytes(buffer, bufferSize);
		}
		if(DEBUG)
			Character.log(bytesCounter + " bytes resulting of decompression.");
		binaryMap.setPos(0);
		if(binaryMap.readByte() != 77)
			throw new FatalError("Invalid binary map header.");
		binaryMap.setPos(0);
	}
}