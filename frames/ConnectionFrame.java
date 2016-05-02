package frames;

import gamedata.character.CharacterBaseInformations;
import gamedata.connection.GameServerInformations;
import gamedata.connection.VersionExtended;
import gamedata.enums.BreedEnum;
import gui.Controller;

import java.util.Random;
import java.util.Vector;

import utilities.ByteArray;
import utilities.Encryption;
import controller.characters.Character;
import main.Emulation;
import main.FatalError;
import main.Main;
import messages.Message;
import messages.UnhandledMessage;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.BasicCharactersListMessage;
import messages.connection.CharacterCreationRequestMessage;
import messages.connection.CharacterCreationResultMessage;
import messages.connection.CharacterFirstSelectionMessage;
import messages.connection.CharacterNameSuggestionFailureMessage;
import messages.connection.CharacterNameSuggestionSuccessMessage;
import messages.connection.CharacterSelectedErrorMessage;
import messages.connection.CharacterSelectedSuccessMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.HelloGameMessage;
import messages.connection.IdentificationFailedForBadVersionMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.NicknameChoiceRequestMessage;
import messages.connection.NicknameRefusedMessage;
import messages.connection.NicknameRegistrationMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;
import messages.connection.TrustStatusMessage;
import messages.security.RawDataMessage;

public class ConnectionFrame extends Frame {
	private static final int MAX_PLAYER_COLOR = 5;
	//private static final int maleSadidaMinCosmeticId = 145;
	private static final int femaleSadidaMinCosmeticId = 153;
	//private static final int maleCraMinCosmeticId = 129;
	private static final int femaleCraMinCosmeticId = 137;
	
	private HelloConnectMessage HCM;
	private IdentificationSuccessMessage ISM;
	private int[] ticket;
	
	public ConnectionFrame(Character character) {
		super(character);
	}
	
	protected void process(HelloConnectMessage HCM) {
		this.HCM = HCM;
		IdentificationMessage IM = new IdentificationMessage();
		IM.version = new VersionExtended(Main.GAME_VERSION[0], Main.GAME_VERSION[1], Main.GAME_VERSION[2], Main.GAME_VERSION[3], Main.GAME_VERSION[4], 0, 1, 1);
		IM.lang = "fr";
		IM.credentials = Encryption.encryptCredentials(Encryption.decryptReceivedKey(ByteArray.toBytes(HCM.key)), this.character.infos.login, this.character.infos.password, HCM.salt);
		IM.serverId = 0; // sélection du serveur automatique
		this.character.net.send(IM);
	}
	
	protected void process(IdentificationSuccessMessage ISM) {
		this.ISM = ISM;
	}
	
	protected void process(IdentificationFailedMessage IFM) { 
		this.character.log.p("Authentification failed for reason " + IFM.reason);
	}
	
	protected void process(IdentificationFailedForBadVersionMessage IFFBVM) {
		Controller.getInstance().exit("Authentification failed for bad version, need to update.");
	}
	
	protected void process(NicknameRegistrationMessage NRM) {
		NicknameChoiceRequestMessage NCRM = new NicknameChoiceRequestMessage();
		NCRM.nickname = getRandomNickname(20);
		this.character.net.send(NCRM);
		this.character.log.p("Nickname asked, sending a generated nickname.");
	}
	
	protected void process(NicknameRefusedMessage NRM) {
		NicknameChoiceRequestMessage NCRM = new NicknameChoiceRequestMessage();
		NCRM.nickname = getRandomNickname(20);
		this.character.net.send(NCRM);
		this.character.log.p("Nickname refused, sending a new generated nickname.");
	}
	
	protected void process(ServersListMessage SLM) {
		if(serverIsSelectable(SLM.servers, this.character.infos.serverId)) {
			ServerSelectionMessage SSM = new ServerSelectionMessage();
			SSM.serverId = this.character.infos.serverId;
			this.character.net.send(SSM);
		}
		else
			this.character.log.p("Backup in progress on the requested server.");
	}
	
	protected void process(ServerStatusUpdateMessage SSUM) {
		if(SSUM.server.id == this.character.infos.serverId && SSUM.server.isSelectable) {	
			ServerSelectionMessage SSM = new ServerSelectionMessage();
			SSM.serverId = this.character.infos.serverId;
			this.character.net.send(SSM);
		}
	}
	
	protected void process(SelectedServerDataMessage SSDM) {
		this.ticket = SSDM.ticket;
		this.character.net.setGameServerIP(SSDM.address);
	}
	
