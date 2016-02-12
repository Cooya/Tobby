package main;

import java.util.Vector;

import roleplay.d2o.modules.MapPosition;
import roleplay.pathfinding.MapsPathfinder;
import roleplay.pathfinding.Path;
import roleplay.pathfinding.Pathfinder;

public class Main {
	private static final boolean MODE = true; // sniffer = false
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	
	public static void main(String[] args) {
		Pathfinder p = new MapsPathfinder(258);
		Path path = p.compute(84676101, 84677124);
		Vector<Integer> vector = path.toVector();
		for(int i : vector) {
			System.out.print(MapPosition.getMapPositionById(i).posX + " ");
			System.out.println(MapPosition.getMapPositionById(i).posY + " ");
		}
		/*
		if(MODE) {
			Emulation.runASLauncher();
			new Instance("maxlebgdu93", "represente", 11); // pour le moment, on en gère qu'un seul
		}
		else
			new Sniffer();
		*/
	}
}