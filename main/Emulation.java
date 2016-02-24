package main;

import java.net.SocketTimeoutException;
import java.util.LinkedList;

import messages.Message;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import utilities.ByteArray;
import utilities.Processes;

public class Emulation {
	private static final String ADL_PATH = "C:/PROGRA~2/AdobeAIRSDK/bin/adl.exe";
	private static final String ANTIBOT_PATH = System.getProperty("user.dir") + "/Ressources/Antibot/application.xml";
	private static final int launcherPort = 5554;
	private static final int serverPort = 5555;
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;
	private static Reader reader = new Reader();

	public static void runLauncher() {
		if(!Processes.inProcess("adl.exe"))
			try {
				Instance.log("Running AS launcher.");
				if(!Processes.fileExists(ADL_PATH))
					throw new Error("AIR debug executable not found.");
				if(!Processes.fileExists(ANTIBOT_PATH))
					throw new Error("Antibot not found.");
				else
					Runtime.getRuntime().exec(ADL_PATH + " " + ANTIBOT_PATH);
			} catch (Exception e) {
				Instance.fatalError(e);
			}
		else
			Instance.log("AS launcher already in process.");
	}
	
	private static void connectToLauncher() {
		Instance.log("Connection to AS launcher.");
		launcherCo = new Connection.Client("127.0.0.1", launcherPort);
		byte[] buffer = new byte[1]; // booléen d'injection
		try {
			launcherCo.receive(buffer);
		} catch(Exception e) {
			throw new Error(e);
		}
		if(buffer[0] == 0)
			Processes.injectDLL(Main.DLL_LOCATION, "adl.exe");
	}
	
	public static void sendCredentials(String login, String password) {
		if(launcherCo == null)
			connectToLauncher();
		
		ByteArray array = new ByteArray();
		array.writeInt(1 + 2 + login.length() + 2 + password.length());
		array.writeByte((byte) 1);
		array.writeUTF(login);
		array.writeUTF(password);
		Instance.log("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
	}
	
	public static Message createServer(HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM, int instanceId) {
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
			
			ByteArray array = new ByteArray();
			array.writeInt(1 + 1);
			array.writeByte((byte) 2);
			array.writeByte((byte) instanceId);
			Instance.log("Asking hash function to AS launcher.");
			launcherCo.send(array.bytes());
			
			Instance.log("Deconnection from Dofus client.");
			clientDofusCo.close();
			return CIM;
		} catch(Exception e) {
			Instance.fatalError(e);
			return null;
		}
	}
	
	public static synchronized ByteArray hashMessage(ByteArray msg, int instanceId) {
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
				Instance.log("Asking to hash message.");
				size = launcherCo.receive(buffer, 1000);
				if(size <= 0)
					throw new Error("Invalid response from launcher.");
				array = new ByteArray(buffer, size);
				if(size - 2 != array.readShort())
					throw new Error("Missing bytes !");
				break;
			} catch(SocketTimeoutException e) {
				Instance.log("Timeout for launcher response.");
			} catch(Exception e) {
				Instance.fatalError(e);
			}
		}
		Instance.log("Message hashed, " + size + " bytes received.");
		return new ByteArray(array.bytesFromPos());
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