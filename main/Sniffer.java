package main;

import java.util.LinkedList;

import messages.Message;
import messages.connection.SelectedServerDataMessage;
import utilities.ByteArray;
import utilities.Log;
import utilities.Processes;

public class Sniffer extends Thread {
	private static final String dofusExe = "Dofus.exe";
	private static String gameServerAddress;
	private static Connection.Server clientCo;
	private static Connection.Client serverCo;
	private static boolean mustDeconnectClient = false;
	
	public Sniffer() {
		
	}
	
	public void launch() {
		Log.p("Waiting for " + dofusExe +" process...");
		while(!Processes.inProcess(dofusExe))
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		Processes.injectDLL(Main.dllLocation, dofusExe);
		
		clientCo = new Connection.Server(Main.serverPort);
		Log.p("Running sniffer server. Waiting Dofus client connection...");
		clientCo.waitClient();
		Log.p("Dofus client connected.");
		serverCo = new Connection.Client(Main.authServerIP, Main.serverPort);
		Log.p("Running sniffer client. Connection to Dofus server.");
		
		start();
		
		byte[] buffer = new byte[Main.BUFFER_SIZE];
		int bytesReceived = 0;
		
		while((bytesReceived = clientCo.receive(buffer)) != -1) {
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)), "s");
			serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
		}
		
		clientCo.waitClient();
		Log.p("Dofus client reconnected.");
		
		while((bytesReceived = clientCo.receive(buffer)) != -1) {
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)), "s");
			serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
		}
		Log.p("Dofus client deconnected from sniffer server.");
		clientCo.close();
	}
	
	public void run() { // connexion au serveur officiel
		byte[] buffer = new byte[Main.BUFFER_SIZE];
		int bytesReceived = 0;
		
		while((bytesReceived = serverCo.receive(buffer)) != -1) {
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)), "r");
			clientCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			if(mustDeconnectClient)
				break;
		}
		clientCo.close();
		Log.p("Deconnection from Dofus client.");
		serverCo.close();
		Log.p("Deconnected from authentification server.");
		
		if(gameServerAddress != null) {
			Log.p("Connecting to game server, waiting response...");
			serverCo = new Connection.Client(gameServerAddress, Main.serverPort);
			while((bytesReceived = serverCo.receive(buffer)) != -1) {
				processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)), "r");
				clientCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			}
			serverCo.close();
			Log.p("Deconnected from game server.");
		}
	}
	
	public static void processMsgStack(LinkedList<Message> msgStack, String direction) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			Log.p(direction, msg);
			if(direction == "r" && msg.getId() == 42) {
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				gameServerAddress = SSDM.getAddress();
				mustDeconnectClient = true;
				System.out.println(gameServerAddress);
			}
		}
	}
}
