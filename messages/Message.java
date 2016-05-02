package messages;

import gui.Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import main.Emulation;
import main.FatalError;
import main.Log;
import utilities.BiMap;
import utilities.ByteArray;
import utilities.Reflection;

@SuppressWarnings("unchecked")
public abstract class Message {
	private static final String MESSAGES_FILEPATH = "Ressources/messages.txt";
	private static final BiMap<Integer, String> messages = new BiMap<Integer, String>(Integer.class, String.class);
	private static final Map<Integer, Object> acknowledgementExceptions = new HashMap<Integer, Object>();
	private static final Map<String, Class<Message>> msgClasses = new HashMap<String, Class<Message>>();
	private static final Map<String, Object> hashedMessages = new HashMap<String, Object>();
	
	static {
		BufferedReader buffer;
		String[] splitLine;
		String line;
		
		// récupération de la liste des messages (id + nom) depuis le fichier "messages.txt"
		try {
			buffer = new BufferedReader(new FileReader(MESSAGES_FILEPATH));
			line = buffer.readLine();
			while(line != null) {
				splitLine = line.split(" ");
				messages.put(Integer.parseInt(splitLine[0]), splitLine[1]);
				line = buffer.readLine();
			}
			buffer.close();
		} catch(Exception e) {
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
		
		// liste des messages non acquittables
		acknowledgementExceptions.put(4, null); // IdentificationMessage
		acknowledgementExceptions.put(40, null); // ServerSelectionMessage
		acknowledgementExceptions.put(182, null); // BasicPingMessage
		
		// listage de tous les messages nécessitant un hash à l'envoi
		hashedMessages.put("GameActionFightCastRequestMessage", null);
		hashedMessages.put("BasicLatencyStatsMessage", null);
		hashedMessages.put("ChatClientMultiMessage", null);
		hashedMessages.put("ChatClientMultiWithObjectMessage", null);
		hashedMessages.put("ChatClientPrivateMessage", null);
		hashedMessages.put("ChatClientPrivateWithObjectMessage", null);
		hashedMessages.put("GameCautiousMapMovementRequestMessage", null);
		hashedMessages.put("GameMapMovementRequestMessage", null);
		hashedMessages.put("GameRolePlayPlayerFightRequestMessage", null);
		hashedMessages.put("NpcGenericActionRequestMessage", null);
		hashedMessages.put("InteractiveUseRequestMessage", null);
		hashedMessages.put("ExchangePlayerMultiCraftRequestMessage", null);
		hashedMessages.put("ExchangePlayerRequestMessage", null);
		hashedMessages.put("ClientKeyMessage", null);
	}
	
	private int id;
	private String name;
	private int lenofsize;
	private int size;
	private Date sendingTime;
	protected ByteArray content;
	
	private int contentBytesAvailables; // nombre d'octets du contenu acquis
	private boolean isComplete;
	
	public abstract void serialize();
	public abstract void deserialize();
	
	// constructeur pour les messages non gérés (UnhandledMessage)
	public Message(String msgName) {
		this.name = msgName;
	}
	
	// constructeur pour les messages gérés
	public Message() {
		this.name = getClass().getSimpleName();
	}
	
	// sorte de méthode "factory" pour les messages reçus
	public static Message create(int id, int lenofsize, int size, byte[] content, int bytesAvailables) {
		Message msg;
		String msgName = (String) messages.get(id);
		if(msgName == null) { // message inconnu
			Log.warn("Unknown message with id = " + id + ".");
			msg = new UnhandledMessage(null);
		}
		else {
			Class<? extends Message> cl = msgClasses.get(msgName);
			if(cl == null)
				msg = new UnhandledMessage(msgName);
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
				msg.content = new ByteArray(content); // complet
			else
				msg.content = new ByteArray(content, size); // incomplet
		}
		
		msg.contentBytesAvailables = bytesAvailables;
		msg.isComplete = bytesAvailables == size;
		return msg;
	}
	
	public byte[] pack(int characterId) {
		this.id = get(this.name);
		this.content = new ByteArray();
		serialize(); // unique appel de la fonction "serialize()"
		if(hashedMessages.containsKey(this.name))
			Emulation.hashMessage(this.content, characterId);
		this.size = this.content.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		
		ByteArray msg = new ByteArray(2 + this.lenofsize + this.size);
		msg.writeShort(this.id << 2 | this.lenofsize);
		if(this.lenofsize == 0) return msg.bytes();
		else if(this.lenofsize == 1)
			msg.writeByte(this.size);
		else if(this.lenofsize == 2)
			msg.writeShort(size);
		else {
			msg.writeByte(this.size >> 16);
			msg.writeShort(size & 65535);
		}
		msg.writeBytes(this.content);
		return msg.bytes();
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
		if(this.isComplete) // si le message est complet alors on reset la position du curseur
			this.content.setPos(0);
		return additionSize;
	}
	
	public void setPosToMax() {
		this.content.setPos(this.contentBytesAvailables);
	}
	
	public void setSendingTime(Date date) {
		this.sendingTime = date;
	}
	
	public static String get(int id) {
		return (String) messages.get(id);
	}
	
	public static int get(String name) {
		Object id = messages.get(name);
		if(id == null)
			throw new FatalError("Unknown message name : \"" + name + "\".");
		return (int) id;
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
		return this.name;
	}

	public int getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public int getTotalSize() {
		return this.contentBytesAvailables + 2 + this.lenofsize;
	}
	
	public Date getSendingTime() {
		return this.sendingTime;
	}
	
	public static boolean isAcknowledgable(int msgId) {
		return !acknowledgementExceptions.containsKey(msgId);
	}
	
	public boolean isAcknowledgable() {
		return !acknowledgementExceptions.containsKey(this.id);
	}
	
	private static short computeLenOfSize(int size) {
	    if(size > 65535)
	        return 3;
	    else if(size > 255)
	        return 2;
	    else if(size > 0)
	        return 1;
	    else
	        return 0;
	}
}