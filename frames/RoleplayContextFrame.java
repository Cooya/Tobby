package frames;

import java.io.File;
import java.security.MessageDigest;

import utilities.ByteArray;
import controller.CharacterController;
import controller.CharacterState;
import controller.FighterController;
import gamedata.context.TextInformationTypeEnum;
import gamedata.d2i.I18n;
import gamedata.d2o.modules.InfoMessage;
import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.MapsCache;
import gui.Controller;
import main.FatalError;
import main.Instance;
import main.InterClientKeyManager;
import main.Main;
import messages.EmptyMessage;
import messages.Message;
import messages.character.BasicWhoIsMessage;
import messages.character.CharacterLevelUpMessage;
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
import messages.context.GameRolePlayShowActorMessage;
import messages.context.MapComplementaryInformationsDataMessage;
import messages.context.MapInformationsRequestMessage;
import messages.context.TextInformationMessage;
import messages.exchanges.ExchangeLeaveMessage;
import messages.exchanges.ExchangeRequestedTradeMessage;
import messages.parties.PartyAcceptInvitationMessage;
import messages.parties.PartyInvitationMessage;
import messages.parties.PartyJoinMessage;
import messages.parties.PartyMemberInFightMessage;
import messages.security.CheckFileMessage;
import messages.security.CheckFileRequestMessage;
import messages.security.ClientKeyMessage;
import messages.security.PopupWarningMessage;

public class RoleplayContextFrame extends Frame {
	private Instance instance;
	private CharacterController character;

