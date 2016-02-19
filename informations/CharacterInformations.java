package informations;

import game.d2p.ankama.Map;

public class CharacterInformations {
	public String login;
	public String password;
	public int serverId;
	public String characterName;
	public double characterId;
	public int currentCellId;
	public int currentDirection;
	public Map currentMap;
	public int kamasNumber;
	public int missingLife;
	public int regenRate;
	
	public CharacterInformations(String login, String password, int serverId) {
		this.login = login;
		this.password = password;
		this.serverId = serverId;
	}
}