package main;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import controller.characters.Character;
import frames.Processor;
import messages.NetworkDataContainerMessage;
import messages.NetworkMessage;
import messages.synchronisation.BasicPingMessage;
import utilities.ByteArray;

public class NetworkInterface extends Thread {
	private Character character;
	private Reader reader;
	private Processor processor;
	private Connection.Client serverCo;
	private String gameServerIP;
	private List<NetworkMessage> acknowledgementList; // file des messages qui attendent d'être acquitté
	private Latency latency;
	
	public NetworkInterface(Character character, String login) {
		super(login + "/receiver");
		this.character = character;
		this.reader = new Reader();
		this.processor = new Processor(character, login);
		this.acknowledgementList = new Vector<NetworkMessage>();
		this.latency = new Latency();
	}
	
	@Override
	public void run() {
		this.character.log.p("Connection to authentification server, waiting response...");
		connectionToServer(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		if(!isInterrupted()) {
			if(this.gameServerIP == null)
				throw new FatalError("Deconnected from authentification server for unknown reason.");
			this.character.log.p("Deconnected from authentification server.");
			this.character.log.p("Connection to game server, waiting response...");
			connectionToServer(this.gameServerIP, Main.SERVER_PORT);
			if(!isInterrupted())
				throw new FatalError("Deconnected from game server for unknown reason.");
			this.character.log.p("Deconnected from game server.");
		}
		Log.info("Thread receiver of character with id = " + character.id + " terminated.");
		this.character.threadTerminated();
	}
	
	private void connectionToServer(String IP, int port) {
		// connexion au serveur et envoi d'un ping
		try {
			this.serverCo = new Connection.Client(IP, port);
		} catch(IOException e) {
			throw new FatalError(e);
		}
		this.character.log.p("Connected to server. Sending ping message...");
		BasicPingMessage BPM = new BasicPingMessage();
		BPM.quiet = true;
		send(BPM);
		
		// écoute du serveur et désérialisation des messages reçus
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		ByteArray array = new ByteArray();
		int bytesReceived;
		while(!isInterrupted())
			try {
				if((bytesReceived = this.serverCo.receive(buffer)) == -1)
					break;
				array.setArray(buffer, bytesReceived); // le buffer n'est pas complet, donc on le coupe
				processMsgStack(this.reader.processBuffer(array));
			} catch(IOException e) {
				if(!isInterrupted())
					throw new FatalError(e);
			}
	}
	
	private void processMsgStack(LinkedList<NetworkMessage> msgStack) {
		NetworkMessage msg;
		while((msg = msgStack.poll()) != null) {
			this.latency.updateLatency();
			this.character.log.p("r", msg);
			this.processor.processMessage(msg);
		}
	}
	
	public void processNetworkDataContainerMessage(NetworkDataContainerMessage msg) {
		processMsgStack(this.reader.processBuffer(msg.getContent()));
	}
	
	public Latency getLatency() {
		return this.latency;
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.gameServerIP = gameServerIP;
	}
	
	// on coupe la connexion côté client
	public void closeReceiver() {
		interrupt();
		if(this.serverCo != null)
			this.serverCo.close();
	}
	
	public void send(NetworkMessage msg) {
		if(isInterrupted())
			return;
		latency.setLatestSent();
		serverCo.send(msg.pack(character.id));
		/*
		if(msg.isAcknowledgable()) {
			msg.setSendingTime(new Date());
			if(!this.acknowledgementList.contains(msg))
				this.acknowledgementList.add(msg);
		}
		browseAcknowledgeList(); // TODO test en cours
		*/
		character.log.p("s", msg);
	}
	
	// appelée lors de la réception d'un BasicAckMessage
	public synchronized void acknowledgeMessage(int msgId) {
		for(NetworkMessage msg : this.acknowledgementList)
			if(msg.getId() == msgId) {
				this.acknowledgementList.remove(msg);
				return;
			}
		//throw new FatalError("Receive a acknowledgement for message with id = " + msgId + " but not found into the acknowledgement list.");
	}
		
	// parcourt la liste des messages en attente d'acquittement
	@SuppressWarnings("unused")
	private synchronized void browseAcknowledgeList() {
		Date now = new Date();
		for(NetworkMessage msg : this.acknowledgementList) {
			if(msg.getId() == 950) {
				if(now.getTime() - msg.getSendingTime().getTime() > 20000) {
					send(msg);
					Log.warn("Resending " + msg.getName() + ".");
				}
			}
			else if(now.getTime() - msg.getSendingTime().getTime() > 10000) { // 10 secondes
				send(msg);
				Log.warn("Resending " + msg.getName() + ".");
			}
		}
	}
}