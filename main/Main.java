package main;

import java.io.InputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;

import utilities.ByteArray;
import utilities.Log;
import messages.AuthentificationTicketMessage;
import messages.CheckIntegrityMessage;
import messages.IdentificationFailedMessage;
import messages.IdentificationMessage;
import messages.Message;
import messages.ReceivedMessage;
import messages.ServerSelectionMessage;

public class Main {
	private static final String authServerIP = "213.248.126.39";
	private static final int port = 5555;
	private static Hashtable<Integer, Message> sendMsgList = new Hashtable<Integer, Message>();
	
	public static void main(String[] args) {
		Log.p("Connecting to authentification server, waiting response...");
		connection(authServerIP);
		Log.p("Deconnected from authentification server.");
		
		String gameServerIP = ((AuthentificationTicketMessage) sendMsgList.get(new Integer(110))).getIP();
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
	
	public static void processMsgStack(LinkedList<ReceivedMessage> msgStack) {
		ReceivedMessage msg;
		while((msg = msgStack.poll()) != null) {
			Log.p("r", msg);
			switch(msg.getId()) {
				case 3 : 
					Message IM = new IdentificationMessage(msg.getContent());
					Sender.getInstance().send(IM);
					sendMsgList.put(new Integer(IM.getId()), IM);
					break;
				case 20 :
					new IdentificationFailedMessage(msg.getContent()); 
					break;
				case 30 : 
					Message SSM = new ServerSelectionMessage();
					Sender.getInstance().send(SSM);
					sendMsgList.put(new Integer(SSM.getId()), SSM);
					break;
				case 42 : 
					Message ATM = new AuthentificationTicketMessage(msg.getContent());
					sendMsgList.put(new Integer(ATM.getId()), ATM);
					break;
				case 101 : 
					Sender.getInstance().send(sendMsgList.get(new Integer(110)));
					break;
				case 6253 :
					byte[] decryptedPublicKey = ((IdentificationMessage) sendMsgList.get(new Integer(4))).getDecryptedPublicKey();
					String gameServerTicket = ((AuthentificationTicketMessage) sendMsgList.get(new Integer(110))).getTicket();
					Message CIM = new CheckIntegrityMessage(decryptedPublicKey, gameServerTicket, msg.getContent());
					Sender.getInstance().send(CIM);
					sendMsgList.put(new Integer(CIM.getId()), CIM);
					break;
			}
		}
	}
}