package messages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import main.Emulation;
import main.FatalError;
import main.Log;
import main.Main;
import utilities.BiMap;
import utilities.ByteArray;
import utilities.Reflection;

@SuppressWarnings("unchecked")
public abstract class NetworkMessage {
	private static final BiMap<Integer, String> messages = new BiMap<Integer, String>(Integer.class, String.class);
	private static final Map<Integer, Object> acknowledgementExceptions = new HashMap<Integer, Object>();
	private static final Map<String, Class<NetworkMessage>> msgClasses = new HashMap<String, Class<NetworkMessage>>();
	private static final Map<String, Object> hashedMessages = new HashMap<String, Object>();
	
	static {
		// liste des messages non acquittables
		acknowledgementExceptions.put(4, null); // IdentificationMessage
		acknowledgementExceptions.put(40, null); // ServerSelectionMessage
		acknowledgementExceptions.put(110, null); // AuthenticationTicketMessage (on ne reçoit pas systématiquement l'acquittement)
		acknowledgementExceptions.put(182, null); // BasicPingMessage
		acknowledgementExceptions.put(5639, null); // NicknameChoiceRequestMessage
		acknowledgementExceptions.put(6372, null); // CheckIntegrityMessage
		
		// listage de tous les messages nécessitant un hash à l'envoi
		hashedMessages.put("GameActionFightCastRequestMessage", null);
		hashedMessages.put("BasicLatencyStatsMessage", null);
		hashedMessages.put("ChatClientMultiMessage", null);
		hashedMessages.put("ChatClientMultiWithObjectMessage", null);
		hashedMessages.put("ChatClientPrivateMessage", null);
		hashedMessages.put("ChatClientPrivateWithObjectMessage", null);
		hashedMessages.put("GameCautiousMapMovementRequestMessage", null);
		hashedMessages.put("GameMapMovementRequestMessage", null);
		hashedMessages.put("GameRolePlayPlayerFightRequestMessage", null);
		hashedMessages.put("NpcGenericActionRequestMessage", null);
		hashedMessages.put("InteractiveUseRequestMessage", null);
		hashedMessages.put("ExchangePlayerMultiCraftRequestMessage", null);
		hashedMessages.put("ExchangePlayerRequestMessage", null);
		hashedMessages.put("ClientKeyMessage", null);
	}
	
	private int id;
	private String name;
	private int lenofsize;
	private int size;
	private Date sendingTime;
	protected ByteArray content;
	
	private int contentBytesAvailables; // nombre d'octets du contenu acquis
	private boolean isComplete;
	
	public abstract void serialize();
	public abstract void deserialize();
	
	// constructeur pour les messages non gérés (UnhandledMessage)
	public NetworkMessage(String msgName) {
		this.name = msgName;
	}
	
	// constructeur pour les messages gérés
	public NetworkMessage() {
		this.name = getClass().getSimpleName();
	}
	
	public static void loadMessagesListAndClasses() {
		// chargement de la liste des messages (id + nom) écrite en dur
		fillMessagesMap();
		Log.info("Network messages list loaded.");
		
		// récupération de toutes les classes de sérialisation/désérialisation des messages dans le package "messages"
		try {
			Class<?>[] classesArray = Reflection.getClassesInPackage("messages");
			for(Class<?> cl : classesArray)
				msgClasses.put(cl.getSimpleName(), (Class<NetworkMessage>) cl);
			Log.info("Network message classes loaded.");
		} catch(Exception e) {
			e.printStackTrace();
			Main.exit("Error occured during loading deserialization classes.");
		}
	}
	
