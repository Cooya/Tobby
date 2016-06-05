package controller.modules;

import gamedata.enums.ServerEnum;

import java.util.HashMap;
import java.util.Map;

import messages.character.BasicWhoIsRequestMessage;
import controller.CharacterState;
import controller.characters.Character;

public class ModeratorDetection {
	private static Map<Integer, String> moderators = new HashMap<Integer, String>(20);
	private static Map<Integer, Long> lastWHOISTimes = new HashMap<Integer, Long>(20);
	
	static {
		moderators.put(1, "[Jial]");
		moderators.put(2, null);
		moderators.put(3, "[Lytimelle]");
		moderators.put(4, "[Ylleh]");
		moderators.put(5, null); // "[Gazviv]"
		moderators.put(6, "[Yesht]");
		moderators.put(7, "[Arkansyelle]");
		moderators.put(8, null);
		moderators.put(9, "[Simeth]");
		moderators.put(10, "[Eknelis]");
		moderators.put(11, "[Alkalino]");
		moderators.put(12, "[Latnac]");
		moderators.put(13, "[Vandavarya]");
		moderators.put(14, "[Gowolik]");
		moderators.put(15, "[Miaidaouh]");
		moderators.put(16, "[Saskhya]");
		moderators.put(17, "[Haeo-Lien]");
		moderators.put(18, "[Griffinx]");
		moderators.put(19, "[Lobeline]");
		moderators.put(20, "[Eradror]");
		
		for(int serverId : ServerEnum.getServerIdsList())
			lastWHOISTimes.put(serverId, 0l);
	}
	
	private Character character;
	private int serverId;
	private String moderator;

	public ModeratorDetection(Character character) {
		this.character = character;
		this.serverId = character.infos.getServerId();
		this.moderator = moderators.get(this.serverId);
	}
	
	public void detectModerator() {
		if(this.moderator == null)
			return;
		
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastWHOISTimes.get(this.serverId) > 120000 && this.character.infos.isInGame()) { // plus de deux minutes
			checkIfModeratorIsOnline(this.moderator);
			lastWHOISTimes.put(this.serverId, currentTime);
		}
	}
	
	public void cancelModeratorDetection() {
		this.moderator = null;
	}
	
	// envoie une requête WHOIS pour savoir si le modérateur du serveur est en ligne
	private void checkIfModeratorIsOnline(String moderatorName) {
		BasicWhoIsRequestMessage BWIRM = new BasicWhoIsRequestMessage();
		BWIRM.verbose = true;
		BWIRM.search = moderatorName;
		this.character.net.send(BWIRM);
		this.character.log.p("Checking if moderator is online.");
		this.character.waitState(CharacterState.WHOIS_RESPONSE);
	}
}