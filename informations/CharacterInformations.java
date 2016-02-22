package informations;

import game.character.CharacterCharacteristicsInformations;
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
	public int regenRate;
	public int weight;
	public int weightMax;
	public CharacterCharacteristicsInformations stats;
	
	public CharacterInformations(String login, String password, int serverId) {
		this.login = login;
		this.password = password;
		this.serverId = serverId;
	}
	
	public int missingLife() {
		if(stats == null)
			return -1;
		return this.stats.maxLifePoints - this.stats.lifePoints;
	}

	public boolean weightMaxAlmostReached() {
		if(this.weightMax - this.weight < this.weight * 0.05) // moins de 5% restant
			return true;
		return false;
	}
}