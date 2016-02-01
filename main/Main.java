package main;

import java.util.Hashtable;
import java.util.LinkedList;

import roleplay.CharacterController;
import roleplay.currentmap.EntityDispositionInformations;
import roleplay.currentmap.MapComplementaryInformationsDataMessage;
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
import messages.currentmap.CurrentMapMessage;
import messages.currentmap.MapInformationsRequestMessage;
import messages.gamestarting.ChannelEnablingMessage;
import messages.gamestarting.ClientKeyMessage;
import messages.gamestarting.InterClientKeyManager;
import messages.gamestarting.PrismsListRegisterMessage;
import messages.synchronisation.BasicNoOperationMessage;
import messages.synchronisation.BasicStatMessage;
import messages.synchronisation.SequenceNumberMessage;

public class Main {
	private static final boolean MODE = true; // sniffer = false
	public static final String dllLocation = "Ressources/DLLInjector/No.Ankama.dll";
	public static final int BUFFER_SIZE = 8192;
	public static final String authServerIP = "213.248.126.39";
	public static final int serverPort = 5555;
	private static Connection serverCo = null; // temporaire bien sûr
	private static CharacterController CC = null; // temporaire aussi
	private static Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public static void main(String[] args) {
		if(MODE) {
			Emulation.runASLauncher();
			CC = new CharacterController("maxlebgdu93", "represente");
			
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
		else
			new Sniffer();
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
					IM.serialize(HCM, CC.getLogin(), CC.getPassword());
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
					usefulInfos.put("ticket", SSDM.ticket);
					usefulInfos.put("address", SSDM.address);
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
					CC.setCharacterId(CLM.getCharacterId().toNumber());
					CharacterSelectionMessage CSM = new CharacterSelectionMessage();
					CSM.serialize(CLM);
					sendMessage(CSM);
					break;
				case 153 :
					BasicStatMessage BSM = new BasicStatMessage();
					BSM.serialize();
					sendMessage(BSM);
					break;
				case 6316 :
					SequenceNumberMessage SNM = new SequenceNumberMessage();
					SNM.serialize();
					sendMessage(SNM);
					break;
				case 176 :
					new BasicNoOperationMessage(msg);
					if(BasicNoOperationMessage.getCounter() % 10 == 0) {
						BSM = new BasicStatMessage();
						BSM.serialize();
						sendMessage(BSM);
					}
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
					CC.setCurrentMap(CMM.getMapId());
					break;
				case 891 :
					MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
					MIRM.serialize(CC.getCurrentMapId());
					sendMessage(MIRM);
					break;
				case 226 :
					MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
					double characterId = CC.getCharacterId();
					String characterName = MCIDM.getCharacterName(characterId);
					if(characterName != null)
						CC.setCharacterName(characterName);
					else
						throw new Error("Invalid character id.");
					EntityDispositionInformations dispo = MCIDM.getCharacterDisposition(characterId);
					if(dispo != null) {
						CC.setCurrentCellId(dispo.cellId);
						CC.setCurrentDirection(dispo.direction);
					}
					else
						throw new Error("Invalid character id");
					break;
			}
		}
	}
	
	public static void sendMessage(Message msg) {
		serverCo.send(msg.makeRaw());
		Log.p("s", msg);
	}
}