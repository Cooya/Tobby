package main;

import java.util.LinkedList;
import java.util.Vector;

import frames.Frame;
import messages.Message;
import utilities.ByteArray;
import utilities.Log;

public class NetworkInterface extends Thread {
	private Reader reader;
	private Connection serverCo;
	private String gameServerIP;
	private Vector<Frame> workingFrames;
	
	public NetworkInterface(Vector<Frame> workingFrames) {
		this.workingFrames = workingFrames;
		this.reader = new Reader();
	}
	
	public void run() {
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		
		Log.p("Connection to authentification server, waiting response...");
		this.serverCo = new Connection.Client(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		while((bytesReceived = this.serverCo.receive(buffer)) != -1) {
			Log.p(bytesReceived + " bytes received from server.");
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
		}
		this.serverCo.close();
		Log.p("Deconnected from authentification server.");
		
		if(gameServerIP != null) {
			Log.p("Connection to game server, waiting response...");
			this.serverCo = new Connection.Client(gameServerIP, Main.SERVER_PORT);
			while((bytesReceived = this.serverCo.receive(buffer)) != -1) {
				Log.p(bytesReceived + " bytes received from server.");
				processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			this.serverCo.close();
			Log.p("Deconnected from game server.");
		}
	}
	
	public void processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			Log.p("r", msg);
			for(Frame frame : this.workingFrames)
				frame.processMessage(msg);
		}
	}
	
	public void sendMessage(Message msg) {
		serverCo.send(msg.makeRaw());
		Log.p("s", msg);
	}
	
	public void setGameServerIP(String gameServerIP) {
		this.gameServerIP = gameServerIP;
	}
}
