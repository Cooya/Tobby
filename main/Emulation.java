package main;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import messages.NetworkMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.security.RawDataMessage;
import utilities.ByteArray;
import utilities.Processes;

public class Emulation {
	private static final boolean DEBUG = false;
	
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;
	private static Reader reader = new Reader();
	private static Process launcherProcess;
	private static Thread securityThread;

	public static void runLauncher() {
		if(!Processes.inProcess(Main.BYPASS_PROCESS_NAME))
			try {
				if(!Processes.fileExists(Main.BYPASS_PATH))
					throw new FatalError("Emulation launcher not found.");
				else {
					Log.info("Running emulation launcher.");
					launcherProcess = Runtime.getRuntime().exec(Main.BYPASS_PATH, null, new File(Main.CLIENT_PATH));
					while(!Processes.inProcess(Main.BYPASS_PROCESS_NAME))
						Thread.sleep(500);
				}
			} catch(Exception e) {
				throw new FatalError(e);
			}
		else
			Log.info("Emulation launcher already in process.");
		
		// on se connecte au launcher de manière permanente
		for(int i = 0; i < 10 ; ++i)
			try {
				launcherCo = new Connection.Client(Main.LOCALHOST, Main.LAUNCHER_PORT);
				break;
			} catch(IOException e) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		if(launcherCo == null)
			Main.exit("Impossible to connect to the launcher.");
		if(DEBUG)
			Log.info("Connected to emulation launcher.");
		
		// on lance le serveur d'émulation
		clientDofusCo = new Connection.Server(Main.SERVER_PORT);
		Log.info("Emulation server started."); 
		
		/*
		// thread qui "secoue" le client toutes les 10 minutes
		securityThread = new Thread() {
			@Override
			public synchronized void run() {
				while(true)
					try {
						wait(60000 * 10); // 10 minutes
						lock.lock();
						shakeUpClient();
						clientDofusCo.waitClient();
						clientDofusCo.closeClient();
						lock.unlock();
					} catch(InterruptedException e) {
						Log.info("Security thread terminated.");
						return;
					}
			}
		};
		securityThread.start();
		*/
	}
	
	public static void killLauncher() {
		// on coupe la connexion avec le launcher
		if(securityThread != null) {
			securityThread.interrupt();
			securityThread = null;
		}
		if(launcherCo != null) {
			launcherCo.close();
			launcherCo = null;
			Log.info("Connection to emulation launcher closed.");
		}
		
		// et on tue le processus via l'objet Process ou via une commande Windows
		if(launcherProcess != null)
			launcherProcess.destroy();
		else if(Processes.inProcess(Main.BYPASS_PROCESS_NAME))
			Processes.killProcess(Main.BYPASS_PROCESS_NAME);
		else { // processus inexistant
			Log.info("Emulation launcher process does not exist");
			return;
		}
		
		// on attend que le processus se termine
		while(Processes.inProcess(Main.BYPASS_PROCESS_NAME))
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				e.printStackTrace();
				return;
			}
		Log.info("Emulation launcher process killed.");
	}
	
	public synchronized static NetworkMessage emulateServer(String login, String password, HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM, int characterId) {
		// simulation de l'authentification
		int requestSize = 1 + 2 + login.length() + 2 + password.length();
		ByteArray array = new ByteArray(4 + requestSize);
		array.writeInt(requestSize);
		array.writeByte(1);
		array.writeUTF(login);
		array.writeUTF(password);
		if(DEBUG)
			Log.info("Sending credentials to emulation launcher.");
		launcherCo.send(array.bytes());
		
		// simulation du serveur officiel
		try {
			// attente de la connexion du client officiel
			if(DEBUG)
				Log.info("Waiting official client connection...");
			clientDofusCo.waitClient();
			if(DEBUG)
				Log.info("Official client connected.");
			
			// envoi du HelloConnectMessage
			clientDofusCo.send(HCM.pack(characterId));
			if(DEBUG)
				Log.info("HCM sent to official client");
			
			// réception de l'IdentificationMessage (aucune utilité)
			receiveDataFromLauncher();
			
			// envoi de l'IdentificationSuccessMessage et du RawDataMessage
			clientDofusCo.send(ISM.pack(characterId));
			clientDofusCo.send(RDM.pack(characterId));
			if(DEBUG)
				Log.info("IdentificationSuccessMessage and RawDataMessage sent to official client");
			
			// réception du CheckIntegrityMessage
			NetworkMessage CIM = receiveDataFromLauncher();
			if(CIM == null) { // réception du BasicPingMessage
				CIM = receiveDataFromLauncher();
				if(CIM == null)
					throw new Exception();
			}
			
			// demande de récupération de la fonction de hachage dans le client officiel
			if(DEBUG)
				Log.info("Asking hash function to emulation launcher.");
			byte[] bytes = {0, 0, 0, 2, 2, (byte) characterId}; // taille (int) + id + characterId
			launcherCo.send(bytes);
			
			// déconnexion du client officiel
			if(DEBUG)
				Log.info("Deconnection from official client.");
			clientDofusCo.closeClient();
			Thread.sleep(2000);
			return CIM;
		} catch(Exception e) {
			Log.info("Interaction with official client has failed, deconnection.");
			clientDofusCo.closeClient();
			throw new FatalError(e);
		}
	}
	
	public synchronized static void hashMessage(ByteArray msg, int characterId) {
		ByteArray array = new ByteArray(msg.getSize() + 2);
		array.writeInt(1 + 1 + msg.getSize());
		array.writeByte(3); 
		array.writeByte(characterId);
		array.writeBytes(msg);
		launcherCo.send(array.bytes());
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int size;
		while(true) {
			try {
				size = launcherCo.receive(buffer, 10000); // timeout de 10 secondes
				if(size <= 0)
					throw new FatalError("Connection lost with the emulation launcher.");
				array.setArray(buffer, size);
				if(size - 2 != array.readShort())
					throw new FatalError("Missing bytes !"); // erreur qui n'est encore jamais arrivée
				break;
			} catch(IOException e) {
				throw new FatalError(e);
			}
		}
		msg.setArray(array.bytesFromPos());
	}
	
	@SuppressWarnings("unused")
	private synchronized static void shakeUpClient() {
		if(launcherCo == null) {
			Log.err("Impossible to shake up the launcher, connection lost or not initialized.");
			return;
		}
		Log.info("Shaking up the client.");
		byte[] array = {0, 0, 0, 1, 4}; // nombre d'octets à envoyer (int) + identifiant de l'action à effectuer
		launcherCo.send(array);
	}
	
	private static NetworkMessage receiveDataFromLauncher() throws Exception {
		ByteArray array = new ByteArray(0);
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int bytesReceived = clientDofusCo.receive(buffer);
		if(bytesReceived == -1)
			throw new Exception();
		if(DEBUG)
			Log.info(bytesReceived + " bytes received from official client.");
		array.setArray(buffer, bytesReceived);
		LinkedList<NetworkMessage> msgStack = reader.processBuffer(array);
		NetworkMessage msg;
		while((msg = msgStack.poll()) != null) {
			if(DEBUG)
				Log.info(msg.toString());
			if(msg.getId() == 6372) // CheckIntegrityMessage
				return msg;
		}
		return null;
	}
}