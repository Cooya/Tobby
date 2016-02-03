package main;

import roleplay.movement.Pathfinder;
import roleplay.paths.Path;
import roleplay.paths.PathsManager;

public class Main {
	private static final boolean MODE = true; // sniffer = false
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	
	public static void main(String[] args) {
		if(MODE) {
			Emulation.runASLauncher();
			createPathExample();
			new Instance("maxlebgdu93", "represente", 11); // pour le moment, on en gère qu'un seul
		}
		else
			new Sniffer();
	}
	
	public static void createPathExample() {
		Path path = PathsManager.createPath("test", true);
		path.addNode(84672771, Pathfinder.UP);
		path.addNode(84672772, Pathfinder.LEFT);
		path.addNode(84804356, Pathfinder.LEFT);
		path.addNode(84804868, Pathfinder.DOWN);
		path.addNode(84804867, Pathfinder.DOWN);
		path.addNode(84804866, Pathfinder.RIGHT);
		path.addNode(84804354, Pathfinder.RIGHT);
		path.addNode(84672770, Pathfinder.UP);
	}
}