package messages;

import gui.Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;

import main.FatalError;
import utilities.BiMap;
import utilities.ByteArray;
import utilities.Reflection;

@SuppressWarnings("unchecked")
public abstract class Message {
	private static final String MESSAGES_FILEPATH = "Ressources/messages.txt";
	private static final BiMap<Integer, String> messages = new BiMap<Integer, String>(Integer.class, String.class);
	private static final Map<String, Class<Message>> msgClasses = new Hashtable<String, Class<Message>>();
	
	static {
		// récupération de la liste des messages (id + nom) depuis le fichier "messages.txt"
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(MESSAGES_FILEPATH));
			String[] splitLine;
			String line = buffer.readLine();
			while(line != null) {
				splitLine = line.split(" ");
				messages.put(Integer.parseInt(splitLine[0]), splitLine[1]);
				line = buffer.readLine();
			}
			buffer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// récupération de toutes les classes de sérialisation/désérialisation des messages dans le package "messages"
		try {
			Class<?>[] classesArray = Reflection.getClasses("messages");
			for(Class<?> cl : classesArray)
				msgClasses.put(cl.getSimpleName(), (Class<Message>) cl);
		} catch(Exception e) {
			e.printStackTrace();
			Controller.getInstance().exit("Error occured during loading deserialization classes.");
		}
	}
	
	private int id;
	private int lenofsize;
	private int size;
	protected ByteArray content;
	
	private int contentBytesAvailables; // nombre d'octets du contenu acquis
	private boolean isComplete;
	
	// constructeur pour les messages à envoyer qui n'ont pas de contenu (UnhandledMessage)
	public Message(String msgName) {
		this.id = get(msgName);
	}
	
	// constructeur pour les messages à envoyer
	public Message() {
		this.id = get(getClass().getSimpleName());
		this.content = new ByteArray();
	}
	
	// sorte de méthode "factory" pour les messages reçus
	public static Message create(int id, int lenofsize, int size, byte[] content, int bytesAvailables) {
		Message msg;
		String msgName = (String) messages.get(id);
		if(msgName == null)
			msg = new UnknownMessage();
		else {
			Class<? extends Message> cl = msgClasses.get(msgName);
			if(cl == null)
				msg = new UnhandledMessage();
			else
				try {
					msg = cl.newInstance();
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
		}
		msg.id = id;
		msg.lenofsize = lenofsize;
		msg.size = size;
		if(content != null) {
			if(content.length == size)		
				msg.content = new ByteArray(content);
			else
				msg.content = new ByteArray(content, size);
		}
		
		msg.contentBytesAvailables = bytesAvailables;
		msg.isComplete = bytesAvailables == size;
		return msg;
	}
	
	public abstract void serialize();
	public abstract void deserialize();
	
	public static String get(int id) {
		return (String) messages.get(id);
	}
	
	public static int get(String name) {
		Object id = messages.get(name);
		if(id == null)
			throw new FatalError("Unknown message name : \"" + name + "\".");
		return (int) id;
	}
	
	public static Class<Message> getClass(int id) { // TODO
		try {
			return (Class<Message>) Class.forName(get(id));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Class<Message> getClassByName(String msgName) {
		return msgClasses.get(msgName);
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
	
	public String getName() {
		return get(this.id);
	}

	public int getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
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
	
	public byte[] pack() {
		this.size = this.content.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		
		ByteArray buffer = new ByteArray(2 + this.lenofsize + this.size);
		buffer.writeShort(this.id << 2 | this.lenofsize);
		if(this.lenofsize == 0) return buffer.bytes();
		else if(this.lenofsize == 1)
			buffer.writeByte(this.size);
		else if(this.lenofsize == 2)
			buffer.writeShort(size);
		else {
			buffer.writeByte(this.size >> 16);
			buffer.writeShort(size & 65535);
		}
		if(this.content != null)
			buffer.writeBytes(this.content);
		return buffer.bytes();
	}
	
	public boolean isComplete() {
		return this.isComplete;
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
		this.content.writeBytes(buffer, additionSize);
		this.contentBytesAvailables += additionSize;
		this.isComplete = this.contentBytesAvailables == this.size;
		return additionSize;
	}
}