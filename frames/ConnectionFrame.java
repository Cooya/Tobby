package frames;

import java.util.Hashtable;

import roleplay.CharacterController;
import utilities.Log;
import main.Emulation;
import main.NetworkInterface;
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
import messages.connection.ServersListMessage;

public class ConnectionFrame extends Frame {
	private Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public ConnectionFrame(NetworkInterface net, CharacterController CC) {
		super(net, CC);
	}
	
	public void processMessage(Message msg) {
		switch(msg.getId()) {
			case 3 :
				HelloConnectMessage HCM = new HelloConnectMessage(msg);
				this.usefulInfos.put("HCM", HCM);
				IdentificationMessage IM = new IdentificationMessage();
				IM.serialize(HCM, CC.getLogin(), CC.getPassword());
				net.sendMessage(IM);
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
				int serverId = CC.getServerId();
				if(SLM.isSelectable(serverId)) {
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					net.sendMessage(SSM);
				}
				else
					Log.p("Backup in progress on the requested server.");
				break;
			case 42 : 
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				this.usefulInfos.put("ticket", SSDM.ticket);
				net.setGameServerIP(SSDM.address);
				break;
			case 101 :
				AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
				ATM.serialize((int[]) this.usefulInfos.get("ticket"));
				net.sendMessage(ATM);
				break;
			case 6253 :
				HCM = (HelloConnectMessage) this.usefulInfos.get("HCM");
				ISM = (IdentificationSuccessMessage) this.usefulInfos.get("ISM");
				RawDataMessage RDM = new RawDataMessage(msg);
				Emulation.sendCredentials();
				Message CIM = Emulation.createServer(HCM, ISM, RDM);
				net.sendMessage(CIM);
				break;
			case 6267 :
				CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
				net.sendMessage(CLRM);
				break;
			case 151 :
				CharactersListMessage CLM = new CharactersListMessage(msg);
				CC.setCharacterId(CLM.getCharacterId().toNumber());
				CharacterSelectionMessage CSM = new CharacterSelectionMessage();
				CSM.serialize(CLM);
				net.sendMessage(CSM);
				break;
		}
	}
}
