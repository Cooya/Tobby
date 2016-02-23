package main;

import java.net.SocketTimeoutException;
import java.util.LinkedList;

import messages.Message;
import messages.synchronisation.BasicPingMessage;
import utilities.ByteArray;

public class NetworkInterface extends Thread {
	private static final boolean DEBUG = false;
	private Instance instance;
	private Reader reader;
	private Connection.Client serverCo;
	private String gameServerIP;
	private boolean alreadyPing;
	protected Latency latency;
	protected Sender sender; 
	
	public NetworkInterface(Instance instance) {
		this.instance = instance;
		this.reader = new Reader();
		this.sender = new Sender();
		this.latency = new Latency();
		this.alreadyPing = false;
	}
	
	public void run() {
		this.instance.log.p("Connection to authentification server, waiting response...");
		connectionToServer(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		this.instance.log.p("Deconnected from authentification server.");
		
		this.alreadyPing = false;
		
		if(!isInterrupted() && gameServerIP != null) {
			this.instance.log.p("Connection to game server, waiting response...");
			connectionToServer(gameServerIP, Main.SERVER_PORT);
			this.instance.log.p("Deconnected from game server.");
		}
		instance.log.p(Log.Status.CONSOLE, "Thread receiver of instance with id = " + instance.id + " terminated.");
	}
	
	private void connectionToServer(String IP, int port) {
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		this.serverCo = new Connection.Client(IP, port);
		while(!isInterrupted()) {
			try {
				if((bytesReceived = this.serverCo.receive(buffer)) == -1)
					break;
			} catch(SocketTimeoutException e) {
				if(!this.alreadyPing) {
					BasicPingMessage ping = new BasicPingMessage();
					ping.serialize(true);
					instance.outPush(ping);
					this.instance.log.p("Sending a ping request to server.");
					this.alreadyPing = true;
				}
				continue;
			} catch(Exception e) {
				break;
			}
			if(DEBUG)
				this.instance.log.p(bytesReceived + " bytes received from server.");
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
		}
		this.serverCo.close();
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
			instance.log.p(Log.Status.CONSOLE, "Thread sender of instance with id = " + instance.id + " terminated.");
		}
		
		public synchronized void wakeUp() {
			notify();
		}
	}
}