package main;

import gui.Controller;

public class Main {
	//private static final boolean MODE = true; // sniffer = false
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	
	public static void main(String[] args) {
		//if(MODE) {
			//new Instance("maxlebgdu96", "represente", 11);
			//new Instance("maxlebgdu98", "represente", 11);
			//new Instance("maxlebgdu99", "represente", 11);
			//new Instance("maxlebgdu100", "represente", 11);
			
			Emulation.runLauncher();
			new Controller();
		//}
		//else
			//new Sniffer();
			
		/*
		CellsPathfinder pf = new CellsPathfinder(MapsCache.loadMap(153879300));
		Path path = pf.compute(187, 141);
		System.out.println(path);
		*/
	}
}