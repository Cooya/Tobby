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
import gamedata.enums.TextInformationTypeEnum;
import gui.Controller;
import main.FatalError;
import main.Instance;
import main.Main;
import messages.EmptyMessage;
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
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.context.SystemMessageDisplayMessage;
import messages.context.TextInformationMessage;
import messages.interactions.InteractiveUseErrorMessage;
import messages.security.AccountLoggingKickedMessage;
import messages.security.CheckFileMessage;
import messages.security.CheckFileRequestMessage;
import messages.security.ClientKeyMessage;
import messages.security.PopupWarningMessage;

public class RoleplayContextFrame extends Frame {

	public RoleplayContextFrame(Instance instance, Character character) {
		super(instance, character);
	}
	
	protected void process(BasicWhoIsMessage BWIM) {
		this.instance.log.p("Whois response received.");
		if(BWIM.playerName == Main.MODERATOR_NAME && BWIM.playerState != 0)
			Controller.getInstance().deconnectAllInstances("The moderator is online.", false, false);
		else
			this.character.updateState(CharacterState.WHOIS_RESPONSE, true);
	}
	
	protected void process(TextInformationMessage TIM) {
		if(TIM.msgType == 1 && TIM.msgId == 245) // limite de 200 combats par jour atteinte
			Controller.getInstance().deconnectCurrentInstance("Limit of 200 fights per day reached.", false, false);
		else {
			this.instance.log.p("Text information received, reading...");
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
				this.instance.log.p("Information message " + (TIM.msgType * 10000 + TIM.msgId) + " cannot be found.");
				if(TIM.msgType == TextInformationTypeEnum.TEXT_INFORMATION_ERROR)
					textId = InfoMessage.getInfoMessageById(10231).textId;
				else
					textId = InfoMessage.getInfoMessageById(207).textId;
				parameters = new String[1];
				parameters[0] = TIM.msgId;
			}
			String messageContent = I18n.getText(textId);
			if(messageContent != null)
				//this.instance.log.p(ParamsDecoder.applyParams(msgContent, parameters));
				this.instance.log.p(messageContent);
			else
				this.instance.log.p("There is no message for id " + (TIM.msgType * 10000 + TIM.msgId) + ".");
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
		this.instance.log.p(str);
	}
	
	protected void process(CharacterLoadingCompleteMessage CLCM) {
		this.instance.log.graphicalFrame.setFightsWonLabel(0);
		this.instance.log.graphicalFrame.setFightsLostLabel(0);
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
		CKM.serialize(this.instance.id);
		instance.outPush(EM1);
		instance.outPush(EM2);
		instance.outPush(EM3);
		instance.outPush(CKM);
		instance.outPush(EM4);
		//instance.outPush(EM5);
		instance.outPush(EM6);
		instance.outPush(PLRM);
		instance.outPush(CEM);
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
		this.character.infos.loadSpellList(SLM.spells);
	}
	
	protected void process(CurrentMapMessage CMM) {
		this.character.infos.currentMap = MapsCache.loadMap(CMM.mapId);
		MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
		MIRM.serialize(this.character.infos.currentMap.id);
		instance.outPush(MIRM);
	}
	
	protected void process(CharacterStatsListMessage CSLM) {
		this.character.infos.stats = CSLM.stats;
		this.instance.log.graphicalFrame.setEnergyLabel(this.character.infos.stats.energyPoints, this.character.infos.stats.maxEnergyPoints);
		this.instance.log.graphicalFrame.setKamasLabel(this.character.infos.stats.kamas);
		this.instance.log.graphicalFrame.setExperienceLabel((int) this.character.infos.stats.experience, (int) this.character.infos.stats.experienceNextLevelFloor);
	}
	
	protected void process(CharacterLevelUpMessage CLUM) {
		this.character.infos.level = CLUM.newLevel;
		this.instance.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
		this.character.updateState(CharacterState.LEVEL_UP, true);
	}
	
	protected void process(MapComplementaryInformationsDataMessage MCIDM) {
		this.character.roleplayContext.newContextActors(MCIDM.actors);
		this.instance.log.p("Current map : " + MapPosition.getMapPositionById(this.character.infos.currentMap.id) + ".\nCurrent cell id : " + this.character.infos.currentCellId + ".\nCurrent area id : " + this.character.infos.currentMap.subareaId + ".");
		this.instance.log.graphicalFrame.setMapLabel(String.valueOf(MapPosition.getMapPositionById(this.character.infos.currentMap.id)));
		this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
		this.character.updatePosition(this.character.infos.currentMap, this.character.infos.currentCellId);
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
		if(GMMM.actorId == this.character.infos.characterId) {
			this.character.infos.currentCellId = position;
			this.instance.log.p("Next cell id after movement : " + position + ".");
			this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
			this.character.mvt.updatePosition(this.character.infos.currentCellId);
			this.character.updateState(CharacterState.CAN_MOVE, true);
		}
	}
	
	protected void process(LifePointsRegenBeginMessage LPRBM) {
		this.character.infos.regenRate = LPRBM.regenRate;
	}
	
	protected void process(InventoryWeightMessage IWM) {
		this.character.infos.weight = IWM.weight;
		this.character.infos.weightMax = IWM.weightMax;
		this.instance.log.graphicalFrame.setWeightLabel(this.character.infos.weight, this.character.infos.weightMax);
		if(this.character.infos.weightMaxAlmostReached()) {
			this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
			this.instance.log.p("Inventory weight maximum almost reached, need to empty.");
		}
	}
	
	protected void process(PlayerStatusUpdateMessage PSUM) {
		if(PSUM.playerId == this.character.infos.characterId) {
			this.character.infos.status = PSUM.status.statusId;
			this.instance.log.p("New status : " + this.character.infos.status + ".");
		}
	}
	
	protected void process(GameRolePlayPlayerLifeStatusMessage GRPPLSM) {
		this.character.infos.healthState = GRPPLSM.state;
	}
	
	protected void process(PopupWarningMessage PWM) {
		this.instance.log.p("Popup received by " + PWM.author + " that contains : \"" + PWM.content + "\".");
		try {
			Thread.sleep(PWM.lockDuration * 1000); // attendre le nombre de secondes indiqué
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	protected void process(CheckFileRequestMessage CFRM) {
		this.instance.log.p("Request for check file \"" + CFRM.filename + "\" received.");
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
		this.instance.outPush(CFM);
		this.instance.log.p(CFM.filenameHash);
		this.instance.log.p(String.valueOf(CFM.type));
		this.instance.log.p(CFM.value);
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
}