	protected void process(HelloGameMessage HCM) {
		AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
		ATM.lang = "fr";
		ATM.ticket = new String(Encryption.decodeWithAES(ByteArray.toBytes(this.ticket)));
		this.character.net.send(ATM);
	}
	
	protected void process(RawDataMessage RDM) {
		Message CIM = Emulation.emulateServer(this.character.infos.login, this.character.infos.password, this.HCM, this.ISM, RDM, character.id);
		CIM.deserialize(); // exception
		this.character.net.send(CIM);
	}
	
	protected void process(TrustStatusMessage TSM) {
		CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
		this.character.net.send(CLRM);
	}
	
	protected void process(CharactersListMessage CLM) {
		if(CLM.characters.size() == 0)
			this.character.net.send(new UnhandledMessage("CharacterNameSuggestionRequestMessage"));
		else
			selectCharacter(CLM.characters);
	}
	
	protected void process(BasicCharactersListMessage BCLM) {
		if(BCLM.characters.size() == 0)
			this.character.net.send(new UnhandledMessage("CharacterNameSuggestionRequestMessage"));
		else
			selectCharacter(BCLM.characters);
	}
	
	protected void process(CharacterNameSuggestionSuccessMessage CNSSM) {
		Random random = new Random();
		if(CNSSM.suggestion.indexOf("-") == -1) {
			CharacterCreationRequestMessage CCRM = new CharacterCreationRequestMessage();
			CCRM.name = CNSSM.suggestion;
			CCRM.breed = this.character.infos.getBreed();
			CCRM.sex = true; // femelle
			CCRM.colors = getRandomColorVector();
			if(CCRM.breed == BreedEnum.Cra)
				CCRM.cosmeticId = femaleCraMinCosmeticId + random.nextInt(7);
			else if(CCRM.breed == BreedEnum.Sadida)
				CCRM.cosmeticId = femaleSadidaMinCosmeticId + random.nextInt(7);
			else
				throw new FatalError("Unhandled breed character.");
			this.character.net.send(CCRM);
			this.character.log.p("Creation of the character.");
		}
		else {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			this.character.net.send(new UnhandledMessage("CharacterNameSuggestionRequestMessage"));
			this.character.log.p("Asking character name suggestion.");
		}
	}
	
	protected void process(CharacterCreationResultMessage CCRM) {
		if(CCRM.result != 0)
			throw new FatalError("Character creation has failed, error id : " + CCRM.result + ".");
		else
			this.character.infos.firstSelection = true;
	}
	
	protected void process(CharacterNameSuggestionFailureMessage CNSFM) {
		throw new FatalError("Generation of character name suggestion has failed.");
	}
	
	protected void process(CharacterSelectedSuccessMessage CSSM) {
		this.character.infos.characterName = CSSM.infos.name;
		this.character.infos.level = CSSM.infos.level;
		if(this.character.infos.getBreed() != CSSM.infos.breed)
			throw new FatalError("Incoherent character breed.");
		this.character.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
		this.character.processor.endOfConnection();
		this.character.infos.isConnected = true;
		this.character.start(); // démarrage du thread contrôleur
	}
	
	protected void process(CharacterSelectedErrorMessage CSEM) {
		throw new FatalError("Error at the character selection.");
	}
	
	private static boolean serverIsSelectable(Vector<GameServerInformations> servers, int serverId) {
		for(GameServerInformations server : servers)
			if(server.id == serverId)
				if(!server.isSelectable)
					return false;
				else
					return true;
		throw new FatalError("Invalid server id.");
	}
	
	private void selectCharacter(Vector<CharacterBaseInformations> characters) {
		for(CharacterBaseInformations character : characters)
			this.character.infos.characterId = character.id; // on suppose qu'il n'y a qu'un seul perso sur le compte
		if(this.character.infos.firstSelection) {
			CharacterFirstSelectionMessage CFSM = new CharacterFirstSelectionMessage();
			CFSM.doTutorial = false;
			CFSM.id = this.character.infos.characterId;
			this.character.net.send(CFSM);
			this.character.infos.firstSelection = false;
		}
		else {
			CharacterSelectionMessage CSM = new CharacterSelectionMessage();
			CSM.id = this.character.infos.characterId;
			this.character.net.send(CSM);
		}
		this.character.log.p("Selection of the character.");
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
	
	private Vector<Integer> getRandomColorVector() {
		Vector<Integer> colors = new Vector<Integer>(MAX_PLAYER_COLOR);
		Random random = new Random();
		for(int i = 0; i < MAX_PLAYER_COLOR; ++i)
			colors.add(random.nextInt(16777215)); // nombre maximal d'une couleur
		return colors;
	}
}