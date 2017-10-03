package frames;

import gamedata.character.CharacterBaseInformations;
import gamedata.connection.GameServerInformations;
import gamedata.connection.VersionExtended;
import gamedata.enums.BreedEnum;
import gamedata.enums.ConnectionResult;
import gamedata.enums.IdentificationFailureReasonEnum;
import gamedata.enums.ServerEnum;
import gamedata.enums.ServerStatusEnum;

import java.util.Random;

import network.DatabaseConnection;
import network.ServerInterface;
import utilities.ByteArray;
import utilities.Encryption;
import controller.characters.Character;
import controller.modules.SalesManager;
import main.CharactersManager;
import main.FatalError;
import main.Log;
import main.Main;
import messages.NetworkDataContainerMessage;
import messages.NetworkMessage;
import messages.UnhandledMessage;
import messages.character.CharacterLoadingCompleteMessage;
import messages.connection.AuthenticationTicketMessage;
import messages.connection.BasicCharactersListMessage;
import messages.connection.ChannelEnablingMessage;
import messages.connection.CharacterCreationRequestMessage;
import messages.connection.CharacterCreationResultMessage;
import messages.connection.CharacterFirstSelectionMessage;
import messages.connection.CharacterNameSuggestionFailureMessage;
import messages.connection.CharacterNameSuggestionSuccessMessage;
import messages.connection.CharacterSelectedErrorMessage;
import messages.connection.CharacterSelectionMessage;
import messages.connection.CharactersListMessage;
import messages.connection.CharactersListRequestMessage;
import messages.connection.HelloConnectMessage;
import messages.connection.HelloGameMessage;
import messages.connection.IdentificationFailedBannedMessage;
import messages.connection.IdentificationFailedForBadVersionMessage;
import messages.connection.IdentificationFailedMessage;
import messages.connection.IdentificationMessage;
import messages.connection.IdentificationSuccessMessage;
import messages.connection.NicknameChoiceRequestMessage;
import messages.connection.NicknameRefusedMessage;
import messages.connection.NicknameRegistrationMessage;
import messages.connection.ObjectAveragePricesMessage;
import messages.connection.PrismsListRegisterMessage;
import messages.connection.SelectedServerDataMessage;
import messages.connection.ServerSelectionMessage;
import messages.connection.ServerStatusUpdateMessage;
import messages.connection.ServersListMessage;
import messages.connection.TrustStatusMessage;
import messages.security.ClientKeyMessage;
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
	
	protected void process(HelloConnectMessage msg) {
		this.HCM = msg;
		IdentificationMessage IM = new IdentificationMessage();
		IM.version = new VersionExtended(Main.GAME_VERSION[0], Main.GAME_VERSION[1], Main.GAME_VERSION[2], Main.GAME_VERSION[3], Main.GAME_VERSION[4], 0, 1, 1);
		IM.lang = "fr";
		IM.credentials = Encryption.encryptCredentials(Encryption.decryptReceivedKey(ByteArray.toBytes(msg.key)), this.character.infos.getLogin(), this.character.infos.getPassword(), msg.salt);
		IM.serverId = 0; // sélection du serveur automatique
		this.character.net.send(IM);
	}
	
	protected void process(IdentificationSuccessMessage msg) {
		this.ISM = msg;
	}
	
	protected void process(IdentificationFailedMessage msg) {
		if(msg.reason == IdentificationFailureReasonEnum.BANNED) {
			DatabaseConnection.updateAccountStatus(this.character.id, 1);
			CharactersManager.getInstance().connectionCallback(ConnectionResult.ACCOUNT_BANNED_DEFINITIVELY, "Account banned definitively.");
			Log.err("Account with id = " + this.character.id + " is banned definitively.");
		}
		else {
			CharactersManager.getInstance().connectionCallback(ConnectionResult.AUTHENTIFICATION_FAILED, "Authentification failed for reason " + msg.reason + ".");
			Log.err("Authentification failed for character with id = " + this.character.id + " for reason " + msg.reason + ".");
		}
	}
	
	protected void process(IdentificationFailedForBadVersionMessage msg) {
		Main.exit("Authentification failed for bad version, need to update.");
	}
	
	protected void process(IdentificationFailedBannedMessage msg) {
		CharactersManager.getInstance().connectionCallback(ConnectionResult.ACCOUNT_BANNED_TEMPORARILY, "Account banned temporarily.");
		Log.err("Account with id = " + this.character.id + " is banned temporarily.");
	}
	
	protected void process(NicknameRegistrationMessage msg) {
		NicknameChoiceRequestMessage NCRM = new NicknameChoiceRequestMessage();
		NCRM.nickname = getRandomNickname(20);
		this.character.net.send(NCRM);
		this.character.log.p("Nickname asked, sending a generated nickname.");
	}
	
	protected void process(NicknameRefusedMessage msg) {
		NicknameChoiceRequestMessage NCRM = new NicknameChoiceRequestMessage();
		NCRM.nickname = getRandomNickname(20);
		this.character.net.send(NCRM);
		this.character.log.p("Nickname refused, sending a new generated nickname.");
	}
	
	protected void process(ServersListMessage msg) {
		int serverId = this.character.infos.getServerId();
		boolean isSelectable = false;
		String str = null;
		ConnectionResult status = null;
		for(GameServerInformations server : msg.servers) {
			if(server.id == serverId) {
				if(server.status == ServerStatusEnum.OFFLINE) {
					str = "Server " + ServerEnum.getServerName(serverId) + " is offline.";
					status = ConnectionResult.SERVER_OFFLINE_OR_FULL;
				}
				else if(server.status == ServerStatusEnum.FULL) {
					str = "Server " + ServerEnum.getServerName(serverId) + " is full.";
					status = ConnectionResult.SERVER_OFFLINE_OR_FULL;
				}
				else if(server.status == ServerStatusEnum.SAVING) {
					str = "Server " + ServerEnum.getServerName(serverId) + " is saving.";
					status = ConnectionResult.SERVER_SAVING;
				}
				else if(server.status != ServerStatusEnum.ONLINE) {
					str = "Server " + ServerEnum.getServerName(serverId) + " status = " + server.status + ".";
					status = ConnectionResult.UNKNOWN;
				}
				else if(server.isSelectable == false) {
					str = "Server " + ServerEnum.getServerName(serverId) + " is unselectable.";
					status = ConnectionResult.SERVER_UNSELECTABLE;
					DatabaseConnection.updateAccountStatus(this.character.id, 2);
				}
				else
					isSelectable = true;
				break;
			}
		}
		if(isSelectable) {
			ServerSelectionMessage SSM = new ServerSelectionMessage();
			SSM.serverId = serverId;
			this.character.net.send(SSM);
		}
		else {
			Log.warn(str);
			CharactersManager.getInstance().connectionCallback(status, str);
		}
	}
	
	protected void process(ServerStatusUpdateMessage msg) {
		int serverId = this.character.infos.getServerId();
		if(msg.server.id == serverId && msg.server.isSelectable) {	
			ServerSelectionMessage SSM = new ServerSelectionMessage();
			SSM.serverId = serverId;
			this.character.net.send(SSM);
		}
	}
	
	protected void process(SelectedServerDataMessage msg) {
		this.ticket = msg.ticket;
		this.character.net.setGameServerIP(msg.address);
	}
	
	protected void process(HelloGameMessage msg) {
		AuthenticationTicketMessage ATM = new AuthenticationTicketMessage();
		ATM.lang = "fr";
		ATM.ticket = new String(Encryption.decodeWithAES(ByteArray.toBytes(this.ticket)));
		this.character.net.send(ATM);
	}
	
	protected void process(RawDataMessage msg) {
		NetworkMessage CIM = ServerInterface.getInstance().emulateServer(this.character.infos.getLogin(), this.character.infos.getPassword(), this.HCM, this.ISM, msg, character.id);
		CIM.deserialize(); // exception
		this.character.net.send(CIM);
	}
	
	protected void process(TrustStatusMessage msg) {
		CharactersListRequestMessage CLRM = new CharactersListRequestMessage();
		this.character.net.send(CLRM);
	}
	
	protected void process(CharactersListMessage msg) {
		if(msg.characters.length == 0)
			this.character.net.send(new UnhandledMessage("CharacterNameSuggestionRequestMessage"));
		else
			selectCharacter(msg.characters);
	}
	
	protected void process(BasicCharactersListMessage msg) {
		if(msg.characters.length == 0)
			this.character.net.send(new UnhandledMessage("CharacterNameSuggestionRequestMessage"));
		else
			selectCharacter(msg.characters);
	}
	
	protected void process(CharacterNameSuggestionSuccessMessage msg) {
		Random random = new Random();
		if(msg.suggestion.indexOf("-") == -1) {
			CharacterCreationRequestMessage CCRM = new CharacterCreationRequestMessage();
			CCRM.name = msg.suggestion;
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
	
	protected void process(CharacterCreationResultMessage msg) {
		if(msg.result != 0)
			throw new FatalError("Character creation has failed, error id : " + msg.result + ".");
		else
			this.character.infos.setFirstSelection(true);
	}
	
	protected void process(CharacterNameSuggestionFailureMessage msg) {
		throw new FatalError("Generation of character name suggestion has failed.");
	}
	
	protected void process(CharacterLoadingCompleteMessage msg) {
		this.character.infos.inGame(true);
		this.character.start(); // démarrage du thread contrôleur
		CharactersManager.getInstance().connectionCallback(ConnectionResult.SUCCESS, "Character connected in game.");
		Log.info("Character with id = " + this.character.id + " connected in game.");
		
		this.character.net.send(new UnhandledMessage("FriendsGetListMessage"));
		this.character.net.send(new UnhandledMessage("IgnoredGetListMessage"));
		this.character.net.send(new UnhandledMessage("SpouseGetInformationsMessage"));
		this.character.net.send(new ClientKeyMessage());
		this.character.net.send(new UnhandledMessage("GameContextCreateRequestMessage"));
		if(!SalesManager.averagePricesAreSet(this.character.infos.getServerId()))
			this.character.net.send(new UnhandledMessage("ObjectAveragePricesGetMessage"));
		this.character.net.send(new UnhandledMessage("QuestListRequestMessage"));
		this.character.net.send(new PrismsListRegisterMessage());
		this.character.net.send(new ChannelEnablingMessage());
	}
	
	protected void process(CharacterSelectedErrorMessage msg) {
		throw new FatalError("Error at the character selection.");
	}
	
	protected void process(NetworkDataContainerMessage msg) {
		this.character.net.processNetworkDataContainerMessage(msg);
	}
	
	protected void process(ObjectAveragePricesMessage msg) {
		SalesManager.setAveragePrices(this.character.infos.getServerId(), msg.ids, msg.avgPrices);
	}
	
	private void selectCharacter(CharacterBaseInformations[] characters) {
		if(characters.length > 1)
			throw new FatalError("Too many characters on the server.");
		CharacterBaseInformations characterInfos = characters[0];
		if(this.character.infos.getBreed() != characterInfos.breed)
			throw new FatalError("Incoherent character breed.");
		this.character.infos.setCharacterId(characterInfos.id); // on suppose qu'il n'y a qu'un seul perso sur le serveur
		this.character.infos.setCharacterName(characterInfos.name);
		this.character.infos.setLevel(characterInfos.level);
		if(this.character.infos.firstSelection()) {
			if(!DatabaseConnection.newCharacter(characterInfos.name, this.character.infos.getServerId(), this.character.id, characterInfos.level, characterInfos.breed))
				throw new FatalError("Error when adding new character into the database.");
			CharacterFirstSelectionMessage CFSM = new CharacterFirstSelectionMessage();
			CFSM.doTutorial = false;
			CFSM.id = this.character.infos.getCharacterId();
			this.character.net.send(CFSM);
			this.character.infos.setFirstSelection(false);
		}
		else {
			DatabaseConnection.updateCharacterLevel(characterInfos.name, this.character.infos.getServerId(), characterInfos.level);
			CharacterSelectionMessage CSM = new CharacterSelectionMessage();
			CSM.id = this.character.infos.getCharacterId();
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
	
	private int[] getRandomColorVector() {
		int[] colors = new int[MAX_PLAYER_COLOR];
		Random random = new Random();
		for(int i = 0; i < MAX_PLAYER_COLOR; ++i)
			colors[i] = random.nextInt(16777215); // nombre maximal d'une couleur
		return colors;
	}
}