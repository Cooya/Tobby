package main;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import controller.characters.Character;
import messages.Message;
import messages.synchronisation.BasicPingMessage;
import utilities.ByteArray;

public class NetworkInterface extends Thread {
	private Character character;
	private Reader reader;
	private Connection.Client serverCo;
	private String gameServerIP;
	public Sender sender;
	public Latency latency;
	
	public NetworkInterface(Character character, String login) {
		super(login + "/receiver");
		this.character = character;
		this.reader = new Reader();
		this.sender = new Sender(login);
		this.latency = new Latency();
	}
	
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
		Controller.getInstance().threadTerminated();
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
		while(!isInterrupted()) {
			if((bytesReceived = this.serverCo.receive(buffer)) == -1)
				break;
			array.setArray(buffer, bytesReceived); // le buffer n'est pas complet, donc on le coupe
			processMsgStack(this.reader.processBuffer(array));
		}
		this.serverCo.close();
	}
	
	private void processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			this.latency.updateLatency();
			this.character.log.p("r", msg);
			this.character.processor.incomingMessage(msg);
		}
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
	
	public void send(Message msg) {
		this.sender.output.add(msg);
		synchronized(this.sender) {
			this.sender.notify();
		}
	}
	
	// appelée lors de la réception d'un BasicAckMessage
	public void acknowledgeMessage(int msgId) {
		this.sender.listLock.lock();
		for(Message msg : this.sender.acknowledgementList)
			if(msg.getId() == msgId) {
				this.sender.acknowledgementList.remove(msg);
				this.sender.listLock.unlock();
				return;
			}
		this.sender.listLock.unlock();
		//throw new FatalError("Receive a acknowledgement for message with id = " + msgId + " but not found into the acknowledgement list.");
	}
	
	public class Sender extends Thread {
		private ConcurrentLinkedQueue<Message> output; // file des messages qui doivent être envoyé
		private List<Message> acknowledgementList; // file des messages qui attendent d'être acquitté
		private ReentrantLock listLock;
		
		private Sender(String login) {
			super(login + "/sender");
			this.output = new ConcurrentLinkedQueue<Message>();
			this.acknowledgementList = new Vector<Message>();
			this.listLock = new ReentrantLock();
		}
		
		private void browseAcknowledgeList() {
			Date now = new Date();
			this.listLock.lock();
			for(Message msg : this.acknowledgementList)
				if(now.getTime() - msg.getSendingTime().getTime() > 10000) { // 10 secondes
					send(msg);
					Log.warn("Resending " + msg.getName() + ".");
				}
			this.listLock.unlock();
		}
		
		private void addMessageToAcknowledgeList(Message msg) {
			msg.setSendingTime(new Date());
			this.listLock.lock();
			if(!this.acknowledgementList.contains(msg))
				this.acknowledgementList.add(msg);
			this.listLock.unlock();
		}
	
		public synchronized void run() {
			Message msg;
			while (!isInterrupted()) {
				if((msg = this.output.poll()) != null) {
					latency.setLatestSent();
					serverCo.send(msg.pack(character.id));
					if(msg.isAcknowledgable())
						addMessageToAcknowledgeList(msg);
					character.log.p("s", msg);
				}
				else
					try {
						wait(5000);
						browseAcknowledgeList();
					} catch(InterruptedException e) {
						interrupt();
					}
			}
			Log.info("Thread sender of character with id = " + character.id + " terminated.");
			Controller.getInstance().threadTerminated();
		}
	}
}