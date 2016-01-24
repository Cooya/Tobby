package movement;

import java.util.Hashtable;
import java.util.zip.Inflater;

import main.Main;
import utilities.ByteArray;
import utilities.Log;

public class D2pReader {
	private static final String d2pPath = "Ressources/Antibot/content/maps/maps0.d2p";
    private static Hashtable<String, Hashtable<String, Object[]>> indexes = new Hashtable<String, Hashtable<String, Object[]>>();
    private static Hashtable<String, Hashtable<String, String>> properties = new Hashtable<String, Hashtable<String, String>>();

    static {
    	readD2pFiles(d2pPath);
    }
    
	public static ByteArray getBinaryMap(int mapId) {
		Log.p("Retrieving binary data of map id " + mapId + "...");
		Object[] index = indexes.get(d2pPath).get(getMapUriFromId(mapId));
		if(index == null)
			throw new Error("Unknown map id.");
		ByteArray binaryMap = ((ByteArray) index[2]).clonePart((int) index[0], (int) index[1]);
		decompressBinaryMap(binaryMap);
		return binaryMap;
	}
	
	private static void readD2pFiles(String filepath) {
		Log.p("Reading d2p files...");
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
				throw new Error("Invalid d2p file header.");
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
		Log.p("Decompressing binary data...");
		if(binaryMap.readByte() == 77) {
			binaryMap.setPos(0);
			return;
		}
		Inflater inflater = new Inflater();
		inflater.setInput(binaryMap.bytes());
		byte[] buffer = new byte[Main.BUFFER_SIZE];
		binaryMap.flushArray();
		int bytesCounter = 0;
		while (!inflater.finished()) {
			try {
				bytesCounter += inflater.inflate(buffer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			binaryMap.writeBytes(buffer);
		}
		Log.p(bytesCounter + " bytes resulting of decompression.");
		binaryMap.setArray(buffer);
		if(binaryMap.readByte() == 77)
			throw new Error("Invalid binary map header.");
		binaryMap.setPos(0);
	}
}