package main;

import java.text.SimpleDateFormat;

import gui.Controller;

public class Main {
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
	
	public static void main(String[] args) {	
		Emulation.runLauncher();
		Controller.runApp();
		
		/*
		MapPosition mp = MapPosition.getMapPositionById(84805636);
		System.out.println(mp.posX + " " + mp.posY);
		
		Vector<Integer> vector = MapPosition.getMapIdByCoord(-3, -17);
		for(Integer i : vector)
			System.out.println(i + " " + MapsCache.loadMap(i).mapType);
		
		Map map = MapsCache.loadMap(84675590);
		for(Cell cell : map.cells)
			System.out.println(cell.id + " " + cell.getFloor() + " " + cell.getNonWalkableDuringRP());
		*/
	}
}