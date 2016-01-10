package main;

import java.io.InputStream;
import java.net.Socket;
import java.util.Stack;

import utilities.ByteArray;
import utilities.Log;
import messages.AuthentificationTicketMessage;
import messages.IdentificationFailedMessage;
import messages.IdentificationMessage;
import messages.ReceivedMessage;

public class Main {
	private static final String authServerIP = "213.248.126.39";
	private static final int port = 5555;
	private static String gameServerIP;
	
	public static void main(String[] args) {
		connection(authServerIP);
		Log.p("Deconnected from authentification server.");
		if(gameServerIP != null) {
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
			
			Log.p("Connecting to server, waiting response...");
			int bytesReceived = 0;
			while(true) {
				bytesReceived = is.read(buffer);
				if(bytesReceived == -1)
					break;
				System.out.println();
				System.out.println(bytesReceived + " bytes received.");
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
		AuthentificationTicketMessage ATM = null;
		while(!msgStack.empty()) {
			msg = msgStack.pop();
			switch(msg.getId()) {
				case 3 : Sender.getInstance().send(new IdentificationMessage(msg.getContent())); break;
				case 20 : new IdentificationFailedMessage(msg.getContent()); break;
				case 6469 : ATM = new AuthentificationTicketMessage(msg.getContent()); gameServerIP = ATM.getIP().toString(); break;
				case 101 : Sender.getInstance().send(ATM);
				default : return;
			}
		}
	}
}