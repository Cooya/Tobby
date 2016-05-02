package main;

import gui.Controller;

import java.text.SimpleDateFormat;

public class Main {
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
	public static final int[] GAME_VERSION = {2, 34, 4, 104034, 1};
	public static final String LIB_PATH;
	public static final String INJECTOR_PATH;
	public static final String BYPASS_PATH;
	public static final String BYPASS_EXE;
	public static final String D2P_PATH = "Ressources/Bypass/content/maps/maps0.d2p";
	public static final String D2O_PATH = "Ressources/Bypass/data/common/";
	public static final String D2I_PATH = "Ressources/Bypass/data/i18n/i18n_fr.d2i";
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final String LOCALHOST = "127.0.0.1";
	public static final int SERVER_PORT = 5555;
	public static final int LAUNCHER_PORT = 5554;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
	public static final String MODERATOR_NAME = "[Alkalino]";
	
	static {
		if(IS_WINDOWS) {
			LIB_PATH = "Ressources/Injector/No.Ankama.dll";
			INJECTOR_PATH = "Ressources/Injector/Injector.exe";
			BYPASS_PATH = "Ressources/Bypass/Bypass.exe";
			BYPASS_EXE = "Bypass.exe";
		}
		else {
			LIB_PATH = "Ressources/Injector/No.Ankama.so";
			INJECTOR_PATH = "";
			BYPASS_PATH = "";
			BYPASS_EXE = "";
		}
	}

	public static void main(String[] args) {
		Controller.getInstance();
		//new Sniffer();
		
		/*
		byte[] content = {1};
		
		Vector<Long> perfTest = new Vector<Long>();
		long startTime;
		for(int i = 0; i < 100000; ++i) {
			startTime = System.nanoTime();
			Message msg = new Message(182, 1, 1, content, 1);
			BasicPingMessage BPM = new BasicPingMessage(msg);
			perfTest.add(System.nanoTime() - startTime);
		}
		
		long sum = 0;
		for(long l : perfTest)
			sum += l;
		System.out.println("Average time : " + sum / perfTest.size());
		
		
		perfTest = new Vector<Long>();
		
		for(int i = 0; i < 100000; ++i) {
			startTime = System.nanoTime();
			Message msg2 = new Message(182, 1, 1, content, 1);
			BasicPingMessage BPM2 = (BasicPingMessage) msg2;
			perfTest.add(System.nanoTime() - startTime);
		}
		
		sum = 0;
		for(long l : perfTest)
			sum += l;
		System.out.println("Average time : " + sum / perfTest.size());
		*/
		
		/*
		GameMapMovementRequestMessage test = new GameMapMovementRequestMessage();
		test.mapId = 100;
		Message test2 = test;
		GameMapMovementRequestMessage test3 = (GameMapMovementRequestMessage) test2;
		System.out.println(test3.mapId);

		ByteArray buffer = new ByteArray();
		buffer.writeByte(0xE6);
		buffer.writeByte(0xEE);
		buffer.writeByte(0x1D);
		buffer.writeByte(0xC7);
		buffer.writeByte(0xB1);
		buffer.writeByte(0x0E);
		buffer.writeByte(0xBA);
		buffer.writeByte(0x2C);
		buffer.setPos(0);
		System.out.println(buffer.readVarInt());
		System.out.println(buffer.readVarInt());
		 */

		/*
		byte[] bytes = null;
		for(SubArea subArea : SubArea.getAllSubArea()) {
			bytes = new String(subArea.getName() + " " + subArea.id + " " + subArea.getArea().getName() + " " + subArea.getArea().id).getBytes();
			System.out.println(new String(bytes, StandardCharsets.UTF_8));
		}
		 */

		//Map map = MapsCache.loadMap(84675590);
		//for(Cell cell : map.cells)
		//System.out.println(cell.id + " " + cell.getNonWalkableDuringRP());

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