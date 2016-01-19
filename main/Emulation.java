package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import messages.HelloConnectMessage;
import messages.IdentificationSuccessMessage;
import messages.RawDataMessage;
import utilities.ByteArray;
import utilities.Log;

public class Emulation {
	private static final String APP_PATH = System.getProperty("user.dir");
	private static final int launcherPort = 5554;
	private static final int serverPort = 5555;
	private static Connection.Client launcherCo;
	private static Connection.Server clientDofusCo;

	public static void runASLauncher() {
		if(!isInProcess("adl.exe"))
			try {
				Log.p("Running AS launcher.");
				String adlPath = "C:/PROGRA~2/AdobeAIRSDK/bin/adl.exe";
				if(!fileExists(adlPath))
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
			try {
				Process p = Runtime.getRuntime().exec(APP_PATH + "/Ressources/DLLInjector/Injector.exe No.Ankama.dll adl.exe");
				InputStream in = p.getInputStream();
				byte[] bytes = new byte[Main.BUFFER_SIZE];
				in.read(bytes);
				Log.p("DLL Injection.\n" + new String(bytes));
			} catch (Exception e) {
				e.printStackTrace();
			}
		ByteArray array = new ByteArray();
		array.writeInt(2 + 11 + 2 + 10);
		array.writeUTF("maxlebgdu93");
		array.writeUTF("represente");
		Log.p("Sending credentials to AS launcher.");
		launcherCo.send(array.bytes());
	}
	
	public static void createServer(HelloConnectMessage HCM, IdentificationSuccessMessage ISM, RawDataMessage RDM) {
		try {
			clientDofusCo = new Connection.Server(serverPort);
			Log.p("Running emulation server. Waiting Dofus client connection...");
			
			clientDofusCo.waitClient();
			Log.p("Dofus client connected.");
			
			clientDofusCo.send(HCM.makeRaw());
			Log.p("HCM sent to Dofus client");
			
			byte[] buffer = new byte[Main.BUFFER_SIZE];
			int bytesReceived = 0;
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			Main.processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			clientDofusCo.send(ISM.makeRaw());
			Log.p("ISM sent to Dofus client");
			clientDofusCo.send(RDM.makeRaw());
			Log.p("RDM sent to Dofus client");
			
			bytesReceived = clientDofusCo.receive(buffer);
			Log.p(bytesReceived + " bytes received from Dofus client.");
			Main.processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			
			Log.p("Deconnection from AS launcher and Dofus client.");
			launcherCo.close();
			clientDofusCo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isInProcess(String processName) {
		try {
		    String line;
		    Process p = Runtime.getRuntime().exec("tasklist");
		    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while ((line = input.readLine()) != null)
		        if(line.split(" ")[0].equals(processName))
		        	return true;
		    input.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	private static boolean fileExists(String filePath) {
		File f = new File(filePath);
		if(f.exists() && !f.isDirectory())
		    return true;
		return false;
	}
}
