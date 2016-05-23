package frames;

import java.io.File;
import java.security.MessageDigest;

import utilities.ByteArray;
import controller.CharacterState;
import controller.characters.Character;
import controller.modules.SalesManager;
import gamedata.d2i.I18n;
import gamedata.d2o.modules.InfoMessage;
import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import gamedata.enums.DialogTypeEnum;
import gamedata.enums.TextInformationTypeEnum;
import main.Controller;
import main.FatalError;
import main.Log;
import messages.UnhandledMessage;
import messages.character.BasicWhoIsMessage;
import messages.character.CharacterLevelUpMessage;
import messages.character.CharacterLoadingCompleteMessage;
import messages.character.CharacterStatsListMessage;
import messages.character.GameRolePlayPlayerLifeStatusMessage;
import messages.character.LifePointsRegenBeginMessage;
import messages.character.PlayerStatusUpdateMessage;
import messages.character.SpellListMessage;
import messages.connection.ChannelEnablingMessage;
import messages.connection.PrismsListRegisterMessage;
import messages.context.CurrentMapMessage;
import messages.context.GameContextCreateMessage;
import messages.context.GameContextRemoveElementMessage;
import messages.context.GameMapMovementMessage;
import messages.context.GameMapNoMovementMessage;
import messages.context.GameRolePlayPlayerFightFriendlyAnswerMessage;
import messages.context.GameRolePlayPlayerFightFriendlyRequestedMessage;
import messages.context.GameRolePlayShowActorMessage;
import messages.context.LeaveDialogMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.context.SystemMessageDisplayMessage;
import messages.context.TextInformationMessage;
import messages.interactions.InteractiveUseErrorMessage;
import messages.interactions.NpcDialogCreationMessage;
import messages.interactions.NpcGenericActionFailureMessage;
import messages.security.AccountLoggingKickedMessage;
import messages.security.CheckFileMessage;
import messages.security.CheckFileRequestMessage;
import messages.security.ClientKeyMessage;
import messages.security.PopupWarningMessage;

public class RoleplayContextFrame extends Frame {

	public RoleplayContextFrame(Character character) {
		super(character);
	}
	
	protected void process(BasicWhoIsMessage msg) {
		this.character.log.p("Whois response received.");
		if(msg.playerState != 0)
			Controller.getInstance().serverDeconnection("The moderator is online.", this.character.infos.getServerId(), true, false);
		else
			this.character.updateState(CharacterState.WHOIS_RESPONSE, true);
	}
	
	protected void process(NpcDialogCreationMessage msg) {
		this.character.log.p("NPC dialog displayed.");
		this.character.updateState(CharacterState.DIALOG_DISPLAYED, true);
	}
	
	protected void process(TextInformationMessage msg) {
		if(msg.msgType == 1 && msg.msgId == 245) // limite de 200 combats par jour atteinte
			Controller.getInstance().deconnectCurrentCharacter("Limit of 200 fights per day reached.", false, false);
		else {
			this.character.log.p("Text information received, reading...");
			InfoMessage infoMessage = InfoMessage.getInfoMessageById((msg.msgType * 10000) + msg.msgId);
			int textId;
			Object[] parameters;
			if(infoMessage != null) {
				textId = infoMessage.textId;
				if(msg.parameters.size() > 0) {
					String parameter = msg.parameters.get(0);
					if(parameter != null && parameter.indexOf("~") == -1)
						parameters = parameter.split("~");
					else
						parameters = (String[]) msg.parameters.toArray();
				}
			}
			else {
				this.character.log.p("Information message " + (msg.msgType * 10000 + msg.msgId) + " cannot be found.");
				if(msg.msgType == TextInformationTypeEnum.TEXT_INFORMATION_ERROR)
					textId = InfoMessage.getInfoMessageById(10231).textId;
				else
					textId = InfoMessage.getInfoMessageById(207).textId;
				parameters = new String[1];
				parameters[0] = msg.msgId;
			}
			String messageContent = I18n.getText(textId);
			if(messageContent != null)
				//this.character.log.p(ParamsDecoder.applyParams(msgContent, parameters));
				this.character.log.p(messageContent);
			else
				this.character.log.p("There is no message for id " + (msg.msgType * 10000 + msg.msgId) + ".");
		}
	}
	
	protected void process(SystemMessageDisplayMessage msg) {
		InfoMessage infoMsg = InfoMessage.getInfoMessageById(40000 + msg.msgId);
		String str;
		if(infoMsg != null) {
			str = I18n.getText(infoMsg.textId);
			//if(str != null)
				//str = ParamsDecoder.applyParams(str, msg.parameters);
		}
		else
			str = "Information message " + (40000 + msg.msgId) + " cannot be found.";
		this.character.log.p(str);
	}
	
	protected void process(LeaveDialogMessage msg) {
		if(msg.dialogType == DialogTypeEnum.DIALOG_DIALOG) {
			this.character.log.p("Dialog window closed.");
			this.character.updateState(CharacterState.DIALOG_DISPLAYED, false);
		}
		else if(msg.dialogType == DialogTypeEnum.DIALOG_EXCHANGE) {
			this.character.log.p("Exchange closed.");
			this.character.updateState(CharacterState.IN_EXCHANGE, false);
		}
		else
			this.character.log.p("Unknown dialog window closed.");
	}
	
