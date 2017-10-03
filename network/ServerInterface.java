package network;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

import main.ConsoleInterface;
import main.FatalError;
import main.Log;
import main.Main;
import messages.NetworkMessage;
import messages.UserCommandMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.security.CheckIntegrityMessage;
import messages.security.RawDataMessage;
import utilities.ByteArray;
import utilities.Processes;

//serveur d'émulation et de réception des commandes utilisateur
public class ServerInterface extends Thread {
	private static final boolean DEBUG = false;
	private static ServerInterface self;
	private static int characterId;
	private static HelloConnectMessage HCM;
	private static IdentificationSuccessMessage ISM;
	private static RawDataMessage RDM;
	private static CheckIntegrityMessage CIM;
	
	private ServerSocket server;
	private Client patchedClient;
	private Process patchedClientProcess;
	
	private ServerInterface() {
		super("ServerInterface");
		try {
			this.server = new ServerSocket(Main.SERVER_PORT);
		} catch(IOException e) {
			throw new FatalError(e);
		}
		start();
		Log.info("Background server started.");
	}
	
	public static ServerInterface getInstance() {
		if(self == null)
			self = new ServerInterface();
		return self;
	}

	public void runPatchedClient() {
		if(!Processes.inProcess(Main.BYPASS_PROCESS_NAME))
			try {
				if(!Processes.fileExists(Main.BYPASS_PATH))
					throw new FatalError("Patched client not found.");
				else {
					Log.info("Running patched client.");
					this.patchedClientProcess = Runtime.getRuntime().exec(Main.BYPASS_PATH, null, new File(Main.CLIENT_PATH));
					while(!Processes.inProcess(Main.BYPASS_PROCESS_NAME))
						Thread.sleep(500);
				}
			} catch(Exception e) {
				throw new FatalError(e);
			}
		else
			Log.info("Patched client already in process.");
		
		// on se connecte au client modifié de manière permanente (10 tentatives de connexion)
		for(int i = 0; i < 10 ; ++i)
			try {
				this.patchedClient = new Client(Main.LOCALHOST, Main.LAUNCHER_PORT);
				break;
			} catch(IOException e) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		if(this.patchedClient == null)
			Main.exit("Impossible to connect to the client patched.");
		if(DEBUG)
			Log.info("Connected to client patched.");
	}
	
	public void exitClientPatched() {
		// on coupe la connexion avec le launcher
		if(this.patchedClient != null) {
			this.patchedClient.close();
			this.patchedClient = null;
			Log.info("Connection to patched client closed.");
		}
		
		// et on tue le processus via l'objet Process ou via une commande
		if(this.patchedClientProcess != null)
			this.patchedClientProcess.destroy();
		else if(Processes.inProcess(Main.BYPASS_PROCESS_NAME))
			Processes.killProcess(Main.BYPASS_PROCESS_NAME);
		else { // processus inexistant
			Log.info("Patched client process does not exist");
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
		Log.info("Patched client process killed.");
	}
	
	public synchronized NetworkMessage emulateServer(String login, String password, HelloConnectMessage _HCM, IdentificationSuccessMessage _ISM, RawDataMessage _RDM, int _characterId) {
		characterId = _characterId;
		HCM = _HCM;
		ISM = _ISM;
		RDM = _RDM;
		
		// envoie d'une requête d'authentification
		int requestSize = 1 + 2 + login.length() + 2 + password.length();
		ByteArray array = new ByteArray(4 + requestSize);
		array.writeInt(requestSize);
		array.writeByte(1);
		array.writeUTF(login);
		array.writeUTF(password);
		if(DEBUG)
			Log.info("Sending credentials to patched client.");
		try {
			this.patchedClient.send(array.bytes());
			wait();
			Thread.sleep(2000);
			
			// demande de récupération de la fonction de hachage dans le client modifié
			if(DEBUG)
				Log.info("Asking hash function to patched client.");
			byte[] bytes = {0, 0, 0, 2, 2, (byte) characterId}; // taille (int) + id + characterId
			this.patchedClient.send(bytes);
		} catch(Exception e) {
			throw new FatalError(e);
		}
		return CIM;
	}
	
	public synchronized void hashMessage(ByteArray msg, int characterId) {
		ByteArray array = new ByteArray(msg.getSize() + 2);
		array.writeInt(1 + 1 + msg.getSize());
		array.writeByte(3); 
		array.writeByte(characterId);
		array.writeBytes(msg);
		try {
			this.patchedClient.send(array.bytes());
		} catch(IOException e) {
			throw new FatalError(e);
		}
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int size;
		while(true) {
			try {
				size = this.patchedClient.receive(buffer, 10000); // timeout de 10 secondes
				if(size <= 0)
					throw new FatalError("Connection lost with the patched client.");
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
	
	@Override
	public void run() {
		while(true) {
			try {
				new ServerClient(new Client(this.server.accept()));
				if(DEBUG)
					Log.info("Client connected on background server.");
			} catch(IOException e) {
				throw new FatalError(e);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private synchronized void shakeUpClient() {
		if(this.patchedClient == null) {
			Log.err("Impossible to shake up the patched client, connection lost or not initialized.");
			return;
		}
		Log.info("Shaking up the client.");
		byte[] array = {0, 0, 0, 1, 4}; // nombre d'octets à envoyer (int) + identifiant de l'action à effectuer
		try {
			this.patchedClient.send(array);
		} catch(IOException e) {
			throw new FatalError(e);
		}
	}
	
	private static class ServerClient extends Thread {
		private Client client;
		private Reader reader;
		
		private ServerClient(Client client) {
			this.client = client;
			this.reader = new Reader();
			
			start();
		}
		
		@Override
		public void run() {
			if(HCM != null)
				try {
					this.client.send(HCM.pack(characterId));
					if(DEBUG)
						Log.info("HCM sent to patched client.");
				} catch(IOException e) {
					Log.info("Client disconnected from background server.");
					return;
				}
			
			ByteArray array = new ByteArray(0);
			byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
			int bytesReceived;
			while(true) {
				try {
					if((bytesReceived = this.client.receive(buffer)) == -1)
						break;
					if(DEBUG)
						Log.info(bytesReceived + " bytes received from patched client.");
					array.setArray(buffer, bytesReceived);
					if(processMsgStack(this.reader.processBuffer(array)))
						break;
				}
				catch(IOException e) {
					Log.info("Client disconnected from background server.");
					return;
				}
			}
			if(DEBUG)
				Log.info("Deconnection from client.");
			this.client.close();
		}
		
		private boolean processMsgStack(LinkedList<NetworkMessage> msgStack) throws IOException {
			NetworkMessage msg;
			while((msg = msgStack.poll()) != null) {
				if(DEBUG)
					Log.info(msg.toString());
				if(msg instanceof UserCommandMessage) {
					Log.info("Command received.");
					msg.deserialize();
					UserCommandMessage response = new UserCommandMessage();
					response.command = ConsoleInterface.processCommand(((UserCommandMessage) msg).command);
					this.client.send(response.pack(0));
				}
				else if(msg instanceof IdentificationMessage) {
					this.client.send(ISM.pack(characterId));
					this.client.send(RDM.pack(characterId));
					if(DEBUG)
						Log.info("IdentificationSuccessMessage and RawDataMessage sent to patched client.");
				}
				else if(msg instanceof CheckIntegrityMessage) {
					CIM = (CheckIntegrityMessage) msg;
					synchronized(self) {
						self.notify();
					}
					return true;
				}
			}
			return false;
		}
	}
}