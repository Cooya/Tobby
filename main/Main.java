package main;

import java.text.SimpleDateFormat;

import network.DatabaseConnection;
import network.RemoteConsoleInterface;
import network.ServerInterface;
import messages.NetworkMessage;
import utilities.Processes;

public class Main {
	public static final String USERNAME = System.getProperty("user.name");
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
	public static final String BYPASS_PATH;
	public static final String BYPASS_PROCESS_NAME;
	public static final String CLIENT_PATH;
	public static final String DOFUS_PATH;
	
	static {
		if(IS_WINDOWS) {
			BYPASS_PATH = "C:/Program Files (x86)/Bypass/Bypass.exe";
			BYPASS_PROCESS_NAME = "Bypass.exe";
			CLIENT_PATH = "C:/Program Files (x86)/Bypass/";
			DOFUS_PATH = "C:/Program Files (x86)/Ankama/Dofus/app";
		}
		else {
			BYPASS_PATH = "/opt/Dofus2/Dofus/bin/Dofus";
			BYPASS_PROCESS_NAME = "Dofus";
			CLIENT_PATH = "/opt/Dofus2/Dofus/share/";
			DOFUS_PATH = "/opt/Dofus2/Dofus/share/";
		}
	}
	
	public static final String D2P_PATH = CLIENT_PATH + "content/maps/maps0.d2p";
	public static final String D2O_PATH = CLIENT_PATH + "data/common/";
	public static final String D2I_PATH = CLIENT_PATH + "data/i18n/i18n_fr.d2i";
	public static final String LOG_PATH = "Logs/";
	public static final int[] GAME_VERSION = {2, 34, 6, 105356, 5};
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final String LOCALHOST = "127.0.0.1";
	public static final int SERVER_PORT = 5555;
	public static final int LAUNCHER_PORT = 5554;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
	public static boolean TEST_MODE = false;
	private static boolean exitAsked = false;

	public static void main(String[] args) {
		if(args.length == 1) {
			if(args[0].equals("--server")) { // mode serveur
				if(!Processes.fileExists(BYPASS_PATH)) {
					Log.err("Bypass application not installed on the computer.");
					return;
				}
				ServerInterface.getInstance().runPatchedClient();
				NetworkMessage.loadMessagesListAndClasses();
				DatabaseConnection.unlockAllAccounts();
			}
			else if(args[0].equals("--driver")) { // mode driver
				NetworkMessage.loadMessagesListAndClasses();
				RemoteConsoleInterface.start();
			}
			else
				Log.err("Invalid argument.");
		}
		else { // mode test
			TEST_MODE = true;
			if(!Processes.fileExists(BYPASS_PATH)) {
				Log.err("Bypass application not installed on the computer.");
				return;
			}
			ServerInterface.getInstance().runPatchedClient();
			NetworkMessage.loadMessagesListAndClasses();
			DatabaseConnection.unlockAllAccounts();
			ConsoleInterface.start();
		}
	}
	
	protected static void askExit() {
		exitAsked = true;
	}
	
	protected static boolean exitAsked() {
		return exitAsked;
	}
	
	// fonction appelée à la fermeture de l'application ou lors d'une erreur critique
	public static void exit(String reason) {
		ServerInterface.getInstance().exitClientPatched();
		DatabaseConnection.unlockAllAccounts();
		if(reason != null)
			Log.err(reason);
		else
			Log.info("Application closed by user.");
		System.exit(0);
	}
}