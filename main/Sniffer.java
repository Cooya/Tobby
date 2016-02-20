package main;

import gui.CharacterFrame;

import java.util.LinkedList;

import messages.Message;
import messages.connection.SelectedServerDataMessage;
import utilities.ByteArray;
import utilities.Log;
import utilities.Processes;

public class Sniffer extends Thread {
	private static Reader reader = new Reader();
	private static final String dofusExe = "Dofus.exe";
	private static String gameServerAddress;
	private static Connection.Server clientCo;
	private static Connection.Client serverCo;
	private static boolean mustDeconnectClient = false;
	private static Log log;
	
	public Sniffer(CharacterFrame graphicalFrame) {
		launch();
		log = new Log("Sniffer", graphicalFrame);
	}
	
	private void launch() {
		log.p("Waiting for " + dofusExe +" process...");
		while(!Processes.inProcess(dofusExe))
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		Processes.injectDLL(Main.DLL_LOCATION, dofusExe);
		
		clientCo = new Connection.Server(Main.SERVER_PORT);
		log.p("Running sniffer server. Waiting Dofus client connection...");
		clientCo.waitClient();
		log.p("Dofus client connected.");
		serverCo = new Connection.Client(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		log.p("Running sniffer client. Connection to Dofus server.");
		
		start();
		
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		
		while((bytesReceived = clientCo.receive(buffer)) != -1) {
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)), "s");
			serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
		}
		
		log.p("Waiting client reconnection...");
		clientCo.waitClient();
		log.p("Dofus client reconnected.");
		
		while((bytesReceived = clientCo.receive(buffer)) != -1) {
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)), "s");
			serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
		}
		log.p("Dofus client deconnected from sniffer server.");
		clientCo.close();
	}
	
	public void run() { // connexion au serveur officiel
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int bytesReceived = 0;
		
		while((bytesReceived = serverCo.receive(buffer)) != -1) {
			if(processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)), "r"))
				clientCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			if(mustDeconnectClient)
				break;
		}
		clientCo.closeClient();
		log.p("Deconnection from Dofus client.");
		serverCo.close();
		log.p("Deconnected from authentification server.");
		
		if(gameServerAddress != null) {
			log.p("Connecting to game server, waiting response...");
			serverCo = new Connection.Client(gameServerAddress, Main.SERVER_PORT);
			while((bytesReceived = serverCo.receive(buffer)) != -1) {
				processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)), "r");
				clientCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			}
			serverCo.close();
			log.p("Deconnected from game server.");
		}
	}
	
	public static boolean processMsgStack(LinkedList<Message> msgStack, String direction) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			log.p(direction, msg);
			if(direction == "r" && msg.getId() == 42) {
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				gameServerAddress = SSDM.address;
				mustDeconnectClient = true;
				if(msgStack.size() > 1)
					throw new Error("Little problem !");
				SSDM.serialize("127.0.0.1");
				clientCo.send(SSDM.makeRaw());
				return false;
			}
		}
		return true;
	}
}
