package frames;

import java.io.File;
import java.security.MessageDigest;

import utilities.ByteArray;
import controller.CharacterState;
import controller.characters.Character;
import controller.characters.Fighter;
import gamedata.d2i.I18n;
import gamedata.d2o.modules.InfoMessage;
import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import gamedata.enums.TextInformationTypeEnum;
import main.Controller;
import main.FatalError;
import main.Log;
import main.Main;
import messages.UnhandledMessage;
import messages.character.BasicWhoIsMessage;
import messages.character.CharacterLevelUpMessage;
import messages.character.CharacterLoadingCompleteMessage;
import messages.character.CharacterStatsListMessage;
import messages.character.GameRolePlayPlayerLifeStatusMessage;
import messages.character.InventoryWeightMessage;
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
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.context.SystemMessageDisplayMessage;
import messages.context.TextInformationMessage;
import messages.interactions.InteractiveUseErrorMessage;
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
	
	protected void process(BasicWhoIsMessage BWIM) {
		this.character.log.p("Whois response received.");
		if(BWIM.playerName == Main.MODERATOR_NAME && BWIM.playerState != 0)
			Controller.getInstance().globalDeconnection("The moderator is online.", true, false);
		else
			this.character.updateState(CharacterState.WHOIS_RESPONSE, true);
	}
	
	protected void process(TextInformationMessage TIM) {
		if(TIM.msgType == 1 && TIM.msgId == 245) // limite de 200 combats par jour atteinte
			Controller.getInstance().deconnectCurrentCharacter("Limit of 200 fights per day reached.", false, false);
		else {
			this.character.log.p("Text information received, reading...");
			InfoMessage infoMessage = InfoMessage.getInfoMessageById((TIM.msgType * 10000) + TIM.msgId);
			int textId;
			Object[] parameters;
			if(infoMessage != null) {
				textId = infoMessage.textId;
				if(TIM.parameters.size() > 0) {
					String parameter = TIM.parameters.get(0);
					if(parameter != null && parameter.indexOf("~") == -1)
						parameters = parameter.split("~");
					else
						parameters = (String[]) TIM.parameters.toArray();
				}
			}
			else {
				this.character.log.p("Information message " + (TIM.msgType * 10000 + TIM.msgId) + " cannot be found.");
				if(TIM.msgType == TextInformationTypeEnum.TEXT_INFORMATION_ERROR)
					textId = InfoMessage.getInfoMessageById(10231).textId;
				else
					textId = InfoMessage.getInfoMessageById(207).textId;
				parameters = new String[1];
				parameters[0] = TIM.msgId;
			}
			String messageContent = I18n.getText(textId);
			if(messageContent != null)
				//this.character.log.p(ParamsDecoder.applyParams(msgContent, parameters));
				this.character.log.p(messageContent);
			else
				this.character.log.p("There is no message for id " + (TIM.msgType * 10000 + TIM.msgId) + ".");
		}
	}
	
	protected void process(SystemMessageDisplayMessage SMDM) {
		InfoMessage infoMsg = InfoMessage.getInfoMessageById(40000 + SMDM.msgId);
		String str;
		if(infoMsg != null) {
			str = I18n.getText(infoMsg.textId);
			//if(str != null)
				//str = ParamsDecoder.applyParams(str, msg.parameters);
		}
		else
			str = "Information message " + (40000 + SMDM.msgId) + " cannot be found.";
		this.character.log.p(str);
	}
	
	protected void process(CharacterLoadingCompleteMessage CLCM) {
		Log.info("Character with id = " + this.character.id + " connected in game.");
		
		this.character.net.send(new UnhandledMessage("FriendsGetListMessage"));
		this.character.net.send(new UnhandledMessage("IgnoredGetListMessage"));
		this.character.net.send(new UnhandledMessage("SpouseGetInformationsMessage"));
		this.character.net.send(new ClientKeyMessage());
		this.character.net.send(new UnhandledMessage("GameContextCreateRequestMessage"));
		//this.character.net.send(new UnhandledMessage("ObjectAveragePricesGetMessage"));
		this.character.net.send(new UnhandledMessage("QuestListRequestMessage"));
		this.character.net.send(new PrismsListRegisterMessage());
		this.character.net.send(new ChannelEnablingMessage());
	}
	
	protected void process(GameContextCreateMessage GCCM) {
		if(GCCM.context == 1 && this.character.inState(CharacterState.IN_FIGHT)) {
			this.character.updateState(CharacterState.IN_FIGHT, false);
			this.character.updateState(CharacterState.IN_GAME_TURN, false);
			if(this.character instanceof Fighter)
				((Fighter) this.character).fightContext.clearFightContext();
		}
		else if(GCCM.context == 2) {
			this.character.updateState(CharacterState.IS_LOADED, false);
			this.character.updateState(CharacterState.IN_FIGHT, true);
		}
	}
	
	protected void process(SpellListMessage SLM) {
		this.character.infos.setSpellList(SLM.spells);
	}
	
	protected void process(CurrentMapMessage CMM) {
		this.character.infos.setCurrentMap(MapsCache.loadMap(CMM.mapId));
		MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
		MIRM.mapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(MIRM);
	}
	
	protected void process(CharacterStatsListMessage CSLM) {
		this.character.infos.setStats(CSLM.stats);
	}
	
	protected void process(CharacterLevelUpMessage CLUM) {
		this.character.infos.setLevel(CLUM.newLevel);
		this.character.updateState(CharacterState.LEVEL_UP, true);
	}
	
	protected void process(MapComplementaryInformationsDataMessage MCIDM) {
		this.character.roleplayContext.newContextActors(MCIDM.actors);
		this.character.roleplayContext.newContextInteractives(MCIDM.interactiveElements);
		int currentCellId = this.character.infos.getCurrentCellId();
		Map currentMap = this.character.infos.getCurrentMap();
		this.character.log.p("Current map : " + MapPosition.getMapPositionById(currentMap.id) + ".\nCurrent cell id : " + currentCellId + ".\nCurrent area id : " + currentMap.subareaId + ".");
		this.character.updatePosition(currentMap, currentCellId);
		this.character.updateState(CharacterState.IS_LOADED, true);
	}
	
	protected void process(GameRolePlayShowActorMessage GRPSAM) {
		this.character.roleplayContext.addContextActor(GRPSAM.informations);
		this.character.updateState(CharacterState.NEW_ACTOR_ON_MAP, true);
	}
	
	protected void process(GameContextRemoveElementMessage GCREM) {
		this.character.roleplayContext.removeContextActor(GCREM.id);
	}
	
	protected void process(GameMapMovementMessage GMMM) {
		int position = GMMM.keyMovements.lastElement();
		this.character.roleplayContext.updateContextActorPosition(GMMM.actorId, position);
		if(GMMM.actorId == this.character.infos.getCharacterId()) {
			this.character.infos.setCurrentCellId(position);
			this.character.log.p("Next cell id after movement : " + position + ".");
			this.character.mvt.updatePosition(position);
			this.character.updateState(CharacterState.CAN_MOVE, true);
		}
	}
	
	protected void process(LifePointsRegenBeginMessage LPRBM) {
		this.character.infos.setRegenRate(LPRBM.regenRate);
	}
	
	protected void process(InventoryWeightMessage IWM) {
		this.character.infos.setWeight(IWM.weight);
		this.character.infos.setWeightMax(IWM.weightMax);
		if(this.character.infos.weightMaxAlmostReached()) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
			this.character.log.p("Inventory weight maximum almost reached, need to empty.");
		}
	}
	
	protected void process(PlayerStatusUpdateMessage PSUM) {
		if(PSUM.playerId == this.character.infos.getCharacterId())
			this.character.log.p("New status : " + PSUM.status.statusId + ".");
	}
	
	protected void process(GameRolePlayPlayerLifeStatusMessage GRPPLSM) {
		this.character.infos.setHealthState(GRPPLSM.state);
	}
	
	protected void process(GameRolePlayPlayerFightFriendlyRequestedMessage GRPPFFRM) {
		this.character.log.p("Player fight request received.");
		this.character.updateState(CharacterState.PENDING_DEMAND, true);
		try {
			Thread.sleep(2000); // pour faire un peu normal
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		GameRolePlayPlayerFightFriendlyAnswerMessage GRPPFFAM = new GameRolePlayPlayerFightFriendlyAnswerMessage();
		GRPPFFAM.fightId = GRPPFFRM.fightId;
		GRPPFFAM.accept = false;
		this.character.net.send(GRPPFFAM);
		this.character.updateState(CharacterState.PENDING_DEMAND, false);
	}
	
	protected void process(PopupWarningMessage PWM) {
		this.character.log.p("Popup received by " + PWM.author + " that contains : \"" + PWM.content2 + "\".");
		try {
			Thread.sleep(PWM.lockDuration * 1000); // attendre le nombre de secondes indiqué
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	protected void process(CheckFileRequestMessage CFRM) {
		this.character.log.p("Request for check file \"" + CFRM.filename + "\" received.");
		CheckFileMessage CFM = new CheckFileMessage();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] filenameBytes = CFRM.filename.getBytes("UTF-8");
			CFM.filenameHash = new String(md.digest(filenameBytes), "UTF-8");
		} catch(Exception e) {
			throw new FatalError(e);
		}
		File file = new File(CFRM.filename);
		if(file == null || !file.exists())
			CFM.value = "-1";
		else {
			ByteArray buffer = ByteArray.fileToByteArray(CFRM.filename);
			if(buffer == null)
				CFM.value = "-1";
			if(CFM.value.equals("")) {
				if(CFRM.type == 0)
					CFM.value = String.valueOf(buffer.getSize());
				else if(CFRM.type == 1)
					try {
						CFM.value = new String(md.digest(buffer.bytes()), "UTF-8");
					} catch(Exception e) {
						throw new FatalError(e);
					}
			}
		}
		CFM.type = CFRM.type;
		this.character.net.send(CFM);
		this.character.log.p(CFM.filenameHash);
		this.character.log.p(String.valueOf(CFM.type));
		this.character.log.p(CFM.value);
	}
	
	protected void process(GameMapNoMovementMessage GMNMM) {
		throw new FatalError("Movement refused by server.");
	}
	
	protected void process(InteractiveUseErrorMessage IUEM) {
		throw new FatalError("Error during use of a interactive.");
	}
	
	protected void process(AccountLoggingKickedMessage ALKM) {
		throw new FatalError("Kicked from the game server.");
	}
	
	protected void process(NpcGenericActionFailureMessage NGAFM) {
		throw new FatalError("Error during dialog with a NPC.");
	}
}