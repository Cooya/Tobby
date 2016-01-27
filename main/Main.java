package main;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import utilities.ByteArray;
import utilities.Log;
import messages.EmptyMessage;
import messages.Message;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.CheckIntegrityMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.InterClientKeyManager;
import messages.gamestarting.PrismsListRegisterMessage;
import messages.maps.CurrentMapMessage;
import messages.maps.MapComplementaryInformationsDataMessage;
import messages.maps.MapInformationsRequestMessage;
import messages.synchronisation.SequenceNumberMessage;
import movement.Cell;
import movement.D2pReader;
import movement.Pathfinder;
import movement.ankama.Map;
import movement.ankama.MapMovementAdapter;
import movement.ankama.MovementPath;

public class Main {
	public static final String dllLocation = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_SIZE = 8192;
	public static final String authServerIP = "213.248.126.39";
	public static final int serverPort = 5555;
	private static Connection serverCo = null; // temporaire bien sûr
	private static Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public static void main(String[] args) {
		
		/*
		ByteArray binaryMap = D2pReader.getBinaryMap(84804865);
		Map map = new Map(binaryMap);
		
		Pathfinder.initMap(map);
		//Vector<Cell> obs = Pathfinder.getObstacles();
		MovementPath path = Pathfinder.compute(214, 133);
		Vector<Integer> vector = MapMovementAdapter.getServerMovement(path);
		System.out.println(vector);
		*/
		
		
		
		Emulation.runASLauncher();
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesReceived = 0;
		
		Log.p("Connecting to authentification server, waiting response...");
		serverCo = new Connection.Client(authServerIP, serverPort);
		while((bytesReceived = serverCo.receive(buffer)) != -1) {
			Log.p(bytesReceived + " bytes received from server.");
			processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
		}
		serverCo.close();
		Log.p("Deconnected from authentification server.");
		
		String address = (String) usefulInfos.get("address");
		if(address != null) {
			Log.p("Connecting to game server, waiting response...");
			serverCo = new Connection.Client(address, serverPort);
			while((bytesReceived = serverCo.receive(buffer)) != -1) {
				Log.p(bytesReceived + " bytes received from server.");
				processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			serverCo.close();
			Log.p("Deconnected from game server.");
		}
	}
	
	public static void processMsgStack(LinkedList<Message> msgStack) {
		Message msg;
		while((msg = msgStack.poll()) != null) {
			Log.p("r", msg);
			switch(msg.getId()) {
				case 3 :
					HelloConnectMessage HCM = new HelloConnectMessage(msg);
					usefulInfos.put("HCM", HCM);
					IdentificationMessage IM = new IdentificationMessage();
					IM.serialize(HCM);
					sendMessage(IM);
					break;
				case 22 :
					IdentificationSuccessMessage ISM = new IdentificationSuccessMessage(msg);
					usefulInfos.put("ISM", ISM);
					break;
				case 20 :
					IdentificationFailedMessage IFM = new IdentificationFailedMessage(msg); 
					IFM.deserialize();
					break;
				case 30 : 
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize();
					sendMessage(SSM);
					break;
				case 42 : 
					SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
					usefulInfos.put("ticket", SSDM.getTicket());
					usefulInfos.put("address", SSDM.getAddress());
					break;
				case 101 :
					AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
					ATM.serialize((int[]) usefulInfos.get("ticket"));
					sendMessage(ATM);
					break;
				case 6253 :
					HCM = (HelloConnectMessage) usefulInfos.get("HCM");
					ISM = (IdentificationSuccessMessage) usefulInfos.get("ISM");
					RawDataMessage RDM = new RawDataMessage(msg);
					Emulation.sendCredentials();
					Emulation.createServer(HCM, ISM, RDM);
					break;
				case 6372 : 
					Message CIM = new CheckIntegrityMessage(msg);
					sendMessage(CIM);
					break;
				case 6267 :
					CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
					sendMessage(CLRM);
					break;
				case 151 :
					CharactersListMessage CLM = new CharactersListMessage(msg);
					CharacterSelectionMessage CSM = new CharacterSelectionMessage();
					CSM.serialize(CLM);
					sendMessage(CSM);
					break;
				case 6316 :
					SequenceNumberMessage SNM = new SequenceNumberMessage();
					SNM.serialize();
					sendMessage(SNM);
					break;
				case 6471 :
					InterClientKeyManager ICKM = InterClientKeyManager.getInstance();
					ICKM.getKey();
					EmptyMessage EM1 = new EmptyMessage("FriendsGetListMessage");
					EmptyMessage EM2 = new EmptyMessage("IgnoredGetListMessage");
					EmptyMessage EM3 = new EmptyMessage("SpouseGetInformationsMessage");
					EmptyMessage EM4 = new EmptyMessage("GameContextCreateRequestMessage");
					//EmptyMessage EM5 = new EmptyMessage("ObjectAveragePricesGetMessage");
					EmptyMessage EM6 = new EmptyMessage("QuestListRequestMessage");
					PrismsListRegisterMessage PLRM = new PrismsListRegisterMessage();
					PLRM.serialize();
					ChannelEnablingMessage CEM = new ChannelEnablingMessage();
					CEM.serialize();
					ClientKeyMessage CKM = new ClientKeyMessage();
					CKM.serialize(ICKM);
					sendMessage(EM1);
					sendMessage(EM2);
					sendMessage(EM3);
					sendMessage(CKM);
					sendMessage(EM4);
					//sendMessage(EM5);
					sendMessage(EM6);
					sendMessage(PLRM);
					sendMessage(CEM);
					break;
				case 220 :
					CurrentMapMessage CMM = new CurrentMapMessage(msg);
					MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
					MIRM.serialize(CMM);
					sendMessage(MIRM);	
					
					ByteArray binaryMap = D2pReader.getBinaryMap(CMM.getMapId());
					Map map = new Map(binaryMap);
					
					Pathfinder.initMap(map);
					MovementPath path = Pathfinder.compute(214, 133);
					Vector<Integer> vector = MapMovementAdapter.getServerMovement(path);
					System.out.println(vector);
					
					break;
				case 226 :
					MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
					System.out.println(MCIDM.getClass().getDeclaredFields());
					
					break;
			}
		}
	}
	
	private static void sendMessage(Message msg) {
		
		//displayAllFields(msg);
		
		serverCo.send(msg.makeRaw());
		Log.p("s", msg);
	}
	
	private static void displayAllFields(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		for(Field field : fields)
			try {
				field.setAccessible(true);
				System.out.println(field.getName() + " = " + field.get(o));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}