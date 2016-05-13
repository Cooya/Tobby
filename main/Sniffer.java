package main;

import java.io.IOException;
import java.util.LinkedList;

import messages.Message;
import messages.connection.SelectedServerDataMessage;
import utilities.ByteArray;
import utilities.Processes;

public class Sniffer extends Thread {
	private static final String DOFUS_EXE = "Dofus.exe";
	private static Reader reader = new Reader();
	private static String gameServerAddress;
	private static Connection.Server clientCo;
	private static Connection.Client serverCo;
	private static boolean mustDeconnectClient = false;
	private static Log log;
	private static Thread serverCoThread;
	
	public Sniffer() {
		log = new Log("Sniffer", null);
		launch();
	}
	
	private void launch() {
		log.p("Waiting for " + DOFUS_EXE +" process...");
		while(!Processes.inProcess(DOFUS_EXE))
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		Processes.injectDLL(Main.LIB_PATH, "Dofus.exe");
		
		clientCo = new Connection.Server(Main.SERVER_PORT);
		log.p("Running sniffer server. Waiting Dofus client connection...");
		clientCo.waitClient();
		log.p("Dofus client connected.");
		try {
			serverCo = new Connection.Client(Main.AUTH_SERVER_IP, Main.SERVER_PORT);
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		log.p("Running sniffer client. Connection to Dofus server.");
		
		start();
		
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		ByteArray array = new ByteArray();
		int bytesReceived = 0;
		try {
			while((bytesReceived = clientCo.receive(buffer)) != -1) {
				array.setArray(buffer, bytesReceived);
				processMsgStack(reader.processBuffer(array), "s");
				serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		log.p("Waiting client reconnection...");
		clientCo.waitClient();
		log.p("Dofus client reconnected.");
		synchronized(this) {
			serverCoThread.notify();
		}
		
		try {
			while((bytesReceived = clientCo.receive(buffer)) != -1) {
				array.setArray(buffer, bytesReceived);
				processMsgStack(reader.processBuffer(array), "s");
				serverCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		log.p("Dofus client deconnected from sniffer server.");
		clientCo.close();
	}
	
	public void run() { // connexion au serveur officiel
		serverCoThread = this;
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		ByteArray array = new ByteArray();
		int bytesReceived = 0;
		
		while((bytesReceived = serverCo.receive(buffer)) != -1) {
			array.setArray(buffer, bytesReceived);
			if(processMsgStack(reader.processBuffer(array), "r"))
				clientCo.send(ByteArray.trimBuffer(buffer, bytesReceived));
			if(mustDeconnectClient)
				break;
		}
		clientCo.closeClient();
		log.p("Deconnection from Dofus client.");
		serverCo.close();
		log.p("Deconnected from authentification server.");
		
		synchronized(this) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(gameServerAddress != null) {
			log.p("Connecting to game server, waiting response...");
			try {
				serverCo = new Connection.Client(gameServerAddress, Main.SERVER_PORT);
			} catch(IOException e) {
				e.printStackTrace();
				return;
			}
			while((bytesReceived = serverCo.receive(buffer)) != -1) {
				array.setArray(buffer, bytesReceived);
				processMsgStack(reader.processBuffer(array), "r");
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
			//if(direction.equals("r"))
				//Reflection.displayMessageFields(msg);
			if(direction.equals("r") && msg.getId() == 42) {
				SelectedServerDataMessage SSDM = (SelectedServerDataMessage) msg;
				gameServerAddress = SSDM.address;
				mustDeconnectClient = true;
				if(msgStack.size() > 1)
					throw new FatalError("Little problem !");
				SSDM.address = Main.LOCALHOST;
				clientCo.send(SSDM.pack(0));
				return false;
			}
		}
		return true;
	}
}
