package main;

import java.io.InputStream;
import java.net.Socket;
import java.util.Stack;

import utilities.ByteArray;
import utilities.Log;
import messages.AuthentificationTicketMessage;
import messages.IdentificationFailedMessage;
import messages.IdentificationMessage;
import messages.RawDataMessage;
import messages.ReceivedMessage;

public class Main {
	private static final String authServerIP = "213.248.126.39";
	private static final int port = 5555;
	private static String gameServerIP;
	private static AuthentificationTicketMessage ATM;
	
	public static void main(String[] args) {
		Log.p("Connecting to authentification server, waiting response...");
		connection(authServerIP);
		Log.p("Deconnected from authentification server.");
		if(gameServerIP != null) {
			Log.p("Connecting to game server, waiting response...");
			connection(gameServerIP);
			Log.p("Deconnected from game server.");
		}
	}
	
	public static void connection(String serverIP) {
		try {
			Socket socket = new Socket(serverIP, port);
			Sender.create(socket.getOutputStream());
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[8192];
			
			int bytesReceived = 0;
			while(true) {
				bytesReceived = is.read(buffer);
				if(bytesReceived == -1)
					break;
				Log.p(bytesReceived + " bytes received.");
				processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			
			is.close();
			socket.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void processMsgStack(Stack<ReceivedMessage> msgStack) {
		ReceivedMessage msg;
		while(!msgStack.empty()) {
			msg = msgStack.pop();
			Log.p("r", msg);
			switch(msg.getId()) {
				case 3 : Sender.getInstance().send(new IdentificationMessage(msg.getContent())); break;
				case 20 : new IdentificationFailedMessage(msg.getContent()); break;
				case 6469 : ATM = new AuthentificationTicketMessage(msg.getContent()); gameServerIP = new String(ATM.getIP()); break;
				case 101 : Sender.getInstance().send(ATM); ATM = null; break;
				case 6253 : new RawDataMessage(msg); break;
				default : return;
			}
		}
	}
}