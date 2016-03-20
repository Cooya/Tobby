package frames;

import gamedata.character.CharacterBaseInformations;

import java.util.Hashtable;

import controller.CharacterController;
import main.Emulation;
import main.FatalError;
import main.Instance;
import messages.Message;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.BasicCharactersListMessage;
import messages.connection.CharacterSelectedSuccessMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;
import messages.security.RawDataMessage;

public class ConnectionFrame extends Frame {
	private Instance instance;
	private CharacterController character;
	private Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public ConnectionFrame(Instance instance, CharacterController character) {
		this.instance = instance;
		this.character = character;
	}
	
	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 3 : // HelloConnectMessage
				HelloConnectMessage HCM = new HelloConnectMessage(msg);
				this.usefulInfos.put("HCM", HCM);
				IdentificationMessage IM = new IdentificationMessage();
				IM.serialize(HCM, this.character.infos.login, this.character.infos.password);
				instance.outPush(IM);
				return true;
			case 22 : // IdentificationSuccessMessage
				IdentificationSuccessMessage ISM = new IdentificationSuccessMessage(msg);
				this.usefulInfos.put("ISM", ISM);
				return true;
			case 20 : // IdentificationFailedMessage
				IdentificationFailedMessage IFM = new IdentificationFailedMessage(msg); 
				this.instance.log.p("Authentification failed for reason " + IFM.reason);
				return true;
			case 30 : // ServersListMessage
				ServersListMessage SLM = new ServersListMessage(msg);
				int serverId = this.character.infos.serverId;
				if(SLM.isSelectable(serverId)) {
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				else
					this.instance.log.p("Backup in progress on the requested server.");
				return true;
			case 50 : // ServerStatusUpdateMessage
				ServerStatusUpdateMessage SSUM = new ServerStatusUpdateMessage(msg);
				serverId = this.character.infos.serverId;
				if(SSUM.server.id == serverId && SSUM.server.isSelectable) {	
					ServerSelectionMessage SSM = new ServerSelectionMessage();
					SSM.serialize(serverId);
					instance.outPush(SSM);
				}
				return true;
			case 42 : // SelectedServerDataMessage
				SelectedServerDataMessage SSDM = new SelectedServerDataMessage(msg);
				this.usefulInfos.put("ticket", SSDM.ticket);
				instance.setGameServerIP(SSDM.address);
				return true;
			case 101 : // HelloGameMessage
				AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
				ATM.serialize("fr", (int[]) this.usefulInfos.get("ticket"));
				instance.outPush(ATM);
				return true;
			case 6253 : // RawDataMessage
				HCM = (HelloConnectMessage) this.usefulInfos.get("HCM");
				ISM = (IdentificationSuccessMessage) this.usefulInfos.get("ISM");
				RawDataMessage RDM = new RawDataMessage(msg);
				Message CIM = Emulation.emulateServer(this.character.infos.login, this.character.infos.password, HCM, ISM, RDM, instance.id);
				instance.outPush(CIM);
				return true;		
			case 6267 : // TrustStatusMessage
				CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
				instance.outPush(CLRM);
				return true;
			case 151 : // CharactersListMessage
				CharactersListMessage CLM = new CharactersListMessage(msg);
				CharacterSelectionMessage CSM = new CharacterSelectionMessage();
				for(CharacterBaseInformations character : CLM.characters)
					CSM.id = character.id; // on suppose qu'il n'y a qu'un seul perso sur le compte
				
				System.out.println(CSM.id);
				
				this.character.infos.characterId = CSM.id;
				CSM.serialize();
				instance.outPush(CSM);
				return true;
			case 6475 : // BasicCharactersListMessage
				BasicCharactersListMessage BCLM = new BasicCharactersListMessage(msg);
				BCLM.deserialize();
				CSM = new CharacterSelectionMessage();
				for(CharacterBaseInformations character : BCLM.characters)
					CSM.id = character.id; // on suppose qu'il n'y a qu'un seul perso sur le compte
				this.character.infos.characterId = CSM.id;
				CSM.serialize();
				instance.outPush(CSM);
				return true;
			case 153 : // CharacterSelectedSuccessMessage
				CharacterSelectedSuccessMessage CSSM = new CharacterSelectedSuccessMessage(msg);
				this.character.infos.characterName = CSSM.infos.name;
				this.character.infos.level = CSSM.infos.level;
				this.character.infos.setBreed(CSSM.infos.breed);
				this.instance.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
				this.instance.endOfConnection();
				this.character.infos.isConnected = true;
				return true;
			case 5836 : // CharacterSelectedErrorMessage
				throw new FatalError("Error at the character selection.");
		}
		return false;
	}
}