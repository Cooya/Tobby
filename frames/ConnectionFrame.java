package frames;

import java.util.Hashtable;

import utilities.Log;
import main.CharacterController;
import main.Emulation;
import main.Instance;
import messages.Message;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.RawDataMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;

public class ConnectionFrame implements Frame {
	private Instance instance;
	private CharacterController CC;
	private Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public ConnectionFrame(Instance instance, CharacterController CC) {
		this.instance = instance;
		this.CC = CC;
	}
	
	public void processMessage(Message msg) {
		switch(msg.getId()) {
			case 3 :
				HelloConnectMessage HCM = new HelloConnectMessage(msg);
				this.usefulInfos.put("HCM", HCM);
				IdentificationMessage IM = new IdentificationMessage();
				IM.serialize(HCM, CC.login, CC.password);
				instance.outPush(IM);
				break;
			case 22 :
				IdentificationSuccessMessage ISM = new IdentificationSuccessMessage(msg);
				this.usefulInfos.put("ISM", ISM);
				break;
			case 20 :
				IdentificationFailedMessage IFM = new IdentificationFailedMessage(msg); 
				IFM.deserialize();
				break;
			case 30 :
				ServersListMessage SLM = new ServersListMessage(msg);
				int serverId = CC.serverId;
				if(SLM.isSelectable(serverId)) {
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				else
					Log.p("Backup in progress on the requested server.");
				break;
			case 50 :
				ServerStatusUpdateMessage SSUM = new ServerStatusUpdateMessage(msg);
				serverId = CC.serverId;
				if(SSUM.server.id == serverId && SSUM.server.isSelectable) {	
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				break;
			case 42 : 
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				this.usefulInfos.put("ticket", SSDM.ticket);
				instance.setGameServerIP(SSDM.address);
				break;
			case 101 :
				AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
				ATM.serialize((int[]) this.usefulInfos.get("ticket"));
				instance.outPush(ATM);
				break;
			case 6253 :
				HCM = (HelloConnectMessage) this.usefulInfos.get("HCM");
				ISM = (IdentificationSuccessMessage) this.usefulInfos.get("ISM");
				RawDataMessage RDM = new RawDataMessage(msg);
				Emulation.sendCredentials();
				Message CIM = Emulation.createServer(HCM, ISM, RDM);
				instance.outPush(CIM);
				break;
			case 6267 :
				CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
				instance.outPush(CLRM);
				break;
			case 151 :
				CharactersListMessage CLM = new CharactersListMessage(msg);
				CC.characterId = CLM.getCharacterId().toNumber();
				CharacterSelectionMessage CSM = new CharacterSelectionMessage();
				CSM.serialize(CLM);
				instance.outPush(CSM);
				
				this.instance.endOfConnection();
				break;
		}
	}
}
