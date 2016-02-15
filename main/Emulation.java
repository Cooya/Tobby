package main;

import java.util.LinkedList;

import messages.Message;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import utilities.ByteArray;
import utilities.Log;
import utilities.Processes;

public class Emulation {
	private static final String APP_PATH = System.getProperty("user.dir");
	private static final int launcherPort = 5554;
	private static final int serverPort = 5555;
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;
	private static Reader reader = new Reader();

	public static void runASLauncher() {
		if(!Processes.inProcess("adl.exe"))
			try {
				Log.p("Running AS launcher.");
				String adlPath = "C:/PROGRA~2/AdobeAIRSDK/bin/adl.exe";
				if(!Processes.fileExists(adlPath))
					throw new Error("AIR debug launcher not found.");
				else
					Runtime.getRuntime().exec(adlPath + " " + APP_PATH + "/Ressources/Antibot/application.xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
		else
			Log.p("AS launcher already in process.");
	}
	
	public static void sendCredentials() {
		launcherCo = new Connection.Client("127.0.0.1", launcherPort);
		byte[] buffer = new byte[1]; // bool d'injection
		launcherCo.receive(buffer);
		if(buffer[0] == 0)
			Processes.injectDLL(Main.DLL_LOCATION, "adl.exe");
		ByteArray array = new ByteArray();
		array.writeInt(1 + 2 + 11 + 2 + 10);
		array.writeByte((byte) 1);
		array.writeUTF("maxlebgdu93");
		array.writeUTF("represente");
		Log.p("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
	}
	
	public static Message createServer(HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM, int instanceId) {
		try {
			clientDofusCo = new Connection.Server(serverPort);
			Log.p("Running emulation server. Waiting Dofus client connection...");
			
			clientDofusCo.waitClient();
			Log.p("Dofus client connected.");
			
			clientDofusCo.send(HCM.makeRaw());
			Log.p("HCM sent to Dofus client");
			
			byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
			int bytesReceived = 0;
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			clientDofusCo.send(ISM.makeRaw());
			Log.p("ISM sent to Dofus client");
			clientDofusCo.send(RDM.makeRaw());
			Log.p("RDM sent to Dofus client");
			
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			Message CIM = processMsgStack(reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			ByteArray array = new ByteArray();
			array.writeInt(1 + 1);
			array.writeByte((byte) 2);
			array.writeByte((byte) instanceId);
			Log.p("Asking hash function to AS launcher.");
			launcherCo.send(array.bytes());
			
			Log.p("Deconnection from Dofus client.");
			clientDofusCo.close();
			return CIM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ByteArray hashMessage(ByteArray msg, int instanceId) {
		ByteArray bytes = new ByteArray(msg.getSize() + 2);
		bytes.writeInt(1 + 1 + msg.getSize());
		bytes.writeByte((byte) 3); 
		bytes.writeByte((byte) instanceId);
		bytes.writeBytes(msg);
		launcherCo.send(bytes.bytes());
		byte[] buffer = new byte[Main.BUFFER_DEFAULT_SIZE];
		int size = launcherCo.receive(buffer);
		return new ByteArray(buffer, size);
	}
	
	public static Message processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			Log.p("r", msg);
			if(msg.getId() == 6372)
				return msg;
		}
		return null;
	}
}