	public RoleplayContextFrame(Instance instance, CharacterController character) {
		this.instance = instance;
		this.character = character;
	}

	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 180 : // BasicWhoIsMessage
				this.instance.log.p("Whois response received.");
				BasicWhoIsMessage BWIM = new BasicWhoIsMessage(msg);
				if(BWIM.playerName == Main.MODERATOR_NAME && BWIM.playerState != 0)
					Controller.deconnectAllInstances("The moderator is online.");
				else
					this.character.updateState(CharacterState.WHOIS_RESPONSE, true);
				return true;
			case 780 : // TextInformationMessage
				TextInformationMessage TIM = new TextInformationMessage(msg);
				if(TIM.msgType == 1 && TIM.msgId == 245) // limite de 200 combats par jour atteinte
					Controller.deconnectInstance("Limit of 200 fights per day reached.");
				else {
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
				return true;	
			case 6471 : // CharacterLoadingCompleteMessage
				this.instance.log.graphicalFrame.setFightsWonLabel(0);
				this.instance.log.graphicalFrame.setFightsLostLabel(0);
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
				CKM.serialize(ICKM, this.instance.id);
				instance.outPush(EM1);
				instance.outPush(EM2);
				instance.outPush(EM3);
				instance.outPush(CKM);
				instance.outPush(EM4);
				//instance.outPush(EM5);
				instance.outPush(EM6);
				instance.outPush(PLRM);
				instance.outPush(CEM);
				return true;
			case 200 : // GameContextCreateMessage
				GameContextCreateMessage GCCM = new GameContextCreateMessage(msg);
				if(GCCM.context == 1 && this.character.inState(CharacterState.IN_FIGHT)) {
					this.character.updateState(CharacterState.IN_FIGHT, false);
					this.character.updateState(CharacterState.IN_GAME_TURN, false);
					this.instance.quitFightContext();
					if(this.character instanceof FighterController)
						((FighterController) this.character).fightContext.clearFightContext();
				}
				else if(GCCM.context == 2) {
					this.character.updateState(CharacterState.IS_LOADED, false);
					this.character.updateState(CharacterState.IN_FIGHT, true);
					this.instance.startFightContext();
				}
				return true;
			case 1200 : // SpellListMessage
				SpellListMessage SLM = new SpellListMessage(msg);
				SLM.deserialize();
				this.character.infos.loadSpellList(SLM.spells);
				return true;	
			case 220 : // CurrentMapMessage
				CurrentMapMessage CMM = new CurrentMapMessage(msg);
				this.character.infos.currentMap = MapsCache.loadMap(CMM.mapId);
				MapInformationsRequestMessage MIRM = new MapInformationsRequestMessage();
				MIRM.serialize(this.character.infos.currentMap.id);
				instance.outPush(MIRM);
				return true;
			case 500 : // CharacterStatsListMessage
				CharacterStatsListMessage CSLM = new CharacterStatsListMessage(msg);
				this.character.infos.stats = CSLM.stats;
				this.instance.log.graphicalFrame.setEnergyLabel(this.character.infos.stats.energyPoints, this.character.infos.stats.maxEnergyPoints);
				this.instance.log.graphicalFrame.setKamasLabel(this.character.infos.stats.kamas);
				this.instance.log.graphicalFrame.setExperienceLabel((int) this.character.infos.stats.experience, (int) this.character.infos.stats.experienceNextLevelFloor);
				return true;
			case 5670 : // CharacterLevelUpMessage
				CharacterLevelUpMessage CLUM = new CharacterLevelUpMessage(msg);
				this.character.infos.level = CLUM.newLevel;
				this.instance.log.graphicalFrame.setNameLabel(this.character.infos.characterName, this.character.infos.level);
				this.character.updateState(CharacterState.LEVEL_UP, true);
				return true;
			case 226 : // MapComplementaryInformationsDataMessage
				MapComplementaryInformationsDataMessage MCIDM = new MapComplementaryInformationsDataMessage(msg);
				this.character.roleplayContext.newContextActors(MCIDM.actors);
				this.instance.log.p("Current map : " + MapPosition.getMapPositionById(this.character.infos.currentMap.id) + ".\nCurrent cell id : " + this.character.infos.currentCellId + ".\nCurrent area id : " + this.character.infos.currentMap.subareaId + ".");
				this.instance.log.graphicalFrame.setMapLabel(String.valueOf(MapPosition.getMapPositionById(this.character.infos.currentMap.id)));
				this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
				this.character.updatePosition(this.character.infos.currentMap, this.character.infos.currentCellId);
				this.character.updateState(CharacterState.IS_LOADED, true);
				return true;
			case 5632 : // GameRolePlayShowActorMessage
				GameRolePlayShowActorMessage GRPSAM = new GameRolePlayShowActorMessage(msg);
				this.character.roleplayContext.addContextActor(GRPSAM.informations);
				this.character.updateState(CharacterState.NEW_ACTOR_ON_MAP, true);
				return true;
			case 251 : // GameContextRemoveElementMessage
				GameContextRemoveElementMessage GCREM = new GameContextRemoveElementMessage(msg);
				this.character.roleplayContext.removeContextActor(GCREM.id);
				return true;
			case 951 : // GameMapMovementMessage
				GameMapMovementMessage GMMM = new GameMapMovementMessage(msg);
				int position = GMMM.keyMovements.lastElement();
				this.character.roleplayContext.updateContextActorPosition(GMMM.actorId, position);
				if(GMMM.actorId == this.character.infos.characterId) {
					this.character.infos.currentCellId = position;
					this.instance.log.p("Next cell id after movement : " + position + ".");
					this.instance.log.graphicalFrame.setCellLabel(String.valueOf(this.character.infos.currentCellId));
					this.character.updatePosition(this.character.infos.currentCellId);
					this.character.updateState(CharacterState.CAN_MOVE, true);
				}
				return true;
			case 5684 : // LifePointsRegenBeginMessage
				LifePointsRegenBeginMessage LPRBM = new LifePointsRegenBeginMessage(msg);
				this.character.infos.regenRate = LPRBM.regenRate;
				return true;
			case 954 : // GameMapNoMovementMessage
				throw new FatalError("Movement refused by server.");
			case 3009 : // InventoryWeightMessage
				InventoryWeightMessage IWM = new InventoryWeightMessage(msg);
				this.character.infos.weight = IWM.weight;
				this.character.infos.weightMax = IWM.weightMax;
				this.instance.log.graphicalFrame.setWeightLabel(this.character.infos.weight, this.character.infos.weightMax);
				if(this.character.infos.weightMaxAlmostReached()) {
					this.character.updateState(CharacterState.NEED_TO_EMPTY_INVENTORY, true);
					this.instance.log.p("Inventory weight maximum almost reached, need to empty.");
				}
				return true;
			case 6386 : // PlayerStatusUpdateMessage
				PlayerStatusUpdateMessage PSUM = new PlayerStatusUpdateMessage(msg);
				if(PSUM.playerId == this.character.infos.characterId) {
					this.character.infos.status = PSUM.status.statusId;
					this.instance.log.p("New status : " + this.character.infos.status + ".");
				}
				return true;
			case 5669 : // GameRolePlayPlayerLifeStatusMessage
				GameRolePlayPlayerLifeStatusMessage GRPPLSM = new GameRolePlayPlayerLifeStatusMessage(msg);
				this.character.infos.healthState = GRPPLSM.state;
				return true;
			case 5523 : // ExchangeRequestedTradeMessage
				ExchangeRequestedTradeMessage ERTM = new ExchangeRequestedTradeMessage(msg);
				this.character.roleplayContext.actorDemandingExchange = ERTM.source;
				this.character.updateState(CharacterState.PENDING_DEMAND, true);
				return true;
			case 6129 : // ExchangeStartedWithPodsMessage
				this.instance.startExchangeContext();
				this.character.updateState(CharacterState.PENDING_DEMAND, false);
				this.character.updateState(CharacterState.IN_EXCHANGE, true);
				return true;
			case 5628 : // ExchangeLeaveMessage
				ExchangeLeaveMessage ELM = new ExchangeLeaveMessage(msg);
				this.character.roleplayContext.lastExchangeOutcome = ELM.success;
				if(this.character.inState(CharacterState.IN_EXCHANGE)) { // on quitte un échange
					this.instance.quitExchangeContext();
					this.character.updateState(CharacterState.IN_EXCHANGE, false);
				}
				else { // on refuse un échange
					this.character.roleplayContext.actorDemandingExchange = 0;
					this.character.updateState(CharacterState.PENDING_DEMAND, false);
				}
				return true;
			case 5586 : // PartyInvitationMessage
				PartyInvitationMessage PIM = new PartyInvitationMessage(msg);
				this.instance.log.p("Party invitation received.");
				if(Controller.isWorkmate(PIM.fromId)) {
					PartyAcceptInvitationMessage PAIM = new PartyAcceptInvitationMessage();
					PAIM.partyId = PIM.partyId;
					PAIM.serialize();
					this.instance.outPush(PAIM);
					this.instance.log.p("Party invitation acceptation sent.");
				}
				return true;
			case 5576 : // PartyJoinMessage
				PartyJoinMessage PJM = new PartyJoinMessage(msg);
				this.instance.log.p("Party joined.");
				this.character.setPartyId(PJM.partyId);
				this.character.updateState(CharacterState.IN_PARTY, true);
				return true;
			case 6306 : // PartyNewMemberMessage
				this.character.updateState(CharacterState.NEW_PARTY_MEMBER, true);
				return true;
			case 6342 : // PartyMemberInFightMessage
				PartyMemberInFightMessage PMIFM = new PartyMemberInFightMessage(msg);
				this.character.roleplayContext.currentCaptainFightId = PMIFM.fightId;
				this.character.updateState(CharacterState.FIGHT_LAUNCHED, true);
				return true;
			case 5594 : // PartyLeaveMessage
			case 6261 : // PartyDeletedMessage
				this.character.setPartyId(0);
				this.character.updateState(CharacterState.IN_PARTY, false);
				this.instance.log.p("Party left.");
				return true;
			case 6134 : // PopupWarningMessage
				PopupWarningMessage PWM = new PopupWarningMessage(msg);
				this.instance.log.p("Popup received by " + PWM.author + " that contains : \"" + PWM.content + "\".");
				try {
					Thread.sleep(PWM.lockDuration * 1000); // attendre le nombre de secondes indiqué
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return true;
			case 6154 : // CheckFileRequestMessage
				CheckFileRequestMessage CFRM = new CheckFileRequestMessage(msg);
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
				return true;
			case 6029 : // AccountLoggingKickedMessage
				this.instance.log.p("Banned by a moderator.");
				return true;
		}
		return false;
	}
}