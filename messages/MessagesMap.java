package messages;

import utilities.BiMap;

public class MessagesMap {
	private static BiMap<Integer, String> map = new BiMap<Integer, String>(Integer.class, String.class);

	public static String get(int id) {
		return (String) map.get(id);
	}
	
	public static int get(String name) {
		return (int) map.get(name);
	}
	
	static {
		map.put(1   , "ProtocolRequired");
		map.put(2   , "NetworkDataContainerMessage");
		map.put(3   , "HelloConnectMessage");
		map.put(4   , "IdentificationMessage");
		map.put(10  , "LoginQueueStatusMessage");
		map.put(20  , "IdentificationFailedMessage");
		map.put(21  , "IdentificationFailedForBadVersionMessage");
		map.put(22  , "IdentificationSuccessMessage");
		map.put(30  , "ServersListMessage");
		map.put(40  , "ServerSelectionMessage");
		map.put(41  , "SelectedServerRefusedMessage");
		map.put(42  , "SelectedServerDataMessage");
		map.put(50  , "ServerStatusUpdateMessage");
		map.put(101 , "HelloGameMessage");
		map.put(109 , "AlreadyConnectedMessage");
		map.put(110 , "AuthenticationTicketMessage");
		map.put(111 , "AuthentificationTicketAcceptedMessage");
		map.put(150 , "CharactersListRequestMessage");
		map.put(151 , "CharactersListMessage");
		map.put(152 , "CharacterSelectionMessage");
		map.put(153 , "CharacterSelectedSuccessMessage");
		map.put(170 , "SetCharacterRestrictionsMessage");
		map.put(175 , "AtlasPointsInformations");
		map.put(176 , "BasicNoOperationMessage");
		map.put(189 , "SystemMessageDisplayMessage");
		map.put(200 , "GameContextCreateMessage");
		map.put(201 , "ActorAlignmentInformations");
		map.put(210 , "MapFightCountMessage");
		map.put(220 , "CurrentMapMessage");
		map.put(225 , "MapInformationsRequestMessage");
		map.put(226 , "MapComplementaryInformationsDataMessage");
		map.put(250 , "GameContextCreateRequestMessage");
		map.put(251 , "GameContextRemoveElementMessage");
		map.put(300 , "GameRolePlayRemoveChallengeMessage");
		map.put(301 , "GameRolePlayShowChallengeMessage");
		map.put(500 , "CharacterStatsListMessage");
		map.put(711 , "GameFightRemoveTeamMemberMessage");
		map.put(780 , "TextInformationMessage");
		map.put(801 , "ChatSmileyMessage");
		map.put(851 , "ChatClientPrivateMessage");
		map.put(870 , "ChatErrorMessage");
		map.put(881 , "ChatServerMessage");
		map.put(882 , "ChatServerCopyMessage");
		map.put(883 , "ChatServerWithObjectMessage");
		map.put(890 , "ChannelEnablingMessage");
		map.put(891 , "ChannelEnablingChangeMessage");
		map.put(892 , "EnabledChannelsMessage");
		map.put(946 , "GameMapChangeOrientationMessage");
		map.put(951 , "GameMapMovementMessage");
		map.put(1200, "SpellListMessage");
		map.put(1301, "StartupActionsListMessage");
		map.put(3009, "InventoryWeightMessage");
		map.put(3016, "InventoryContentMessage");
		map.put(4001, "FriendsGetListMessage");
		map.put(4002, "FriendsListMessage");
		map.put(5503, "SetUpdateMessage");
		map.put(5572, "GameFightUpdateTeamMessage");
		map.put(5607, "ClientKeyMessage");
		map.put(5623, "QuestListRequestMessage");
		map.put(5626, "QuestListMessage");
		map.put(5630, "FriendWarnOnConnectionStateMessage");
		map.put(5632, "GameRolePlayShowActorMessage");
		map.put(5637, "GameContextRefreshEntityLookMessage");
		map.put(5652, "JobCrafterDirectorySettingsMessage");
		map.put(5655, "JobDescriptionMessage");
		map.put(5674, "IgnoredListMessage");
		map.put(5676, "IgnoredGetListMessage");
		map.put(5683, "EmotePlayMessage");
		map.put(5684, "LifePointsRegenBeginMessage");
		map.put(5689, "EmoteListMessage");
		map.put(5726, "OnConnectionEventMessage");
		map.put(5745, "InteractiveUsedMessage");
		map.put(5809, "JobExperienceMultiUpdateMessage");
		map.put(5816, "BasicLatencyStatsRequestMessage");
		map.put(5834, "SpellForgottenMessage");
		map.put(5836, "CharacterSelectedErrorMessage");
		map.put(5927, "GameFightOptionStateUpdateMessage");
		map.put(5968, "MountSetMessage");
		map.put(5970, "MountXpRatioMessage");
		map.put(6058, "AlignmentRankUpdateMessage");
		map.put(6078, "FriendWarnOnLevelGainStateMessage");
		map.put(6087, "NotificationListMessage");
		map.put(6100, "QueueStatusMessage");
		map.put(6160, "GuildMemberWarnOnConnectionStateMessage");
		map.put(6162, "InventoryContentAndPresetMessage");
		map.put(6205, "AchievementListMessage");
		map.put(6216, "AccountCapabilitiesMessage");
		map.put(6231, "ShortcutBarContentMessage");
		map.put(6253, "RawDataMessage");
		map.put(6265, "SpouseStatusMessage");
		map.put(6267, "TrustStatusMessage");
		map.put(6275, "MailStatusMessage");
		map.put(6301, "GameRolePlayArenaUpdatePlayerInfosMessage");
		map.put(6305, "ServerOptionalFeaturesMessage");
		map.put(6314, "CredentialsAcknowledgementMessage");
		map.put(6316, "SequenceNumberRequestMessage");
		map.put(6317, "SequenceNumberMessage");
		map.put(6334, "ObjectAveragePricesGetMessage");
		map.put(6339, "CharacterCapabilitiesMessage");
		map.put(6340, "ServerSettingsMessage");
		map.put(6341, "AlmanachCalendarDateMessage");
		map.put(6355, "SpouseGetInformationsMessage");
		map.put(6362, "BasicAckMessage");
		map.put(6372, "CheckIntegrityMessage");
		map.put(6383, "FriendGuildWarnOnAchievementCompleteStateMessage");
		map.put(6434, "ServerSessionConstantsMessage");
		map.put(6440, "PrismsListMessage");
		map.put(6441, "PrismsListRegisterMessage");
		map.put(6454, "UpdateMapPlayersAgressableStatusMessage");
		map.put(6469, "SelectedServerDataExtendedMessage");
		map.put(6471, "CharacterLoadingCompleteMessage");
		map.put(6513, "WarnOnPermaDeathStateMessage");
		map.put(6585, "IdolListMessage");
		map.put(6620, "HavenBagPackListMessage");
		map.put(6630, "RoomAvailableUpdateMessage");
	}
}
