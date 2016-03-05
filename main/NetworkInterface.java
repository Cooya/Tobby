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
	protected Latency latency;
	protected Sender sender; 
	
	public NetworkInterface(Instance instance, String login) {
		super(login + "/receiver");
		this.instance = instance;
		this.reader = new Reader();
		this.sender = new Sender(login);
		this.latency = new Latency();
	}
	
	public void run() {
		this.instance.log.p("Connection to authentification server, waiting response...");
		connectionToServer(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		this.instance.log.p("Deconnected from authentification server.");
		
		if(!isInterrupted() && gameServerIP != null) {
			this.instance.log.p("Connection to game server, waiting response...");
			connectionToServer(gameServerIP, Main.SERVER_PORT);
			this.instance.log.p("Deconnected from game server.");
			if(!isInterrupted())
				throw new FatalError("Deconnected from game server.");
		}
		instance.log.p(Log.Status.CONSOLE, "Thread receiver of instance with id = " + instance.id + " terminated.");
	}
	
	private void connectionToServer(String IP, int port) {
		boolean canPing = true;
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		this.serverCo = new Connection.Client(IP, port);
		while(!isInterrupted()) {
			try {
				if((bytesReceived = this.serverCo.receive(buffer)) == -1)
					break;
				canPing = false;
			} catch(SocketTimeoutException e) {
				if(canPing) {
					BasicPingMessage ping = new BasicPingMessage();
					ping.serialize(true);
					instance.outPush(ping);
					this.instance.log.p("Sending a ping request to server.");
					canPing = false;
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
		private Sender(String login) {
			super(login + "/sender");
		}
		
		public synchronized void run() {
			Message msg;
			while (!isInterrupted()) {
				if((msg = instance.outPull()) != null) {
					//instance.log.p("Message pulled from the output queue.");
					latency.setLatestSent();
					serverCo.send(msg.makeRaw());
					instance.log.p("s", msg);
				}
				else
					try {
						//instance.log.p("None message to pull from the output queue.");
						wait();
					} catch(Exception e) {
						interrupt();
					}
			}
			instance.log.p(Log.Status.CONSOLE, "Thread sender of instance with id = " + instance.id + " terminated.");
		}
		
		public synchronized void wakeUp() {
			notify();
			//instance.log.p("Sender waking up.");
		}
	}
}