	protected void process(CharacterLoadingCompleteMessage msg) {
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
	
	protected void process(GameContextCreateMessage msg) {
		if(msg.context == 1 && this.character.inState(CharacterState.IN_FIGHT)) {
			this.character.updateState(CharacterState.IN_FIGHT, false);
			this.character.updateState(CharacterState.IN_GAME_TURN, false);
			this.character.fightContext.clearFightContext();
		}
		else if(msg.context == 2) {
			this.character.updateState(CharacterState.IS_LOADED, false);
			this.character.updateState(CharacterState.IN_FIGHT, true);
		}
	}
	
	protected void process(SpellListMessage msg) {
		this.character.infos.setSpellList(msg.spells);
	}
	
	protected void process(CurrentMapMessage msg) {
		this.character.infos.setCurrentMap(MapsCache.loadMap(msg.mapId));
		MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
		MIRM.mapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(MIRM);
	}
	
	protected void process(CharacterStatsListMessage msg) {
		this.character.infos.setStats(msg.stats);
		this.character.inventory.setKamas(msg.stats.kamas);
	}
	
	protected void process(CharacterLevelUpMessage msg) {
		this.character.infos.setLevel(msg.newLevel);
		this.character.updateState(CharacterState.LEVEL_UP, true);
	}
	
	protected void process(MapComplementaryInformationsDataMessage msg) {
		this.character.roleplayContext.newContextActors(msg.actors);
		this.character.roleplayContext.newContextInteractives(msg.interactiveElements);
		int currentCellId = this.character.infos.getCurrentCellId();
		Map currentMap = this.character.infos.getCurrentMap();
		this.character.log.p("Current map : " + MapPosition.getMapPositionById(currentMap.id) + ".\nCurrent cell id : " + currentCellId + ".\nCurrent area id : " + currentMap.subareaId + ".");
		this.character.updatePosition(currentMap, currentCellId);
		this.character.updateState(CharacterState.IS_LOADED, true);
	}
	
	protected void process(GameRolePlayShowActorMessage msg) {
		this.character.roleplayContext.addContextActor(msg.informations);
		this.character.updateState(CharacterState.NEW_ACTOR_ON_MAP, true);
	}
	
	protected void process(GameContextRemoveElementMessage msg) {
		this.character.roleplayContext.removeContextActor(msg.id);
	}
	
	protected void process(GameMapMovementMessage msg) {
		int position = msg.keyMovements.lastElement();
		this.character.roleplayContext.updateContextActorPosition(msg.actorId, position);
		if(msg.actorId == this.character.infos.getCharacterId()) {
			this.character.infos.setCurrentCellId(position);
			this.character.log.p("Next cell id after movement : " + position + ".");
			this.character.mvt.updatePosition(position);
		}
	}
	
	protected void process(LifePointsRegenBeginMessage msg) {
		this.character.infos.setRegenRate(msg.regenRate);
	}
	
	protected void process(PlayerStatusUpdateMessage msg) {
		if(msg.playerId == this.character.infos.getCharacterId())
			this.character.log.p("New status : " + msg.status.statusId + ".");
	}
	
	protected void process(GameRolePlayPlayerLifeStatusMessage msg) {
		this.character.infos.setHealthState(msg.state);
	}
	
	protected void process(GameRolePlayPlayerFightFriendlyRequestedMessage msg) {
		this.character.log.p("Player fight request received.");
		this.character.updateState(CharacterState.PENDING_DEMAND, true);
		try {
			Thread.sleep(2000); // pour faire un peu normal
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		GameRolePlayPlayerFightFriendlyAnswerMessage answer = new GameRolePlayPlayerFightFriendlyAnswerMessage();
		answer.fightId = msg.fightId;
		answer.accept = false;
		this.character.net.send(answer);
		this.character.updateState(CharacterState.PENDING_DEMAND, false);
	}
	
	protected void process(PopupWarningMessage msg) {
		this.character.log.p("Popup received by " + msg.author + " that contains : \"" + msg.content2 + "\".");
		try {
			Thread.sleep(msg.lockDuration * 1000); // attendre le nombre de secondes indiqué
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	protected void process(CheckFileRequestMessage msg) {
		this.character.log.p("Request for check file \"" + msg.filename + "\" received.");
		CheckFileMessage CFM = new CheckFileMessage();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] filenameBytes = msg.filename.getBytes("UTF-8");
			CFM.filenameHash = new String(md.digest(filenameBytes), "UTF-8");
		} catch(Exception e) {
			throw new FatalError(e);
		}
		File file = new File(msg.filename);
		if(file == null || !file.exists())
			CFM.value = "-1";
		else {
			ByteArray buffer = ByteArray.fileToByteArray(msg.filename);
			if(buffer == null)
				CFM.value = "-1";
			if(CFM.value.equals("")) {
				if(msg.type == 0)
					CFM.value = String.valueOf(buffer.getSize());
				else if(msg.type == 1)
					try {
						CFM.value = new String(md.digest(buffer.bytes()), "UTF-8");
					} catch(Exception e) {
						throw new FatalError(e);
					}
			}
		}
		CFM.type = msg.type;
		this.character.net.send(CFM);
		this.character.log.p(CFM.filenameHash);
		this.character.log.p(String.valueOf(CFM.type));
		this.character.log.p(CFM.value);
	}
	
	protected void process(GameMapNoMovementMessage msg) {
		throw new FatalError("Movement refused by server.");
	}
	
	protected void process(InteractiveUseErrorMessage msg) {
		throw new FatalError("Error during use of a interactive.");
	}
	
	protected void process(AccountLoggingKickedMessage msg) {
		throw new FatalError("Kicked from the game server.");
	}
	
	protected void process(NpcGenericActionFailureMessage msg) {
		throw new FatalError("Error during dialog with a NPC.");
	}
}