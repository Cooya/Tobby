package main;

import java.text.SimpleDateFormat;

public class Main {
	public static final int[] GAME_VERSION = {2, 34, 2, 103887, 2};
	public static final String DLL_LOCATION = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_DEFAULT_SIZE = 8192;
	public static final String AUTH_SERVER_IP = "213.248.126.39";
	public static final int SERVER_PORT = 5555;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
	public static final String MODERATOR_NAME = "[Alkalino]";
	

	public static void main(String[] args) {	
		//Controller.getInstance();
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