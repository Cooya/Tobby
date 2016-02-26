package messages;

import java.io.BufferedReader;
import java.io.FileReader;

import main.FatalError;
import main.Instance;
import utilities.BiMap;
import utilities.ByteArray;

public class Message {
	private static final String messagesFilePath = "Ressources/messages.txt";
	private static final BiMap<Integer, String> map = new BiMap<Integer, String>(Integer.class, String.class);
	
	static  {
		try {
			Instance.log("Loading informations from messages file.");
			BufferedReader buffer = new BufferedReader(new FileReader(messagesFilePath));
			String[] splitLine;
			String line = buffer.readLine();
			while(line != null) {
				splitLine = line.split(" ");
				map.put(Integer.parseInt(splitLine[0]), splitLine[1]);
				line = buffer.readLine();
			}
			buffer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected int id;
	protected int lenofsize;
	protected int size;
	protected byte[] content;
	
	protected int contentBytesAvailables; // nombre d'octets du contenu acquis
	protected boolean complete;
	
	public Message(int id, int lenofsize, int size, byte[] content, int bytesAvailables) { // constructeur générique
		this.id = id;
		this.lenofsize = lenofsize;
		this.size = size;
		if(content != null) {
			if(content.length == size)		
				this.content = content;
			else {
				this.content = new byte[size];
				for(int i = 0; i < content.length; ++i)
					this.content[i] = content[i];
			}
		}
		
		this.contentBytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
	}
	
	public Message(Message msg) { // constructeur spécifique
		this(msg.id, msg.lenofsize, msg.size, msg.content, msg.contentBytesAvailables);
	}
	
	public Message(String msgName) { // message vide à envoyer
		this.id = get(msgName);
	}
	
	public Message() { // message à envoyer
		this.id = get(getClass().getSimpleName());
	}
	
	public static String get(int id) {
		return (String) map.get(id);
	}
	
	public static int get(String name) {
		Object id = map.get(name);
		if(id == null)
			throw new FatalError("Unknown message name : \"" + name + "\".");
		return (int) id;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<Message> getClass(int id) {
		try {
			return (Class<Message>) Class.forName(get(id));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int get(Message msg) {
		return get(msg.getClass().getSimpleName());
	}
	
	public static int get(Class<Message> c) {
		return get(c.getSimpleName());
	}
	
	public int getId() {
		return this.id;
	}

	public int getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	public static short computeLenOfSize(int size) {
	    if(size > 65535)
	        return 3;
	    else if(size > 255)
	        return 2;
	    else if(size > 0)
	        return 1;
	    else
	        return 0;
	}
	
	public byte[] makeRaw() {
		ByteArray buffer = new ByteArray(2 + this.lenofsize + this.size);
		buffer.writeShort((short) (id << 2 | lenofsize));
		if(this.lenofsize == 0) return buffer.bytes();
		else if(this.lenofsize == 1)
			buffer.writeByte((byte) this.size);
		else if(this.lenofsize == 2)
			buffer.writeShort((short) size);
		else {
			buffer.writeByte((byte) (this.size >> 16));
			buffer.writeShort((short) (size & 65535));
		}
		buffer.writeBytes(this.content);
		return buffer.bytes();
	}
	
	public void completeInfos(ByteArray buffer) {
		this.size = buffer.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		if(this.size > 0)
			this.content = buffer.bytes();
	}
	
	public boolean isComplete() {
		return this.complete;
	}
	
	public int getTotalSize() {
		return this.contentBytesAvailables + 2 + this.lenofsize;
	}
	
	public int appendContent(byte[] buffer) {
		int additionSize;
		if(buffer.length > this.size - this.contentBytesAvailables)
			additionSize = this.size - this.contentBytesAvailables;
		else
			additionSize = buffer.length;	
		for(int read = 0; read < additionSize; ++read)
			this.content[this.contentBytesAvailables + read] = buffer[read];
		this.contentBytesAvailables += additionSize;
		this.complete = contentBytesAvailables == size;
		return additionSize;
	}
}