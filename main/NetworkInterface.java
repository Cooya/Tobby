package main;

import java.util.LinkedList;

import messages.Message;
import utilities.ByteArray;

public class NetworkInterface extends Thread {
	private static final boolean DEBUG = false;
	private Instance instance;
	private Reader reader;
	private Connection serverCo;
	private String gameServerIP;
	protected Latency latency;
	protected Sender sender; 
	
	public NetworkInterface(Instance instance) {
		this.instance = instance;
		this.reader = new Reader();
		this.sender = new Sender();
		this.latency = new Latency();
	}
	
	public void run() {
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		
		this.instance.log.p("Connection to authentification server, waiting response...");
		this.serverCo = new Connection.Client(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		while((bytesReceived = this.serverCo.receive(buffer)) != -1) {
			if(DEBUG)
				this.instance.log.p(bytesReceived + " bytes received from server.");
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
		}
		this.serverCo.close();
		this.instance.log.p("Deconnected from authentification server.");
		
		if(gameServerIP != null) {
			this.instance.log.p("Connection to game server, waiting response...");
			this.serverCo = new Connection.Client(gameServerIP, Main.SERVER_PORT);
			while((bytesReceived = this.serverCo.receive(buffer)) != -1) {
				if(DEBUG)
					this.instance.log.p(bytesReceived + " bytes received from server.");
				processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			this.serverCo.close();
			this.instance.log.p("Deconnected from game server.");
		}
	}
	
	public void processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			latency.updateLatency();
			this.instance.log.p("r", msg);
			instance.inPush(msg);
		}
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.gameServerIP = gameServerIP;
	}
	
	class Sender extends Thread {
		public synchronized void run() {
			Message msg;
			while (!isInterrupted()) {
				if((msg = instance.outPull()) != null) {
					latency.setLatestSent();
					serverCo.send(msg.makeRaw());
					instance.log.p("s", msg);
				}
				else
					try {
						wait();
					} catch(Exception e) {
						Thread.currentThread().interrupt();
					}
			}
		}
		
		public synchronized void wakeUp() {
			notify();
		}
	}
}