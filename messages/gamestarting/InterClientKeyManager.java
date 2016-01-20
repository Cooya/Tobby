package messages.gamestarting;

import java.util.Date;
import java.util.Vector;

public class InterClientKeyManager {
	/*
	private static final char[] hex_chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	private static final int KEY_SIZE = 21;
	private static final int MAX_CLIENTS = 50;
	private static final String CONNECTION_NAME= "_dofusClient_RELEASE#";
	*/
	private static InterClientKeyManager instance;
	private static String uidKey = "E4jtiFC4zf5lIQAPrB";
	
    //private var _sendingLc:LocalConnection;
    //private var _receivingLc:LocalConnection;
    private String key;
    private double keyTimestamp;
    private boolean initKey;
    private boolean connected;
    private String connectionName;
    private int currentClientId;
    private Vector<Integer> clientsIds;
    private Vector<Object> clientsKeys;
    private int numAskedClients;
    
    private String flashKey; // InterClientManager
	
	private InterClientKeyManager() {
		
	}
	
	public static InterClientKeyManager getInstance() {
		if(instance == null)
			instance = new InterClientKeyManager();
		return instance;
	}
	
	/*
	private static String getRandomFlashKey() {
		String str = "";
		for(int i = 0; i < KEY_SIZE - 4; ++i)
			str += getRandomChar();
		return str + checksum(str);
	}
	
	private static char getRandomChar() {
		double db = Math.ceil(Math.random() * 100);
		if(db <= 40)
			return (char) (Math.floor((Math.random() * 26)) + 65);
		if(db <= 80)
			return (char) (Math.floor((Math.random() * 26)) + 97);
		return (char) (Math.floor((Math.random() * 10)) + 48);
	}
	
	private static char checksum(String str) {
		int nb = 0;
		for(int i = 0; i < str.length(); ++i)
			nb += str.charAt(i) % 16;
		return hex_chars[nb % 16];
	}
	*/
	
    public void getKey() {
        this.initKey = true;
        this.currentClientId = 0;
        this.clientsIds = new Vector<Integer>();
        this.clientsKeys = new Vector<Object>();
        //pingNext();
        
        saveKey(uidKey); // raccourci
    }
    
    private void saveKey(String str) {
    	String flashKey;
    	Date var3 = new Date();
    	this.key = str;
    	this.keyTimestamp = var3.getTime();
    	flashKey = str + "#01";
    	this.flashKey = flashKey;
    }
    
    public String getFlashKey() {
    	return this.flashKey;
    }
}
