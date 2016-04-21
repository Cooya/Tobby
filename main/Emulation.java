package main;

import gui.Controller;

import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import messages.Message;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.security.RawDataMessage;
import utilities.ByteArray;
import utilities.Processes;

public class Emulation {
	private static final String ADL_PATH = "C:/PROGRA~2/AdobeAIRSDK/bin/adl.exe";
	private static final String ANTIBOT_PATH = System.getProperty("user.dir") + "/Ressources/Antibot/application.xml";
	private static final String LAUNCHER_PROCESS_NAME = "adl.exe";
	private static final int launcherPort = 5554;
	private static final int serverPort = 5555;
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;
	private static Reader reader = new Reader();
	private static Lock lock = new ReentrantLock();
	private static Process launcherProcess;

	public static void runLauncher() {
		if(!Processes.inProcess(LAUNCHER_PROCESS_NAME))
			try {
				Log.info("Running AS launcher.");
				if(!Processes.fileExists(ADL_PATH))
					throw new FatalError("AIR debug executable not found.");
				if(!Processes.fileExists(ANTIBOT_PATH))
					throw new FatalError("Antibot not found.");
				else
					launcherProcess = Runtime.getRuntime().exec(ADL_PATH + " " + ANTIBOT_PATH);
			} catch(Exception e) {
				throw new FatalError(e);
			}
		else
			Log.info("AS launcher already in process.");
	}
	
	public static void killLauncher() {
		if(launcherCo != null) {
			launcherCo.close(); // on coupe la connexion
			launcherCo = null;
			Log.info("Connection to AS launcher closed.");
		}
		if(launcherProcess != null) // et on tue le processus
			launcherProcess.destroy();
		else if(Processes.inProcess(LAUNCHER_PROCESS_NAME))
			Processes.killProcess(LAUNCHER_PROCESS_NAME);
	}
	
	public static void restartLauncher() {
		killLauncher();
		while(Processes.inProcess(LAUNCHER_PROCESS_NAME))
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		Log.info("AS launcher process killed.");
		runLauncher();
	}
	
	public static Message emulateServer(String login, String password, HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM, int instanceId) {
		lock.lock();
		if(launcherCo == null)
			connectToLauncher();
		
		// simulation de l'authentification
		ByteArray array = new ByteArray();
		array.writeInt(1 + 2 + login.length() + 2 + password.length());
		array.writeByte((byte) 1);
		array.writeUTF(login);
		array.writeUTF(password);
		Instance.log("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
		
		// simulation du serveur officiel
		try {
			clientDofusCo = new Connection.Server(serverPort);
			Instance.log("Running emulation server. Waiting Dofus client connection...");
			
			clientDofusCo.waitClient();
			Instance.log("Dofus client connected.");
			
			clientDofusCo.send(HCM.makeRaw());
			Instance.log("HCM sent to Dofus client");
			
			byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
			int bytesReceived = 0;
			bytesReceived = clientDofusCo.receive(buffer);
			Instance.log(bytesReceived + " bytes received from Dofus client.");
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			clientDofusCo.send(ISM.makeRaw());
			Instance.log("ISM sent to Dofus client");
			clientDofusCo.send(RDM.makeRaw());
			Instance.log("RDM sent to Dofus client");
			
			bytesReceived = clientDofusCo.receive(buffer);
			Instance.log(bytesReceived + " bytes received from Dofus client.");
			Message CIM = processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			array = new ByteArray();
			array.writeInt(1 + 1);
			array.writeByte((byte) 2);
			array.writeByte((byte) instanceId);
			Instance.log("Asking hash function to AS launcher.");
			launcherCo.send(array.bytes());
			
			Instance.log("Deconnection from Dofus client.");
			clientDofusCo.close();
			Thread.sleep(2000);
			lock.unlock();
			return CIM;
		} catch(InterruptedException e) {
			clientDofusCo.close();
			lock.unlock();
			return null;
		} catch(Exception e) {
			Instance.log("Interaction with Dofus client has failed, deconnection.");
			clientDofusCo.close();
			lock.unlock();
			throw new FatalError(e);
		}
	}
	
	public static ByteArray hashMessage(ByteArray msg, int instanceId) {
		lock.lock();
		ByteArray bytes = new ByteArray(msg.getSize() + 2);
		bytes.writeInt(1 + 1 + msg.getSize());
		bytes.writeByte((byte) 3); 
		bytes.writeByte((byte) instanceId);
		bytes.writeBytes(msg);
		launcherCo.send(bytes.bytes());
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int size;
		ByteArray array;
		while(true) {
			try {
				//Instance.log("Asking to hash message.");
				size = launcherCo.receive(buffer, 10000);
				if(size <= 0) {
					lock.unlock();
					throw new FatalError("Launcher deconnected.");
				}
				array = new ByteArray(buffer, size);
				if(size - 2 != array.readShort()) {
					lock.unlock();
					throw new FatalError("Missing bytes !"); // erreur qui n'est encore jamais arrivée
				}
				break;
			} catch(SocketTimeoutException e) {
				lock.unlock();
				Log.err("Timeout for launcher response, connection lost with the launcher.");
				Controller.getInstance().deconnectAllInstances("Connection lost with the launcher.", true, true);
				return null;
			} catch(Exception e) {
				lock.unlock();
				throw new FatalError(e);
			}
		}
		//Instance.log("Message hashed, " + size + " bytes received.");
		lock.unlock();
		return new ByteArray(array.bytesFromPos());
	}
	
	private static void connectToLauncher() {
		Instance.log("Connection to AS launcher.");
		launcherCo = new Connection.Client("127.0.0.1", launcherPort);
		byte[] buffer = new byte[1]; // booléen d'injection
		try {
			launcherCo.receive(buffer);
		} catch(Exception e) {
			throw new FatalError(e);
		}
		if(buffer[0] == 0)
			Processes.injectDLL(Main.DLL_LOCATION, LAUNCHER_PROCESS_NAME);
	}
	
	private static Message processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			Instance.log("r", msg);
			if(msg.getId() == 6372)
				return msg;
		}
		return null;
	}
}