package main;

import java.util.Hashtable;
import java.util.LinkedList;

import utilities.ByteArray;
import utilities.Log;
import messages.AuthentificationTicketMessage;
import messages.CheckIntegrityMessage;
import messages.HelloConnectMessage;
import messages.IdentificationFailedMessage;
import messages.IdentificationMessage;
import messages.IdentificationSuccessMessage;
import messages.Message;
import messages.RawDataMessage;
import messages.ReceivedMessage;
import messages.SelectedServerDataMessage;
import messages.ServerSelectionMessage;

public class Main {
	private static final String APP_PATH = System.getProperty("user.dir");
	private static final int BUFFER_SIZE = 8192;
	private static final String authServerIP = "213.248.126.39";
	private static final int port = 5555;
	private static Connection serverCo = null; // temporaire bien sûr
	private static Hashtable<Integer, Message> sentMsgList = new Hashtable<Integer, Message>();
	private static Hashtable<Integer, Message> receivedMsgList = new Hashtable<Integer, Message>(); 
	
	public static void main(String[] args) {
		runASLauncher();
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesReceived = 0;
		
		Log.p("Connecting to authentification server, waiting response...");
		serverCo = new Connection.Client(authServerIP, port);
		while((bytesReceived = serverCo.receive(buffer)) != -1) {
			Log.p(bytesReceived + " bytes received from server.");
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
		}
		serverCo.close();
		Log.p("Deconnected from authentification server.");
		
		String gameServerIP = ((SelectedServerDataMessage) receivedMsgList.get(SelectedServerDataMessage.ID)).getAddress();
		if(gameServerIP != null) {
			Log.p("Connecting to game server, waiting response...");
			serverCo = new Connection.Client(gameServerIP, port);
			while((bytesReceived = serverCo.receive(buffer)) != -1) {
				Log.p(bytesReceived + " bytes received from server.");
				processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			serverCo.close();
			Log.p("Deconnected from game server.");
		}
	}
	
	public static void processMsgStack(LinkedList<ReceivedMessage> msgStack) {
		ReceivedMessage msg;
		while((msg = msgStack.poll()) != null) {
			Log.p("r", msg);
			switch(msg.getId()) {
				case 3 :
					HelloConnectMessage HCM = new HelloConnectMessage(msg);
					receivedMsgList.put(new Integer(3), HCM);
					Message IM = new IdentificationMessage(HCM);
					sendMessage(IM);
					break;
				case 22 :
					IdentificationSuccessMessage ISM = new IdentificationSuccessMessage(msg);
					receivedMsgList.put(new Integer(ISM.getId()), ISM);
					break;
				case 20 :
					new IdentificationFailedMessage(msg.getContent()); 
					break;
				case 30 : 
					Message SSM = new ServerSelectionMessage();
					sendMessage(SSM);
					break;
				case 42 : 
					SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
					receivedMsgList.put(new Integer(SSDM.getId()), SSDM);
					break;
				case 101 :
					SSDM = (SelectedServerDataMessage) receivedMsgList.get(new Integer(SelectedServerDataMessage.ID));
					Message ATM = new AuthentificationTicketMessage(SSDM);
					sendMessage(ATM);
					break;
				case 6253 :
					HCM = (HelloConnectMessage) receivedMsgList.get(new Integer(3));
					ISM = (IdentificationSuccessMessage) receivedMsgList.get(new Integer(22));
					RawDataMessage RDM = new RawDataMessage(msg);
					sendCredentials();
					createServerEmulation(HCM, ISM, RDM);
					break;
				case 6372 : 
					Message CIM = new CheckIntegrityMessage(msg);
					sendMessage(CIM);
					break;
			}
		}
	}
	
	private static void sendMessage(Message msg) {
		serverCo.send(msg.makeRaw());
		Log.p("s", msg);
		sentMsgList.put(new Integer(msg.getId()), msg);
	}
	
	private static void createServerEmulation(HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM) {
		try {
			Connection.Server clientCo = new Connection.Server(port);
			Log.p("Running emulation server. Waiting Dofus client connection...");
			
			clientCo.waitClient();
			Log.p("Dofus client connected.");
			
			clientCo.send(HCM.makeRaw());
			Log.p("HCM sent to Dofus client");
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesReceived = 0;
			bytesReceived = clientCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			
			clientCo.send(ISM.makeRaw());
			Log.p("ISM sent to Dofus client");
			clientCo.send(RDM.makeRaw());
			Log.p("RDM sent to Dofus client");
			
			bytesReceived = clientCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			clientCo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sendCredentials() {
		Connection launcherCo = new Connection.Client("127.0.0.1", 5554);
		byte[] buffer = new byte[1]; // bool d'injection
		launcherCo.receive(buffer);
		if(buffer[0] == 0)
			try {
				Log.p("DLL Injection.");
				Runtime.getRuntime().exec(APP_PATH + "/Ressources/DLLInjector/Injector.exe");
			} catch (Exception e) {
				e.printStackTrace();
			}
		ByteArray array = new ByteArray();
		array.writeInt(2 + 11 + 2 + 10);
		array.writeUTF("maxlebgdu93");
		array.writeUTF("represente");
		Log.p("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
		launcherCo.close();	
	}
	
	private static void runASLauncher() {
		try {
			Log.p("Running AS launcher.");
			Runtime.getRuntime().exec("C:/PROGRA~2/AdobeAIRSDK/bin/adl " + APP_PATH + "/Ressources/Antibot/application.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

	/*
ByteArray buffer = new ByteArray();
	buffer.writeInt(publicKey.length + ticket.length + 4); // + 2 shorts
	buffer.writeShort((short) publicKey.length);
for(int i = 0; i < publicKey.length; ++i)
	buffer.writeByte((byte) publicKey[i]);
buffer.writeShort((short) ticket.length);
for(int i = 0; i < ticket.length; ++i)
	buffer.writeByte((byte) ticket[i]);
buffer.writeInt(2 + RDM.getLenOfSize() + RDM.getSize());
buffer.writeBytes(RDM.getRaw());
*/