	// sorte de méthode "factory" pour les messages reçus
	public static NetworkMessage create(int id, int lenofsize, int size, byte[] content, int bytesAvailables) {
		NetworkMessage msg;
		String msgName = (String) messages.get(id);
		if(msgName == null) { // message inconnu
			Log.warn("Unknown message with id = " + id + ".");
			msg = new UnhandledMessage(null);
		}
		else {
			Class<? extends NetworkMessage> cl = msgClasses.get(msgName);
			if(cl == null)
				msg = new UnhandledMessage(msgName);
			else
				try {
					msg = cl.newInstance();
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
		}
		msg.id = id;
		msg.lenofsize = lenofsize;
		msg.size = size;
		if(content != null) {
			if(content.length == size)		
				msg.content = new ByteArray(content); // complet
			else
				msg.content = new ByteArray(content, size); // incomplet
		}
		
		msg.contentBytesAvailables = bytesAvailables;
		msg.isComplete = bytesAvailables == size;
		return msg;
	}
	
	public byte[] pack(int characterId) {
		this.id = get(this.name);
		this.content = new ByteArray();
		serialize(); // unique appel de la fonction "serialize()"
		if(hashedMessages.containsKey(this.name))
			Emulation.hashMessage(this.content, characterId);
		this.size = this.content.getSize();
		this.lenofsize = computeLenOfSize(this.size);
		
		ByteArray msg = new ByteArray(2 + this.lenofsize + this.size);
		msg.writeShort(this.id << 2 | this.lenofsize);
		if(this.lenofsize == 0) return msg.bytes();
		else if(this.lenofsize == 1)
			msg.writeByte(this.size);
		else if(this.lenofsize == 2)
			msg.writeShort(size);
		else {
			msg.writeByte(this.size >> 16);
			msg.writeShort(size & 65535);
		}
		msg.writeBytes(this.content);
		return msg.bytes();
	}
	
	public int appendContent(byte[] buffer) {
		int additionSize;
		if(buffer.length > this.size - this.contentBytesAvailables)
			additionSize = this.size - this.contentBytesAvailables;
		else
			additionSize = buffer.length;
		this.content.writeBytes(buffer, additionSize);
		this.contentBytesAvailables += additionSize;
		this.isComplete = this.contentBytesAvailables == this.size;
		if(this.isComplete) // si le message est complet alors on reset la position du curseur
			this.content.setPos(0);
		return additionSize;
	}
	
	public void setPosToMax() {
		this.content.setPos(this.contentBytesAvailables);
	}
	
	public void setSendingTime(Date date) {
		this.sendingTime = date;
	}
	
	public static String get(int id) {
		return (String) messages.get(id);
	}
	
	public static int get(String name) {
		Object id = messages.get(name);
		if(id == null)
			throw new FatalError("Unknown message name : \"" + name + "\".");
		return (int) id;
	}
	
	public static Class<NetworkMessage> getClassByName(String msgName) {
		return msgClasses.get(msgName);
	}
	
	public static int get(NetworkMessage msg) {
		return get(msg.getClass().getSimpleName());
	}
	
	public static int get(Class<NetworkMessage> c) {
		return get(c.getSimpleName());
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}

	public int getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public int getTotalSize() {
		return this.contentBytesAvailables + 2 + this.lenofsize;
	}
	
	public Date getSendingTime() {
		return this.sendingTime;
	}
	
	public static boolean isAcknowledgable(int msgId) {
		return !acknowledgementExceptions.containsKey(msgId);
	}
	
	public boolean isAcknowledgable() {
		return !acknowledgementExceptions.containsKey(this.id);
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Network message :\n");
		str.append("name = " + this.name + "\n");
		str.append("id = " + this.id + "\n");
		str.append("lenofsize = " + this.lenofsize + "\n");
		str.append("size = " + this.size + "\n");
		return str.toString();
	}
	
	private static short computeLenOfSize(int size) {
	    if(size > 65535)
	        return 3;
	    else if(size > 255)
	        return 2;
	    else if(size > 0)
	        return 1;
	    else
	        return 0;
	}
	
	private static void fillMessagesMap() {
		messages.put(1, "ProtocolRequired");
		
		messages.put(6118, "AbstractGameActionFightTargetedAbilityMessage");
		messages.put(1000, "AbstractGameActionMessage");
		messages.put(1001, "AbstractGameActionWithAckMessage");
		messages.put(6273, "AbstractPartyEventMessage");
		messages.put(6274, "AbstractPartyMessage");
		messages.put(6568, "AbstractTaxCollectorListMessage");
		messages.put(6521, "AccessoryPreviewErrorMessage");
		messages.put(6517, "AccessoryPreviewMessage");
		messages.put(6518, "AccessoryPreviewRequestMessage");
		messages.put(6216, "AccountCapabilitiesMessage");
		messages.put(6315, "AccountHouseMessage");
		messages.put(6607, "AccountLinkRequiredMessage");
		messages.put(6029, "AccountLoggingKickedMessage");
		messages.put(6358, "AchievementDetailedListMessage");
		messages.put(6357, "AchievementDetailedListRequestMessage");
		messages.put(6378, "AchievementDetailsMessage");
		messages.put(6380, "AchievementDetailsRequestMessage");
		messages.put(6381, "AchievementFinishedInformationMessage");
		messages.put(6208, "AchievementFinishedMessage");
		messages.put(6205, "AchievementListMessage");
		messages.put(6375, "AchievementRewardErrorMessage");
		messages.put(6377, "AchievementRewardRequestMessage");
		messages.put(6376, "AchievementRewardSuccessMessage");
		messages.put(6143, "AcquaintanceSearchErrorMessage");
		messages.put(6144, "AcquaintanceSearchMessage");
		messages.put(6142, "AcquaintanceServerListMessage");
		messages.put(76, "AdminCommandMessage");
		messages.put(5662, "AdminQuietCommandMessage");
		messages.put(6669, "AggregateStatMessage");
		messages.put(6662, "AggregateStatWithDataMessage");
		messages.put(6058, "AlignmentRankUpdateMessage");
		messages.put(6426, "AllianceChangeGuildRightsMessage");
		messages.put(6391, "AllianceCreationResultMessage");
		messages.put(6394, "AllianceCreationStartedMessage");
		messages.put(6393, "AllianceCreationValidMessage");
		messages.put(6423, "AllianceFactsErrorMessage");
		messages.put(6414, "AllianceFactsMessage");
		messages.put(6409, "AllianceFactsRequestMessage");
		messages.put(6399, "AllianceGuildLeavingMessage");
		messages.put(6403, "AllianceInsiderInfoMessage");
		messages.put(6417, "AllianceInsiderInfoRequestMessage");
		messages.put(6401, "AllianceInvitationAnswerMessage");
		messages.put(6395, "AllianceInvitationMessage");
		messages.put(6392, "AllianceInvitationStateRecrutedMessage");
		messages.put(6396, "AllianceInvitationStateRecruterMessage");
		messages.put(6397, "AllianceInvitedMessage");
		messages.put(6402, "AllianceJoinedMessage");
		messages.put(6400, "AllianceKickRequestMessage");
		messages.put(6398, "AllianceLeftMessage");
		messages.put(6408, "AllianceListMessage");
		messages.put(6390, "AllianceMembershipMessage");
		messages.put(6447, "AllianceModificationEmblemValidMessage");
		messages.put(6449, "AllianceModificationNameAndTagValidMessage");
		messages.put(6444, "AllianceModificationStartedMessage");
		messages.put(6450, "AllianceModificationValidMessage");
		messages.put(6427, "AlliancePartialListMessage");
		messages.put(6448, "AlliancePrismDialogQuestionMessage");
		messages.put(6445, "AllianceTaxCollectorDialogQuestionExtendedMessage");
		messages.put(6436, "AllianceVersatileInfoListMessage");
		messages.put(6341, "AlmanachCalendarDateMessage");
		messages.put(109, "AlreadyConnectedMessage");
		messages.put(6493, "AreaFightModificatorUpdateMessage");
		messages.put(5956, "AtlasPointInformationsMessage");
		messages.put(111, "AuthenticationTicketAcceptedMessage");
		messages.put(110, "AuthenticationTicketMessage");
		messages.put(112, "AuthenticationTicketRefusedMessage");
		messages.put(6362, "BasicAckMessage");
		messages.put(6475, "BasicCharactersListMessage");
		messages.put(177, "BasicDateMessage");
		messages.put(5663, "BasicLatencyStatsMessage");
		messages.put(5816, "BasicLatencyStatsRequestMessage");
		messages.put(176, "BasicNoOperationMessage");
		messages.put(182, "BasicPingMessage");
		messages.put(183, "BasicPongMessage");
		messages.put(6530, "BasicStatMessage");
		messages.put(6573, "BasicStatWithDataMessage");
		messages.put(175, "BasicTimeMessage");
		messages.put(5664, "BasicWhoAmIRequestMessage");
		messages.put(180, "BasicWhoIsMessage");
		messages.put(179, "BasicWhoIsNoMatchMessage");
		messages.put(181, "BasicWhoIsRequestMessage");
		messages.put(5908, "ChallengeFightJoinRefusedMessage");
		messages.put(6022, "ChallengeInfoMessage");
		messages.put(6019, "ChallengeResultMessage");
		messages.put(6123, "ChallengeTargetUpdateMessage");
		messages.put(5613, "ChallengeTargetsListMessage");
		messages.put(5614, "ChallengeTargetsListRequestMessage");
		messages.put(6638, "ChangeHavenBagRoomRequestMessage");
		messages.put(221, "ChangeMapMessage");
		messages.put(6639, "ChangeThemeRequestMessage");
		messages.put(891, "ChannelEnablingChangeMessage");
		messages.put(890, "ChannelEnablingMessage");
		messages.put(6339, "CharacterCapabilitiesMessage");
		messages.put(160, "CharacterCreationRequestMessage");
		messages.put(161, "CharacterCreationResultMessage");
		messages.put(166, "CharacterDeletionErrorMessage");
		messages.put(165, "CharacterDeletionRequestMessage");
		messages.put(6321, "CharacterExperienceGainMessage");
		messages.put(6084, "CharacterFirstSelectionMessage");
		messages.put(6076, "CharacterLevelUpInformationMessage");
		messages.put(5670, "CharacterLevelUpMessage");
		messages.put(6471, "CharacterLoadingCompleteMessage");
		messages.put(164, "CharacterNameSuggestionFailureMessage");
		messages.put(162, "CharacterNameSuggestionRequestMessage");
		messages.put(5544, "CharacterNameSuggestionSuccessMessage");
		messages.put(167, "CharacterReplayRequestMessage");
		messages.put(6551, "CharacterReplayWithRemodelRequestMessage");
		messages.put(6079, "CharacterReportMessage");
		messages.put(5836, "CharacterSelectedErrorMessage");
		messages.put(6068, "CharacterSelectedForceMessage");
		messages.put(6072, "CharacterSelectedForceReadyMessage");
		messages.put(153, "CharacterSelectedSuccessMessage");
		messages.put(152, "CharacterSelectionMessage");
		messages.put(6549, "CharacterSelectionWithRemodelMessage");
		messages.put(500, "CharacterStatsListMessage");
		messages.put(5545, "CharactersListErrorMessage");
		messages.put(151, "CharactersListMessage");
		messages.put(150, "CharactersListRequestMessage");
		messages.put(6120, "CharactersListWithModificationsMessage");
		messages.put(6550, "CharactersListWithRemodelingMessage");
		messages.put(850, "ChatAbstractClientMessage");
		messages.put(880, "ChatAbstractServerMessage");
		messages.put(6135, "ChatAdminServerMessage");
		messages.put(861, "ChatClientMultiMessage");
		messages.put(862, "ChatClientMultiWithObjectMessage");
		messages.put(851, "ChatClientPrivateMessage");
		messages.put(852, "ChatClientPrivateWithObjectMessage");
		messages.put(870, "ChatErrorMessage");
		messages.put(821, "ChatMessageReportMessage");
		messages.put(882, "ChatServerCopyMessage");
		messages.put(884, "ChatServerCopyWithObjectMessage");
		messages.put(881, "ChatServerMessage");
		messages.put(883, "ChatServerWithObjectMessage");
		messages.put(6596, "ChatSmileyExtraPackListMessage");
		messages.put(801, "ChatSmileyMessage");
		messages.put(800, "ChatSmileyRequestMessage");
		messages.put(6156, "CheckFileMessage");
		messages.put(6154, "CheckFileRequestMessage");
		messages.put(6372, "CheckIntegrityMessage");
		messages.put(6053, "CinematicMessage");
		messages.put(5607, "ClientKeyMessage");
		messages.put(6463, "ClientUIOpenedByObjectMessage");
		messages.put(6459, "ClientUIOpenedMessage");
		messages.put(6594, "ClientYouAreDrunkMessage");
		messages.put(6621, "CloseHavenBagFurnitureSequenceRequestMessage");
		messages.put(6536, "ComicReadingBeginMessage");
		messages.put(5584, "CompassResetMessage");
		messages.put(5591, "CompassUpdateMessage");
		messages.put(5589, "CompassUpdatePartyMemberMessage");
		messages.put(6013, "CompassUpdatePvpSeekMessage");
		messages.put(6127, "ConsoleCommandsListMessage");
		messages.put(75, "ConsoleMessage");
		messages.put(6045, "ContactLookErrorMessage");
		messages.put(5934, "ContactLookMessage");
		messages.put(5935, "ContactLookRequestByIdMessage");
		messages.put(5933, "ContactLookRequestByNameMessage");
		messages.put(5932, "ContactLookRequestMessage");
		messages.put(6314, "CredentialsAcknowledgementMessage");
		messages.put(220, "CurrentMapMessage");
		messages.put(6525, "CurrentServerStatusUpdateMessage");
		messages.put(6680, "DareCancelRequestMessage");
		messages.put(6679, "DareCanceledMessage");
		messages.put(6663, "DareCreatedListMessage");
		messages.put(6668, "DareCreatedMessage");
		messages.put(6665, "DareCreationRequestMessage");
		messages.put(6667, "DareErrorMessage");
		messages.put(6656, "DareInformationsMessage");
		messages.put(6659, "DareInformationsRequestMessage");
		messages.put(6661, "DareListMessage");
		messages.put(6676, "DareRewardConsumeRequestMessage");
		messages.put(6675, "DareRewardConsumeValidationMessage");
		messages.put(6678, "DareRewardWonMessage");
		messages.put(6677, "DareRewardsListMessage");
		messages.put(6666, "DareSubscribeRequestMessage");
		messages.put(6658, "DareSubscribedListMessage");
		messages.put(6660, "DareSubscribedMessage");
		messages.put(6657, "DareVersatileListMessage");
		messages.put(6682, "DareWonListMessage");
		messages.put(6681, "DareWonMessage");
		messages.put(2002, "DebugClearHighlightCellsMessage");
		messages.put(2001, "DebugHighlightCellsMessage");
		messages.put(6028, "DebugInClientMessage");
		messages.put(6569, "DecraftResultMessage");
		messages.put(6563, "DisplayNumericalValuePaddockMessage");
		messages.put(5675, "DocumentReadingBeginMessage");
		messages.put(1511, "DownloadCurrentSpeedMessage");
		messages.put(1513, "DownloadErrorMessage");
		messages.put(1510, "DownloadGetCurrentSpeedRequestMessage");
		messages.put(1503, "DownloadPartMessage");
		messages.put(1512, "DownloadSetSpeedRequestMessage");
		messages.put(6299, "DungeonKeyRingMessage");
		messages.put(6296, "DungeonKeyRingUpdateMessage");
		messages.put(6242, "DungeonPartyFinderAvailableDungeonsMessage");
		messages.put(6240, "DungeonPartyFinderAvailableDungeonsRequestMessage");
		messages.put(6248, "DungeonPartyFinderListenErrorMessage");
		messages.put(6246, "DungeonPartyFinderListenRequestMessage");
		messages.put(6243, "DungeonPartyFinderRegisterErrorMessage");
		messages.put(6249, "DungeonPartyFinderRegisterRequestMessage");
		messages.put(6241, "DungeonPartyFinderRegisterSuccessMessage");
		messages.put(6247, "DungeonPartyFinderRoomContentMessage");
		messages.put(6250, "DungeonPartyFinderRoomContentUpdateMessage");
		messages.put(6619, "EditHavenBagCancelRequestMessage");
		messages.put(6628, "EditHavenBagFinishedMessage");
		messages.put(6626, "EditHavenBagRequestMessage");
		messages.put(6632, "EditHavenBagStartMessage");
		messages.put(5644, "EmoteAddMessage");
		messages.put(5689, "EmoteListMessage");
		messages.put(5690, "EmotePlayAbstractMessage");
		messages.put(5688, "EmotePlayErrorMessage");
		messages.put(5691, "EmotePlayMassiveMessage");
		messages.put(5683, "EmotePlayMessage");
		messages.put(5685, "EmotePlayRequestMessage");
		messages.put(5687, "EmoteRemoveMessage");
		messages.put(892, "EnabledChannelsMessage");
		messages.put(6636, "EnterHavenBagRequestMessage");
		messages.put(6110, "EntityTalkMessage");
		messages.put(6197, "ErrorMapNotFoundMessage");
		messages.put(5508, "ExchangeAcceptMessage");
		messages.put(5804, "ExchangeBidHouseBuyMessage");
		messages.put(6272, "ExchangeBidHouseBuyResultMessage");
		messages.put(5947, "ExchangeBidHouseGenericItemAddedMessage");
		messages.put(5948, "ExchangeBidHouseGenericItemRemovedMessage");
		messages.put(5949, "ExchangeBidHouseInListAddedMessage");
		messages.put(5950, "ExchangeBidHouseInListRemovedMessage");
		messages.put(6337, "ExchangeBidHouseInListUpdatedMessage");
		messages.put(5945, "ExchangeBidHouseItemAddOkMessage");
		messages.put(5946, "ExchangeBidHouseItemRemoveOkMessage");
		messages.put(5807, "ExchangeBidHouseListMessage");
		messages.put(5805, "ExchangeBidHousePriceMessage");
		messages.put(5806, "ExchangeBidHouseSearchMessage");
		messages.put(5803, "ExchangeBidHouseTypeMessage");
		messages.put(6612, "ExchangeBidHouseUnsoldItemsMessage");
		messages.put(6464, "ExchangeBidPriceForSellerMessage");
		messages.put(5755, "ExchangeBidPriceMessage");
		messages.put(5802, "ExchangeBidSearchOkMessage");
		messages.put(5774, "ExchangeBuyMessage");
		messages.put(5759, "ExchangeBuyOkMessage");
		messages.put(6595, "ExchangeCraftCountModifiedMessage");
		messages.put(6597, "ExchangeCraftCountRequestMessage");
		messages.put(5794, "ExchangeCraftInformationObjectMessage");
		messages.put(6579, "ExchangeCraftPaymentModificationRequestMessage");
		messages.put(6578, "ExchangeCraftPaymentModifiedMessage");
		messages.put(6188, "ExchangeCraftResultMagicWithObjectDescMessage");
		messages.put(5790, "ExchangeCraftResultMessage");
		messages.put(5999, "ExchangeCraftResultWithObjectDescMessage");
		messages.put(6000, "ExchangeCraftResultWithObjectIdMessage");
		messages.put(6598, "ExchangeCrafterJobLevelupMessage");
		messages.put(5513, "ExchangeErrorMessage");
		messages.put(5762, "ExchangeGuildTaxCollectorGetMessage");
		messages.put(6562, "ExchangeHandleMountsStableMessage");
		messages.put(5509, "ExchangeIsReadyMessage");
		messages.put(5810, "ExchangeItemAutoCraftStopedMessage");
		messages.put(5521, "ExchangeKamaModifiedMessage");
		messages.put(5628, "ExchangeLeaveMessage");
		messages.put(6055, "ExchangeMountFreeFromPaddockMessage");
		messages.put(5981, "ExchangeMountStableErrorMessage");
		messages.put(6056, "ExchangeMountSterilizeFromPaddockMessage");
		messages.put(6561, "ExchangeMountsPaddockAddMessage");
		messages.put(6559, "ExchangeMountsPaddockRemoveMessage");
		messages.put(6555, "ExchangeMountsStableAddMessage");
		messages.put(6557, "ExchangeMountsStableBornAddMessage");
		messages.put(6556, "ExchangeMountsStableRemoveMessage");
		messages.put(6554, "ExchangeMountsTakenFromPaddockMessage");
		messages.put(6020, "ExchangeMultiCraftCrafterCanUseHisRessourcesMessage");
		messages.put(6021, "ExchangeMultiCraftSetCrafterCanUseHisRessourcesMessage");
		messages.put(5516, "ExchangeObjectAddedMessage");
		messages.put(5515, "ExchangeObjectMessage");
		messages.put(6008, "ExchangeObjectModifiedInBagMessage");
		messages.put(5519, "ExchangeObjectModifiedMessage");
		messages.put(6238, "ExchangeObjectModifyPricedMessage");
		messages.put(5520, "ExchangeObjectMoveKamaMessage");
		messages.put(5518, "ExchangeObjectMoveMessage");
		messages.put(5514, "ExchangeObjectMovePricedMessage");
		messages.put(6009, "ExchangeObjectPutInBagMessage");
		messages.put(6010, "ExchangeObjectRemovedFromBagMessage");
		messages.put(5517, "ExchangeObjectRemovedMessage");
		messages.put(6184, "ExchangeObjectTransfertAllFromInvMessage");
		messages.put(6032, "ExchangeObjectTransfertAllToInvMessage");
		messages.put(6325, "ExchangeObjectTransfertExistingFromInvMessage");
		messages.put(6326, "ExchangeObjectTransfertExistingToInvMessage");
		messages.put(6183, "ExchangeObjectTransfertListFromInvMessage");
		messages.put(6039, "ExchangeObjectTransfertListToInvMessage");
		messages.put(6470, "ExchangeObjectTransfertListWithQuantityToInvMessage");
		messages.put(6004, "ExchangeObjectUseInWorkshopMessage");
		messages.put(6535, "ExchangeObjectsAddedMessage");
		messages.put(6533, "ExchangeObjectsModifiedMessage");
		messages.put(6532, "ExchangeObjectsRemovedMessage");
		messages.put(6613, "ExchangeOfflineSoldItemsMessage");
		messages.put(5768, "ExchangeOkMultiCraftMessage");
		messages.put(5772, "ExchangeOnHumanVendorRequestMessage");
		messages.put(5784, "ExchangePlayerMultiCraftRequestMessage");
		messages.put(5773, "ExchangePlayerRequestMessage");
		messages.put(6670, "ExchangePodsModifiedMessage");
		messages.put(5511, "ExchangeReadyMessage");
		messages.put(6001, "ExchangeReplayStopMessage");
		messages.put(5787, "ExchangeReplyTaxVendorMessage");
		messages.put(5505, "ExchangeRequestMessage");
		messages.put(5986, "ExchangeRequestOnMountStockMessage");
		messages.put(5753, "ExchangeRequestOnShopStockMessage");
		messages.put(5779, "ExchangeRequestOnTaxCollectorMessage");
		messages.put(5522, "ExchangeRequestedMessage");
		messages.put(5523, "ExchangeRequestedTradeMessage");
		messages.put(5778, "ExchangeSellMessage");
		messages.put(5792, "ExchangeSellOkMessage");
		messages.put(6389, "ExchangeSetCraftRecipeMessage");
		messages.put(5907, "ExchangeShopStockMovementRemovedMessage");
		messages.put(5909, "ExchangeShopStockMovementUpdatedMessage");
		messages.put(6037, "ExchangeShopStockMultiMovementRemovedMessage");
		messages.put(6038, "ExchangeShopStockMultiMovementUpdatedMessage");
		messages.put(5910, "ExchangeShopStockStartedMessage");
		messages.put(5783, "ExchangeShowVendorTaxMessage");
		messages.put(5775, "ExchangeStartAsVendorMessage");
		messages.put(5813, "ExchangeStartOkCraftMessage");
		messages.put(5941, "ExchangeStartOkCraftWithInformationMessage");
		messages.put(5767, "ExchangeStartOkHumanVendorMessage");
		messages.put(5819, "ExchangeStartOkJobIndexMessage");
		messages.put(5979, "ExchangeStartOkMountMessage");
		messages.put(5991, "ExchangeStartOkMountWithOutPaddockMessage");
		messages.put(5818, "ExchangeStartOkMulticraftCrafterMessage");
		messages.put(5817, "ExchangeStartOkMulticraftCustomerMessage");
		messages.put(5761, "ExchangeStartOkNpcShopMessage");
		messages.put(5785, "ExchangeStartOkNpcTradeMessage");
		messages.put(6600, "ExchangeStartOkRecycleTradeMessage");
		messages.put(6567, "ExchangeStartOkRunesTradeMessage");
		messages.put(5904, "ExchangeStartedBidBuyerMessage");
		messages.put(5905, "ExchangeStartedBidSellerMessage");
		messages.put(5512, "ExchangeStartedMessage");
		messages.put(5984, "ExchangeStartedMountStockMessage");
		messages.put(6664, "ExchangeStartedTaxCollectorShopMessage");
		messages.put(6129, "ExchangeStartedWithPodsMessage");
		messages.put(6236, "ExchangeStartedWithStorageMessage");
		messages.put(6589, "ExchangeStoppedMessage");
		messages.put(5765, "ExchangeTypesExchangerDescriptionForUserMessage");
		messages.put(5752, "ExchangeTypesItemsExchangerDescriptionForUserMessage");
		messages.put(5786, "ExchangeWaitingResultMessage");
		messages.put(5793, "ExchangeWeightMessage");
		messages.put(6631, "ExitHavenBagRequestMessage");
		messages.put(6322, "FighterStatsListMessage");
		messages.put(5600, "FriendAddFailureMessage");
		messages.put(4004, "FriendAddRequestMessage");
		messages.put(5599, "FriendAddedMessage");
		messages.put(5603, "FriendDeleteRequestMessage");
		messages.put(5601, "FriendDeleteResultMessage");
		messages.put(6382, "FriendGuildSetWarnOnAchievementCompleteMessage");
		messages.put(6383, "FriendGuildWarnOnAchievementCompleteStateMessage");
		messages.put(5605, "FriendJoinRequestMessage");
		messages.put(5602, "FriendSetWarnOnConnectionMessage");
		messages.put(6077, "FriendSetWarnOnLevelGainMessage");
		messages.put(5606, "FriendSpouseFollowWithCompassRequestMessage");
		messages.put(5604, "FriendSpouseJoinRequestMessage");
		messages.put(5924, "FriendUpdateMessage");
		messages.put(5630, "FriendWarnOnConnectionStateMessage");
		messages.put(6078, "FriendWarnOnLevelGainStateMessage");
		messages.put(4001, "FriendsGetListMessage");
		messages.put(4002, "FriendsListMessage");
		messages.put(957, "GameActionAcknowledgementMessage");
		messages.put(6545, "GameActionFightActivateGlyphTrapMessage");
		messages.put(5830, "GameActionFightCarryCharacterMessage");
		messages.put(6330, "GameActionFightCastOnTargetRequestMessage");
		messages.put(1005, "GameActionFightCastRequestMessage");
		messages.put(5532, "GameActionFightChangeLookMessage");
		messages.put(6116, "GameActionFightCloseCombatMessage");
		messages.put(1099, "GameActionFightDeathMessage");
		messages.put(6113, "GameActionFightDispellEffectMessage");
		messages.put(5533, "GameActionFightDispellMessage");
		messages.put(6176, "GameActionFightDispellSpellMessage");
		messages.put(6070, "GameActionFightDispellableEffectMessage");
		messages.put(5828, "GameActionFightDodgePointLossMessage");
		messages.put(5826, "GameActionFightDropCharacterMessage");
		messages.put(5527, "GameActionFightExchangePositionsMessage");
		messages.put(5821, "GameActionFightInvisibilityMessage");
		messages.put(6320, "GameActionFightInvisibleDetectedMessage");
		messages.put(5571, "GameActionFightKillMessage");
		messages.put(6310, "GameActionFightLifeAndShieldPointsLostMessage");
		messages.put(6311, "GameActionFightLifePointsGainMessage");
		messages.put(6312, "GameActionFightLifePointsLostMessage");
		messages.put(5540, "GameActionFightMarkCellsMessage");
		messages.put(6304, "GameActionFightModifyEffectsDurationMessage");
		messages.put(6132, "GameActionFightNoSpellCastMessage");
		messages.put(1030, "GameActionFightPointsVariationMessage");
		messages.put(5526, "GameActionFightReduceDamagesMessage");
		messages.put(5530, "GameActionFightReflectDamagesMessage");
		messages.put(5531, "GameActionFightReflectSpellMessage");
		messages.put(5525, "GameActionFightSlideMessage");
		messages.put(1010, "GameActionFightSpellCastMessage");
		messages.put(6219, "GameActionFightSpellCooldownVariationMessage");
		messages.put(6221, "GameActionFightSpellImmunityMessage");
		messages.put(5535, "GameActionFightStealKamaMessage");
		messages.put(5825, "GameActionFightSummonMessage");
		messages.put(1004, "GameActionFightTackledMessage");
		messages.put(5528, "GameActionFightTeleportOnSameMapMessage");
		messages.put(5829, "GameActionFightThrowCharacterMessage");
		messages.put(6147, "GameActionFightTriggerEffectMessage");
		messages.put(5741, "GameActionFightTriggerGlyphTrapMessage");
		messages.put(5570, "GameActionFightUnmarkCellsMessage");
		messages.put(6217, "GameActionFightVanishMessage");
		messages.put(1002, "GameActionNoopMessage");
		messages.put(6497, "GameCautiousMapMovementMessage");
		messages.put(6496, "GameCautiousMapMovementRequestMessage");
		messages.put(6024, "GameContextCreateErrorMessage");
		messages.put(200, "GameContextCreateMessage");
		messages.put(250, "GameContextCreateRequestMessage");
		messages.put(201, "GameContextDestroyMessage");
		messages.put(6081, "GameContextKickMessage");
		messages.put(253, "GameContextMoveElementMessage");
		messages.put(254, "GameContextMoveMultipleElementsMessage");
		messages.put(255, "GameContextQuitMessage");
		messages.put(6071, "GameContextReadyMessage");
		messages.put(5637, "GameContextRefreshEntityLookMessage");
		messages.put(251, "GameContextRemoveElementMessage");
		messages.put(6412, "GameContextRemoveElementWithEventMessage");
		messages.put(252, "GameContextRemoveMultipleElementsMessage");
		messages.put(6416, "GameContextRemoveMultipleElementsWithEventsMessage");
		messages.put(5990, "GameDataPaddockObjectAddMessage");
		messages.put(5992, "GameDataPaddockObjectListAddMessage");
		messages.put(5993, "GameDataPaddockObjectRemoveMessage");
		messages.put(6026, "GameDataPlayFarmObjectAnimationMessage");
		messages.put(5696, "GameEntitiesDispositionMessage");
		messages.put(5695, "GameEntityDispositionErrorMessage");
		messages.put(5693, "GameEntityDispositionMessage");
		messages.put(720, "GameFightEndMessage");
		messages.put(740, "GameFightHumanReadyStateMessage");
		messages.put(702, "GameFightJoinMessage");
		messages.put(701, "GameFightJoinRequestMessage");
		messages.put(721, "GameFightLeaveMessage");
		messages.put(6239, "GameFightNewRoundMessage");
		messages.put(6490, "GameFightNewWaveMessage");
		messages.put(5927, "GameFightOptionStateUpdateMessage");
		messages.put(707, "GameFightOptionToggleMessage");
		messages.put(704, "GameFightPlacementPositionRequestMessage");
		messages.put(703, "GameFightPlacementPossiblePositionsMessage");
		messages.put(6547, "GameFightPlacementSwapPositionsAcceptMessage");
		messages.put(6543, "GameFightPlacementSwapPositionsCancelMessage");
		messages.put(6546, "GameFightPlacementSwapPositionsCancelledMessage");
		messages.put(6548, "GameFightPlacementSwapPositionsErrorMessage");
		messages.put(6544, "GameFightPlacementSwapPositionsMessage");
		messages.put(6542, "GameFightPlacementSwapPositionsOfferMessage");
		messages.put(6541, "GameFightPlacementSwapPositionsRequestMessage");
		messages.put(708, "GameFightReadyMessage");
		messages.put(6309, "GameFightRefreshFighterMessage");
		messages.put(711, "GameFightRemoveTeamMemberMessage");
		messages.put(6067, "GameFightResumeMessage");
		messages.put(6215, "GameFightResumeWithSlavesMessage");
		messages.put(5864, "GameFightShowFighterMessage");
		messages.put(6218, "GameFightShowFighterRandomStaticPoseMessage");
		messages.put(6069, "GameFightSpectateMessage");
		messages.put(6474, "GameFightSpectatePlayerRequestMessage");
		messages.put(6504, "GameFightSpectatorJoinMessage");
		messages.put(712, "GameFightStartMessage");
		messages.put(700, "GameFightStartingMessage");
		messages.put(5921, "GameFightSynchronizeMessage");
		messages.put(719, "GameFightTurnEndMessage");
		messages.put(718, "GameFightTurnFinishMessage");
		messages.put(713, "GameFightTurnListMessage");
		messages.put(716, "GameFightTurnReadyMessage");
		messages.put(715, "GameFightTurnReadyRequestMessage");
		messages.put(6307, "GameFightTurnResumeMessage");
		messages.put(714, "GameFightTurnStartMessage");
		messages.put(6465, "GameFightTurnStartPlayingMessage");
		messages.put(5572, "GameFightUpdateTeamMessage");
		messages.put(946, "GameMapChangeOrientationMessage");
		messages.put(945, "GameMapChangeOrientationRequestMessage");
		messages.put(6155, "GameMapChangeOrientationsMessage");
		messages.put(953, "GameMapMovementCancelMessage");
		messages.put(952, "GameMapMovementConfirmMessage");
		messages.put(951, "GameMapMovementMessage");
		messages.put(950, "GameMapMovementRequestMessage");
		messages.put(954, "GameMapNoMovementMessage");
		messages.put(6618, "GameRefreshMonsterBoostsMessage");
		messages.put(6073, "GameRolePlayAggressionMessage");
		messages.put(6279, "GameRolePlayArenaFightAnswerMessage");
		messages.put(6276, "GameRolePlayArenaFightPropositionMessage");
		messages.put(6281, "GameRolePlayArenaFighterStatusMessage");
		messages.put(6280, "GameRolePlayArenaRegisterMessage");
		messages.put(6284, "GameRolePlayArenaRegistrationStatusMessage");
		messages.put(6575, "GameRolePlayArenaSwitchToFightServerMessage");
		messages.put(6574, "GameRolePlayArenaSwitchToGameServerMessage");
		messages.put(6282, "GameRolePlayArenaUnregisterMessage");
		messages.put(6301, "GameRolePlayArenaUpdatePlayerInfosMessage");
		messages.put(6640, "GameRolePlayArenaUpdatePlayerInfosWithTeamMessage");
		messages.put(6191, "GameRolePlayAttackMonsterRequestMessage");
		messages.put(6150, "GameRolePlayDelayedActionFinishedMessage");
		messages.put(6153, "GameRolePlayDelayedActionMessage");
		messages.put(6425, "GameRolePlayDelayedObjectUseMessage");
		messages.put(5822, "GameRolePlayFightRequestCanceledMessage");
		messages.put(745, "GameRolePlayFreeSoulRequestMessage");
		messages.put(746, "GameRolePlayGameOverMessage");
		messages.put(5732, "GameRolePlayPlayerFightFriendlyAnswerMessage");
		messages.put(5733, "GameRolePlayPlayerFightFriendlyAnsweredMessage");
		messages.put(5937, "GameRolePlayPlayerFightFriendlyRequestedMessage");
		messages.put(5731, "GameRolePlayPlayerFightRequestMessage");
		messages.put(5996, "GameRolePlayPlayerLifeStatusMessage");
		messages.put(300, "GameRolePlayRemoveChallengeMessage");
		messages.put(5632, "GameRolePlayShowActorMessage");
		messages.put(6407, "GameRolePlayShowActorWithEventMessage");
		messages.put(301, "GameRolePlayShowChallengeMessage");
		messages.put(6114, "GameRolePlaySpellAnimMessage");
		messages.put(5954, "GameRolePlayTaxCollectorFightRequestMessage");
		messages.put(1506, "GetPartInfoMessage");
		messages.put(1501, "GetPartsListMessage");
		messages.put(6030, "GoldAddedMessage");
		messages.put(6506, "GuestLimitationMessage");
		messages.put(6505, "GuestModeMessage");
		messages.put(6092, "GuidedModeQuitRequestMessage");
		messages.put(6088, "GuidedModeReturnRequestMessage");
		messages.put(5549, "GuildChangeMemberParametersMessage");
		messages.put(5706, "GuildCharacsUpgradeRequestMessage");
		messages.put(5554, "GuildCreationResultMessage");
		messages.put(5920, "GuildCreationStartedMessage");
		messages.put(5546, "GuildCreationValidMessage");
		messages.put(6424, "GuildFactsErrorMessage");
		messages.put(6415, "GuildFactsMessage");
		messages.put(6404, "GuildFactsRequestMessage");
		messages.put(5717, "GuildFightJoinRequestMessage");
		messages.put(5715, "GuildFightLeaveRequestMessage");
		messages.put(5928, "GuildFightPlayersEnemiesListMessage");
		messages.put(5929, "GuildFightPlayersEnemyRemoveMessage");
		messages.put(5720, "GuildFightPlayersHelpersJoinMessage");
		messages.put(5719, "GuildFightPlayersHelpersLeaveMessage");
		messages.put(6235, "GuildFightTakePlaceRequestMessage");
		messages.put(5550, "GuildGetInformationsMessage");
		messages.put(6180, "GuildHouseRemoveMessage");
		messages.put(5712, "GuildHouseTeleportRequestMessage");
		messages.put(6181, "GuildHouseUpdateInformationMessage");
		messages.put(5919, "GuildHousesInformationMessage");
		messages.put(6422, "GuildInAllianceFactsMessage");
		messages.put(5557, "GuildInformationsGeneralMessage");
		messages.put(5597, "GuildInformationsMemberUpdateMessage");
		messages.put(5558, "GuildInformationsMembersMessage");
		messages.put(5959, "GuildInformationsPaddocksMessage");
		messages.put(5636, "GuildInfosUpgradeMessage");
		messages.put(5556, "GuildInvitationAnswerMessage");
		messages.put(6115, "GuildInvitationByNameMessage");
		messages.put(5551, "GuildInvitationMessage");
		messages.put(5548, "GuildInvitationStateRecrutedMessage");
		messages.put(5563, "GuildInvitationStateRecruterMessage");
		messages.put(5552, "GuildInvitedMessage");
		messages.put(5564, "GuildJoinedMessage");
		messages.put(5887, "GuildKickRequestMessage");
		messages.put(5562, "GuildLeftMessage");
		messages.put(6062, "GuildLevelUpMessage");
		messages.put(6413, "GuildListMessage");
		messages.put(5923, "GuildMemberLeavingMessage");
		messages.put(6061, "GuildMemberOnlineStatusMessage");
		messages.put(6159, "GuildMemberSetWarnOnConnectionMessage");
		messages.put(6160, "GuildMemberWarnOnConnectionStateMessage");
		messages.put(5835, "GuildMembershipMessage");
		messages.put(6328, "GuildModificationEmblemValidMessage");
		messages.put(6327, "GuildModificationNameValidMessage");
		messages.put(6324, "GuildModificationStartedMessage");
		messages.put(6323, "GuildModificationValidMessage");
		messages.put(6590, "GuildMotdMessage");
		messages.put(6591, "GuildMotdSetErrorMessage");
		messages.put(6588, "GuildMotdSetRequestMessage");
		messages.put(5952, "GuildPaddockBoughtMessage");
		messages.put(5955, "GuildPaddockRemovedMessage");
		messages.put(5957, "GuildPaddockTeleportRequestMessage");
		messages.put(5699, "GuildSpellUpgradeRequestMessage");
		messages.put(6435, "GuildVersatileInfoListMessage");
		messages.put(6649, "HaapiApiKeyMessage");
		messages.put(6648, "HaapiApiKeyRequestMessage");
		messages.put(6644, "HavenBagDailyLoteryMessage");
		messages.put(6634, "HavenBagFurnituresMessage");
		messages.put(6637, "HavenBagFurnituresRequestMessage");
		messages.put(6620, "HavenBagPackListMessage");
		messages.put(3, "HelloConnectMessage");
		messages.put(101, "HelloGameMessage");
		messages.put(5738, "HouseBuyRequestMessage");
		messages.put(5735, "HouseBuyResultMessage");
		messages.put(5701, "HouseGuildNoneMessage");
		messages.put(5703, "HouseGuildRightsMessage");
		messages.put(5700, "HouseGuildRightsViewMessage");
		messages.put(5704, "HouseGuildShareRequestMessage");
		messages.put(5661, "HouseKickIndoorMerchantRequestMessage");
		messages.put(5698, "HouseKickRequestMessage");
		messages.put(5885, "HouseLockFromInsideRequestMessage");
		messages.put(5734, "HousePropertiesMessage");
		messages.put(5884, "HouseSellFromInsideRequestMessage");
		messages.put(5697, "HouseSellRequestMessage");
		messages.put(5737, "HouseSoldMessage");
		messages.put(6137, "HouseToSellFilterMessage");
		messages.put(6140, "HouseToSellListMessage");
		messages.put(6139, "HouseToSellListRequestMessage");
		messages.put(6119, "IdentificationAccountForceMessage");
		messages.put(6174, "IdentificationFailedBannedMessage");
		messages.put(21, "IdentificationFailedForBadVersionMessage");
		messages.put(20, "IdentificationFailedMessage");
		messages.put(4, "IdentificationMessage");
		messages.put(22, "IdentificationSuccessMessage");
		messages.put(6209, "IdentificationSuccessWithLoginTokenMessage");
		messages.put(6586, "IdolFightPreparationUpdateMessage");
		messages.put(6585, "IdolListMessage");
		messages.put(6580, "IdolPartyLostMessage");
		messages.put(6583, "IdolPartyRefreshMessage");
		messages.put(6582, "IdolPartyRegisterRequestMessage");
		messages.put(6584, "IdolSelectErrorMessage");
		messages.put(6587, "IdolSelectRequestMessage");
		messages.put(6581, "IdolSelectedMessage");
		messages.put(6602, "IdolsPresetDeleteMessage");
		messages.put(6605, "IdolsPresetDeleteResultMessage");
		messages.put(6603, "IdolsPresetSaveMessage");
		messages.put(6604, "IdolsPresetSaveResultMessage");
		messages.put(6606, "IdolsPresetUpdateMessage");
		messages.put(6615, "IdolsPresetUseMessage");
		messages.put(6614, "IdolsPresetUseResultMessage");
		messages.put(5679, "IgnoredAddFailureMessage");
		messages.put(5673, "IgnoredAddRequestMessage");
		messages.put(5678, "IgnoredAddedMessage");
		messages.put(5680, "IgnoredDeleteRequestMessage");
		messages.put(5677, "IgnoredDeleteResultMessage");
		messages.put(5676, "IgnoredGetListMessage");
		messages.put(5674, "IgnoredListMessage");
		messages.put(5708, "InteractiveElementUpdatedMessage");
		messages.put(5002, "InteractiveMapUpdateMessage");
		messages.put(6112, "InteractiveUseEndedMessage");
		messages.put(6384, "InteractiveUseErrorMessage");
		messages.put(5001, "InteractiveUseRequestMessage");
		messages.put(5745, "InteractiveUsedMessage");
		messages.put(6162, "InventoryContentAndPresetMessage");
		messages.put(3016, "InventoryContentMessage");
		messages.put(6169, "InventoryPresetDeleteMessage");
		messages.put(6173, "InventoryPresetDeleteResultMessage");
		messages.put(6211, "InventoryPresetItemUpdateErrorMessage");
		messages.put(6168, "InventoryPresetItemUpdateMessage");
		messages.put(6210, "InventoryPresetItemUpdateRequestMessage");
		messages.put(6329, "InventoryPresetSaveCustomMessage");
		messages.put(6165, "InventoryPresetSaveMessage");
		messages.put(6170, "InventoryPresetSaveResultMessage");
		messages.put(6171, "InventoryPresetUpdateMessage");
		messages.put(6167, "InventoryPresetUseMessage");
		messages.put(6163, "InventoryPresetUseResultMessage");
		messages.put(3009, "InventoryWeightMessage");
		messages.put(6645, "InviteInHavenBagClosedMessage");
		messages.put(6642, "InviteInHavenBagMessage");
		messages.put(6643, "InviteInHavenBagOfferMessage");
		messages.put(5769, "ItemNoMoreAvailableMessage");
		messages.put(5748, "JobAllowMultiCraftRequestMessage");
		messages.put(6592, "JobBookSubscribeRequestMessage");
		messages.put(6593, "JobBookSubscriptionMessage");
		messages.put(5651, "JobCrafterDirectoryAddMessage");
		messages.put(5649, "JobCrafterDirectoryDefineSettingsMessage");
		messages.put(6044, "JobCrafterDirectoryEntryMessage");
		messages.put(6043, "JobCrafterDirectoryEntryRequestMessage");
		messages.put(6046, "JobCrafterDirectoryListMessage");
		messages.put(6047, "JobCrafterDirectoryListRequestMessage");
		messages.put(5653, "JobCrafterDirectoryRemoveMessage");
		messages.put(5652, "JobCrafterDirectorySettingsMessage");
		messages.put(5655, "JobDescriptionMessage");
		messages.put(5809, "JobExperienceMultiUpdateMessage");
		messages.put(6599, "JobExperienceOtherPlayerUpdateMessage");
		messages.put(5654, "JobExperienceUpdateMessage");
		messages.put(5656, "JobLevelUpMessage");
		messages.put(5747, "JobMultiCraftAvailableSkillsMessage");
		messages.put(5537, "KamasUpdateMessage");
		messages.put(6652, "KickHavenBagRequestMessage");
		messages.put(6439, "KohUpdateMessage");
		messages.put(6345, "KrosmasterAuthTokenErrorMessage");
		messages.put(6351, "KrosmasterAuthTokenMessage");
		messages.put(6346, "KrosmasterAuthTokenRequestMessage");
		messages.put(6343, "KrosmasterInventoryErrorMessage");
		messages.put(6350, "KrosmasterInventoryMessage");
		messages.put(6344, "KrosmasterInventoryRequestMessage");
		messages.put(6347, "KrosmasterPlayingStatusMessage");
		messages.put(6348, "KrosmasterTransferMessage");
		messages.put(6349, "KrosmasterTransferRequestMessage");
		messages.put(5502, "LeaveDialogMessage");
		messages.put(5501, "LeaveDialogRequestMessage");
		messages.put(5684, "LifePointsRegenBeginMessage");
		messages.put(5686, "LifePointsRegenEndMessage");
		messages.put(5725, "LivingObjectChangeSkinRequestMessage");
		messages.put(5723, "LivingObjectDissociateMessage");
		messages.put(6065, "LivingObjectMessageMessage");
		messages.put(6066, "LivingObjectMessageRequestMessage");
		messages.put(6185, "LocalizedChatSmileyMessage");
		messages.put(5666, "LockableChangeCodeMessage");
		messages.put(5672, "LockableCodeResultMessage");
		messages.put(5740, "LockableShowCodeDialogMessage");
		messages.put(5671, "LockableStateUpdateAbstractMessage");
		messages.put(5668, "LockableStateUpdateHouseDoorMessage");
		messages.put(5669, "LockableStateUpdateStorageMessage");
		messages.put(5667, "LockableUseCodeMessage");
		messages.put(10, "LoginQueueStatusMessage");
		messages.put(6275, "MailStatusMessage");
		messages.put(6622, "MapComplementaryInformationsDataInHavenBagMessage");
		messages.put(6130, "MapComplementaryInformationsDataInHouseMessage");
		messages.put(226, "MapComplementaryInformationsDataMessage");
		messages.put(6268, "MapComplementaryInformationsWithCoordsMessage");
		messages.put(210, "MapFightCountMessage");
		messages.put(225, "MapInformationsRequestMessage");
		messages.put(5642, "MapNpcsQuestStatusUpdateMessage");
		messages.put(6051, "MapObstacleUpdateMessage");
		messages.put(6500, "MapRunningFightDetailsExtendedMessage");
		messages.put(5751, "MapRunningFightDetailsMessage");
		messages.put(5750, "MapRunningFightDetailsRequestMessage");
		messages.put(5743, "MapRunningFightListMessage");
		messages.put(5742, "MapRunningFightListRequestMessage");
		messages.put(6462, "MimicryObjectAssociatedMessage");
		messages.put(6457, "MimicryObjectEraseRequestMessage");
		messages.put(6461, "MimicryObjectErrorMessage");
		messages.put(6460, "MimicryObjectFeedAndAssociateRequestMessage");
		messages.put(6458, "MimicryObjectPreviewMessage");
		messages.put(6192, "MoodSmileyRequestMessage");
		messages.put(6196, "MoodSmileyResultMessage");
		messages.put(6388, "MoodSmileyUpdateMessage");
		messages.put(6172, "MountDataErrorMessage");
		messages.put(5973, "MountDataMessage");
		messages.put(5978, "MountEmoteIconUsedOkMessage");
		messages.put(5963, "MountEquipedErrorMessage");
		messages.put(6189, "MountFeedRequestMessage");
		messages.put(5975, "MountInformationInPaddockRequestMessage");
		messages.put(5972, "MountInformationRequestMessage");
		messages.put(5980, "MountReleaseRequestMessage");
		messages.put(6308, "MountReleasedMessage");
		messages.put(5987, "MountRenameRequestMessage");
		messages.put(5983, "MountRenamedMessage");
		messages.put(5967, "MountRidingMessage");
		messages.put(5968, "MountSetMessage");
		messages.put(5989, "MountSetXpRatioRequestMessage");
		messages.put(5962, "MountSterilizeRequestMessage");
		messages.put(5977, "MountSterilizedMessage");
		messages.put(5976, "MountToggleRidingRequestMessage");
		messages.put(5982, "MountUnSetMessage");
		messages.put(5970, "MountXpRatioMessage");
		messages.put(2, "NetworkDataContainerMessage");
		messages.put(6292, "NewMailMessage");
		messages.put(5641, "NicknameAcceptedMessage");
		messages.put(5639, "NicknameChoiceRequestMessage");
		messages.put(5638, "NicknameRefusedMessage");
		messages.put(5640, "NicknameRegistrationMessage");
		messages.put(6103, "NotificationByServerMessage");
		messages.put(6087, "NotificationListMessage");
		messages.put(6089, "NotificationResetMessage");
		messages.put(6090, "NotificationUpdateFlagMessage");
		messages.put(5618, "NpcDialogCreationMessage");
		messages.put(5617, "NpcDialogQuestionMessage");
		messages.put(5616, "NpcDialogReplyMessage");
		messages.put(5900, "NpcGenericActionFailureMessage");
		messages.put(5898, "NpcGenericActionRequestMessage");
		messages.put(6297, "NumericWhoIsMessage");
		messages.put(6298, "NumericWhoIsRequestMessage");
		messages.put(3025, "ObjectAddedMessage");
		messages.put(6336, "ObjectAveragePricesErrorMessage");
		messages.put(6334, "ObjectAveragePricesGetMessage");
		messages.put(6335, "ObjectAveragePricesMessage");
		messages.put(3022, "ObjectDeleteMessage");
		messages.put(3024, "ObjectDeletedMessage");
		messages.put(3005, "ObjectDropMessage");
		messages.put(3004, "ObjectErrorMessage");
		messages.put(6290, "ObjectFeedMessage");
		messages.put(6017, "ObjectFoundWhileRecoltingMessage");
		messages.put(3017, "ObjectGroundAddedMessage");
		messages.put(5925, "ObjectGroundListAddedMessage");
		messages.put(3014, "ObjectGroundRemovedMessage");
		messages.put(5944, "ObjectGroundRemovedMultipleMessage");
		messages.put(6014, "ObjectJobAddedMessage");
		messages.put(3029, "ObjectModifiedMessage");
		messages.put(3010, "ObjectMovementMessage");
		messages.put(3023, "ObjectQuantityMessage");
		messages.put(3021, "ObjectSetPositionMessage");
		messages.put(3019, "ObjectUseMessage");
		messages.put(6234, "ObjectUseMultipleMessage");
		messages.put(3013, "ObjectUseOnCellMessage");
		messages.put(3003, "ObjectUseOnCharacterMessage");
		messages.put(6033, "ObjectsAddedMessage");
		messages.put(6034, "ObjectsDeletedMessage");
		messages.put(6206, "ObjectsQuantityMessage");
		messages.put(6519, "ObtainedItemMessage");
		messages.put(6520, "ObtainedItemWithBonusMessage");
		messages.put(5726, "OnConnectionEventMessage");
		messages.put(6635, "OpenHavenBagFurnitureSequenceRequestMessage");
		messages.put(6368, "OrnamentGainedMessage");
		messages.put(6370, "OrnamentSelectErrorMessage");
		messages.put(6374, "OrnamentSelectRequestMessage");
		messages.put(6369, "OrnamentSelectedMessage");
		messages.put(5951, "PaddockBuyRequestMessage");
		messages.put(6516, "PaddockBuyResultMessage");
		messages.put(6052, "PaddockMoveItemRequestMessage");
		messages.put(5824, "PaddockPropertiesMessage");
		messages.put(5958, "PaddockRemoveItemRequestMessage");
		messages.put(6018, "PaddockSellBuyDialogMessage");
		messages.put(5953, "PaddockSellRequestMessage");
		messages.put(6161, "PaddockToSellFilterMessage");
		messages.put(6138, "PaddockToSellListMessage");
		messages.put(6141, "PaddockToSellListRequestMessage");
		messages.put(1508, "PartInfoMessage");
		messages.put(1502, "PartsListMessage");
		messages.put(6080, "PartyAbdicateThroneMessage");
		messages.put(5580, "PartyAcceptInvitationMessage");
		messages.put(6254, "PartyCancelInvitationMessage");
		messages.put(6251, "PartyCancelInvitationNotificationMessage");
		messages.put(5583, "PartyCannotJoinErrorMessage");
		messages.put(6472, "PartyCompanionUpdateLightMessage");
		messages.put(6261, "PartyDeletedMessage");
		messages.put(5577, "PartyFollowMemberRequestMessage");
		messages.put(5581, "PartyFollowStatusUpdateMessage");
		messages.put(5588, "PartyFollowThisMemberRequestMessage");
		messages.put(6283, "PartyInvitationArenaRequestMessage");
		messages.put(6256, "PartyInvitationCancelledForGuestMessage");
		messages.put(6263, "PartyInvitationDetailsMessage");
		messages.put(6264, "PartyInvitationDetailsRequestMessage");
		messages.put(6262, "PartyInvitationDungeonDetailsMessage");
		messages.put(6244, "PartyInvitationDungeonMessage");
		messages.put(6245, "PartyInvitationDungeonRequestMessage");
		messages.put(5586, "PartyInvitationMessage");
		messages.put(5585, "PartyInvitationRequestMessage");
		messages.put(5576, "PartyJoinMessage");
		messages.put(5592, "PartyKickRequestMessage");
		messages.put(5590, "PartyKickedByMessage");
		messages.put(5578, "PartyLeaderUpdateMessage");
		messages.put(5594, "PartyLeaveMessage");
		messages.put(5593, "PartyLeaveRequestMessage");
		messages.put(5595, "PartyLocateMembersMessage");
		messages.put(5587, "PartyLocateMembersRequestMessage");
		messages.put(6270, "PartyLoyaltyStatusMessage");
		messages.put(6252, "PartyMemberEjectedMessage");
		messages.put(6342, "PartyMemberInFightMessage");
		messages.put(5579, "PartyMemberRemoveMessage");
		messages.put(6277, "PartyModifiableStatusMessage");
		messages.put(6501, "PartyNameSetErrorMessage");
		messages.put(6503, "PartyNameSetRequestMessage");
		messages.put(6502, "PartyNameUpdateMessage");
		messages.put(6260, "PartyNewGuestMessage");
		messages.put(6306, "PartyNewMemberMessage");
		messages.put(6269, "PartyPledgeLoyaltyRequestMessage");
		messages.put(5582, "PartyRefuseInvitationMessage");
		messages.put(5596, "PartyRefuseInvitationNotificationMessage");
		messages.put(6175, "PartyRestrictedMessage");
		messages.put(5574, "PartyStopFollowRequestMessage");
		messages.put(6054, "PartyUpdateLightMessage");
		messages.put(5575, "PartyUpdateMessage");
		messages.put(6012, "PauseDialogMessage");
		messages.put(6385, "PlayerStatusUpdateErrorMessage");
		messages.put(6386, "PlayerStatusUpdateMessage");
		messages.put(6387, "PlayerStatusUpdateRequestMessage");
		messages.put(6134, "PopupWarningMessage");
		messages.put(6492, "PortalUseRequestMessage");
		messages.put(6042, "PrismAttackRequestMessage");
		messages.put(6452, "PrismFightAddedMessage");
		messages.put(5893, "PrismFightAttackerAddMessage");
		messages.put(5897, "PrismFightAttackerRemoveMessage");
		messages.put(5895, "PrismFightDefenderAddMessage");
		messages.put(5892, "PrismFightDefenderLeaveMessage");
		messages.put(5843, "PrismFightJoinLeaveRequestMessage");
		messages.put(6453, "PrismFightRemovedMessage");
		messages.put(6040, "PrismFightStateUpdateMessage");
		messages.put(5901, "PrismFightSwapRequestMessage");
		messages.put(5853, "PrismInfoCloseMessage");
		messages.put(5859, "PrismInfoInValidMessage");
		messages.put(5844, "PrismInfoJoinLeaveRequestMessage");
		messages.put(6531, "PrismModuleExchangeRequestMessage");
		messages.put(6466, "PrismSetSabotagedRefusedMessage");
		messages.put(6468, "PrismSetSabotagedRequestMessage");
		messages.put(6442, "PrismSettingsErrorMessage");
		messages.put(6437, "PrismSettingsRequestMessage");
		messages.put(6041, "PrismUseRequestMessage");
		messages.put(6451, "PrismsInfoValidMessage");
		messages.put(6440, "PrismsListMessage");
		messages.put(6441, "PrismsListRegisterMessage");
		messages.put(6438, "PrismsListUpdateMessage");
		messages.put(5739, "PurchasableDialogMessage");
		messages.put(5626, "QuestListMessage");
		messages.put(5623, "QuestListRequestMessage");
		messages.put(6098, "QuestObjectiveValidatedMessage");
		messages.put(6085, "QuestObjectiveValidationMessage");
		messages.put(5643, "QuestStartRequestMessage");
		messages.put(6091, "QuestStartedMessage");
		messages.put(5625, "QuestStepInfoMessage");
		messages.put(5622, "QuestStepInfoRequestMessage");
		messages.put(6096, "QuestStepStartedMessage");
		messages.put(6099, "QuestStepValidatedMessage");
		messages.put(6097, "QuestValidatedMessage");
		messages.put(6100, "QueueStatusMessage");
		messages.put(6253, "RawDataMessage");
		messages.put(6601, "RecycleResultMessage");
		messages.put(6540, "ReloginTokenRequestMessage");
		messages.put(6539, "ReloginTokenStatusMessage");
		messages.put(6630, "RoomAvailableUpdateMessage");
		messages.put(6469, "SelectedServerDataExtendedMessage");
		messages.put(42, "SelectedServerDataMessage");
		messages.put(41, "SelectedServerRefusedMessage");
		messages.put(956, "SequenceEndMessage");
		messages.put(6317, "SequenceNumberMessage");
		messages.put(6316, "SequenceNumberRequestMessage");
		messages.put(955, "SequenceStartMessage");
		messages.put(6237, "ServerExperienceModificatorMessage");
		messages.put(6305, "ServerOptionalFeaturesMessage");
		messages.put(40, "ServerSelectionMessage");
		messages.put(6434, "ServerSessionConstantsMessage");
		messages.put(6340, "ServerSettingsMessage");
		messages.put(50, "ServerStatusUpdateMessage");
		messages.put(30, "ServersListMessage");
		messages.put(170, "SetCharacterRestrictionsMessage");
		messages.put(6443, "SetEnableAVARequestMessage");
		messages.put(1810, "SetEnablePVPRequestMessage");
		messages.put(5503, "SetUpdateMessage");
		messages.put(6227, "ShortcutBarAddErrorMessage");
		messages.put(6225, "ShortcutBarAddRequestMessage");
		messages.put(6231, "ShortcutBarContentMessage");
		messages.put(6229, "ShortcutBarRefreshMessage");
		messages.put(6222, "ShortcutBarRemoveErrorMessage");
		messages.put(6228, "ShortcutBarRemoveRequestMessage");
		messages.put(6224, "ShortcutBarRemovedMessage");
		messages.put(6226, "ShortcutBarSwapErrorMessage");
		messages.put(6230, "ShortcutBarSwapRequestMessage");
		messages.put(5612, "ShowCellMessage");
		messages.put(5611, "ShowCellRequestMessage");
		messages.put(6158, "ShowCellSpectatorMessage");
		messages.put(6214, "SlaveSwitchContextMessage");
		messages.put(6011, "SpellItemBoostMessage");
		messages.put(1200, "SpellListMessage");
		messages.put(6653, "SpellModifyFailureMessage");
		messages.put(6655, "SpellModifyRequestMessage");
		messages.put(6654, "SpellModifySuccessMessage");
		messages.put(6355, "SpouseGetInformationsMessage");
		messages.put(6356, "SpouseInformationsMessage");
		messages.put(6265, "SpouseStatusMessage");
		messages.put(6538, "StartupActionAddMessage");
		messages.put(1304, "StartupActionFinishedMessage");
		messages.put(6537, "StartupActionsAllAttributionMessage");
		messages.put(1302, "StartupActionsExecuteMessage");
		messages.put(1301, "StartupActionsListMessage");
		messages.put(1303, "StartupActionsObjetAttributionMessage");
		messages.put(5709, "StatedElementUpdatedMessage");
		messages.put(5716, "StatedMapUpdateMessage");
		messages.put(5610, "StatsUpgradeRequestMessage");
		messages.put(5609, "StatsUpgradeResultMessage");
		messages.put(6124, "StopToListenRunningFightRequestMessage");
		messages.put(5646, "StorageInventoryContentMessage");
		messages.put(5645, "StorageKamasUpdateMessage");
		messages.put(5648, "StorageObjectRemoveMessage");
		messages.put(5647, "StorageObjectUpdateMessage");
		messages.put(6035, "StorageObjectsRemoveMessage");
		messages.put(6036, "StorageObjectsUpdateMessage");
		messages.put(5542, "SubscriptionLimitationMessage");
		messages.put(6616, "SubscriptionUpdateMessage");
		messages.put(5573, "SubscriptionZoneMessage");
		messages.put(6522, "SymbioticObjectAssociateRequestMessage");
		messages.put(6527, "SymbioticObjectAssociatedMessage");
		messages.put(6526, "SymbioticObjectErrorMessage");
		messages.put(189, "SystemMessageDisplayMessage");
		messages.put(5918, "TaxCollectorAttackedMessage");
		messages.put(5635, "TaxCollectorAttackedResultMessage");
		messages.put(5619, "TaxCollectorDialogQuestionBasicMessage");
		messages.put(5615, "TaxCollectorDialogQuestionExtendedMessage");
		messages.put(5634, "TaxCollectorErrorMessage");
		messages.put(5930, "TaxCollectorListMessage");
		messages.put(5917, "TaxCollectorMovementAddMessage");
		messages.put(5633, "TaxCollectorMovementMessage");
		messages.put(5915, "TaxCollectorMovementRemoveMessage");
		messages.put(6611, "TaxCollectorMovementsOfflineMessage");
		messages.put(6455, "TaxCollectorStateUpdateMessage");
		messages.put(6294, "TeleportBuddiesAnswerMessage");
		messages.put(6289, "TeleportBuddiesMessage");
		messages.put(6302, "TeleportBuddiesRequestedMessage");
		messages.put(5960, "TeleportDestinationsListMessage");
		messages.put(6646, "TeleportHavenBagAnswerMessage");
		messages.put(6647, "TeleportHavenBagRequestMessage");
		messages.put(6048, "TeleportOnSameMapMessage");
		messages.put(5961, "TeleportRequestMessage");
		messages.put(6293, "TeleportToBuddyAnswerMessage");
		messages.put(6303, "TeleportToBuddyCloseMessage");
		messages.put(6287, "TeleportToBuddyOfferMessage");
		messages.put(780, "TextInformationMessage");
		messages.put(6364, "TitleGainedMessage");
		messages.put(6371, "TitleLostMessage");
		messages.put(6373, "TitleSelectErrorMessage");
		messages.put(6365, "TitleSelectRequestMessage");
		messages.put(6366, "TitleSelectedMessage");
		messages.put(6367, "TitlesAndOrnamentsListMessage");
		messages.put(6363, "TitlesAndOrnamentsListRequestMessage");
		messages.put(6565, "TopTaxCollectorListMessage");
		messages.put(6491, "TreasureHuntAvailableRetryCountUpdateMessage");
		messages.put(6509, "TreasureHuntDigRequestAnswerFailedMessage");
		messages.put(6484, "TreasureHuntDigRequestAnswerMessage");
		messages.put(6485, "TreasureHuntDigRequestMessage");
		messages.put(6483, "TreasureHuntFinishedMessage");
		messages.put(6510, "TreasureHuntFlagRemoveRequestMessage");
		messages.put(6507, "TreasureHuntFlagRequestAnswerMessage");
		messages.put(6508, "TreasureHuntFlagRequestMessage");
		messages.put(6487, "TreasureHuntGiveUpRequestMessage");
		messages.put(6499, "TreasureHuntLegendaryRequestMessage");
		messages.put(6486, "TreasureHuntMessage");
		messages.put(6489, "TreasureHuntRequestAnswerMessage");
		messages.put(6488, "TreasureHuntRequestMessage");
		messages.put(6498, "TreasureHuntShowLegendaryUIMessage");
		messages.put(6267, "TrustStatusMessage");
		messages.put(6266, "URLOpenMessage");
		messages.put(5658, "UpdateLifePointsMessage");
		messages.put(6454, "UpdateMapPlayersAgressableStatusMessage");
		messages.put(6179, "UpdateMountBoostMessage");
		messages.put(6456, "UpdateSelfAgressableStatusMessage");
		messages.put(6512, "WarnOnPermaDeathMessage");
		messages.put(6513, "WarnOnPermaDeathStateMessage");
		messages.put(6523, "WrapperObjectAssociatedMessage");
		messages.put(6524, "WrapperObjectDissociateRequestMessage");
		messages.put(6529, "WrapperObjectErrorMessage");
		messages.put(1604, "ZaapListMessage");
		messages.put(6572, "ZaapRespawnSaveRequestMessage");
		messages.put(6571, "ZaapRespawnUpdatedMessage");
	}
}