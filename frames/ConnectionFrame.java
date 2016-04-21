package frames;

import gamedata.character.BreedEnum;
import gamedata.character.CharacterBaseInformations;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import controller.characters.Character;
import main.Emulation;
import main.FatalError;
import main.Instance;
import messages.EmptyMessage;
import messages.Message;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.BasicCharactersListMessage;
import messages.connection.CharacterCreationRequestMessage;
import messages.connection.CharacterCreationResultMessage;
import messages.connection.CharacterFirstSelectionMessage;
import messages.connection.CharacterNameSuggestionSuccessMessage;
import messages.connection.CharacterSelectedSuccessMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.NicknameChoiceRequestMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;
import messages.security.RawDataMessage;

public class ConnectionFrame extends Frame {
	private static final int MAX_PLAYER_COLOR = 5;
	//private static final int maleSadidaMinCosmeticId = 0x9101;
	private static final int femaleSadidaMinCosmeticId = 0x9901;
	//private static final int maleCraMinCosmeticId = 0x8101;
	private static final int femaleCraMinCosmeticId = 0x8901;
	private static final int cosmeticIdBound = 0x0100; // 256
	
	private Hashtable<String, Object> usefulInfos = new Hashtable<String, Object>();
	
	public ConnectionFrame(Instance instance, Character character) {
		super(instance, character);
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
			case 5640 : // NicknameRegistrationMessage
			case 5638 : // NicknameRefusedMessage
				NicknameChoiceRequestMessage NCRM = new NicknameChoiceRequestMessage();
				NCRM.nickname = getRandomNickname(20);
				NCRM.serialize();
				this.instance.outPush(NCRM);
				this.instance.log.p("Nickname asked, sending a generated nickname.");
				return true;
			case 5641 : // NicknameAcceptedMessage
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
				if(CLM.characters.size() == 0) {
					EmptyMessage EM = new EmptyMessage("CharacterNameSuggestionRequestMessage");
					this.instance.outPush(EM);
				}
				else
					selectCharacter(CLM.characters);
				return true;
			case 6475 : // BasicCharactersListMessage
				BasicCharactersListMessage BCLM = new BasicCharactersListMessage(msg);
				BCLM.deserialize();
				if(BCLM.characters.size() == 0) {
					EmptyMessage EM = new EmptyMessage("CharacterNameSuggestionRequestMessage");
					this.instance.outPush(EM);
				}
				else
					selectCharacter(BCLM.characters);
				return true;
			case 5544 : // CharacterNameSuggestionSuccessMessage
				CharacterNameSuggestionSuccessMessage CNSSM = new CharacterNameSuggestionSuccessMessage(msg);
				Random random = new Random();
				if(CNSSM.suggestion.indexOf("-") == -1) {
					CharacterCreationRequestMessage CCRM = new CharacterCreationRequestMessage();
					CCRM.name = CNSSM.suggestion;
					CCRM.breed = this.character.infos.getBreed();
					CCRM.sex = true;
					CCRM.colors = getRandomColorVector();
					if(CCRM.breed == BreedEnum.Cra)
						CCRM.cosmeticId = femaleCraMinCosmeticId + cosmeticIdBound * random.nextInt(7);
					else if(CCRM.breed == BreedEnum.Sadida)
						CCRM.cosmeticId = femaleSadidaMinCosmeticId + cosmeticIdBound * random.nextInt(7);
					else
						throw new FatalError("Unhandled breed character.");
					CCRM.serialize();
					this.instance.outPush(CCRM);
					this.instance.log.p("Creation of the character.");
				}
				else {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException e) {
						Thread.currentThread().interrupt();
						return true;
					}
					EmptyMessage EM = new EmptyMessage("CharacterNameSuggestionRequestMessage");
					this.instance.outPush(EM);
					this.instance.log.p("Asking character name suggestion.");
				}
				return true;
			case 161 : // CharacterCreationResultMessage
				CharacterCreationResultMessage CCRM = new CharacterCreationResultMessage(msg);
				if(CCRM.result != 0)
					throw new FatalError("Character creation has failed, error id : " + CCRM.result + ".");
				else
					this.character.infos.firstSelection = true;
				return true;
			case 164 : // CharacterNameSuggestionFailureMessage
				throw new FatalError("Generation of character name suggestion has failed.");
			case 153 : // CharacterSelectedSuccessMessage
				CharacterSelectedSuccessMessage CSSM = new CharacterSelectedSuccessMessage(msg);
				this.character.infos.characterName = CSSM.infos.name;
				this.character.infos.level = CSSM.infos.level;
				if(this.character.infos.getBreed() != CSSM.infos.breed)
					throw new FatalError("Incoherent character breed.");
				this.instance.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
				this.instance.endOfConnection();
				this.character.infos.isConnected = true;
				this.instance.startCharacterController();
				return true;
			case 5836 : // CharacterSelectedErrorMessage
				throw new FatalError("Error at the character selection.");
		}
		return false;
	}
	
	private void selectCharacter(Vector<CharacterBaseInformations> characters) {
		for(CharacterBaseInformations character : characters)
			this.character.infos.characterId = character.id; // on suppose qu'il n'y a qu'un seul perso sur le compte
		if(this.character.infos.firstSelection) {
			CharacterFirstSelectionMessage CFSM = new CharacterFirstSelectionMessage();
			CFSM.doTutorial = false;
			CFSM.id = this.character.infos.characterId;
			CFSM.serialize();
			this.instance.outPush(CFSM);
			this.character.infos.firstSelection = false;
		}
		else {
			CharacterSelectionMessage CSM = new CharacterSelectionMessage();
			CSM.id = this.character.infos.characterId;
			CSM.serialize();
			this.instance.outPush(CSM);
		}
		this.instance.log.p("Selection of the character.");
	}
	
	private Vector<Integer> getRandomColorVector() {
		Vector<Integer> colors = new Vector<Integer>(MAX_PLAYER_COLOR);
		//Random random = new Random();
		for(int i = 0; i < MAX_PLAYER_COLOR; ++i)
			//colors.add(random.nextInt(16777215));
			colors.add(-1);
		return colors;
	}
	
	private String getRandomNickname(int size) {
		String str = "";
		String chars = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		
		for(int i = 0; i < size - 2; ++i)
			str += chars.charAt(random.nextInt(26));
		str += random.nextInt(10);
		str += random.nextInt(10);
		return str;
	}
}