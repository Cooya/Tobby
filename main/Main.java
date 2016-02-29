package main;

import gui.Controller;

public class Main {
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	
	public static void main(String[] args) {
		//new Instance("maxlebgdu100", "represente", 11);	
		Emulation.runLauncher();
		new Controller();
	}
}