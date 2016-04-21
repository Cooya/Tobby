package controller.informations;

import java.util.HashMap;
import java.util.Vector;

import main.FatalError;
import gamedata.character.BreedEnum;
import gamedata.character.CharacterCharacteristicsInformations;
import gamedata.character.Elements;
import gamedata.character.PlayerLifeStatusEnum;
import gamedata.d2p.ankama.Map;
import gamedata.inventory.SpellItem;

public class CharacterInformations {
	public boolean isConnected;
	public boolean firstSelection;
	public String login;
	public String password;
	public int serverId;
	public String characterName;
	public double characterId;
	public int healthState;
	public int status;
	public int currentCellId;
	public int currentDirection;
	public Map currentMap;
	public int regenRate;
	public int weight;
	public int weightMax;
	public int level;
	public CharacterCharacteristicsInformations stats;
	public HashMap<Integer, SpellItem> spellList; 
	public int attackSpell;
	public int attackSpellActionPoints;
	public int element;
	public int fightsWonCounter;
	public int fightsLostCounter;
	public int mapsTravelled;
	private int breed;
	
	public CharacterInformations(String login, String password, int serverId, int breed) {
		this.firstSelection = false;
		this.isConnected = false;
		this.login = login;
		this.password = password;
		this.serverId = serverId;
		this.status = 1; // statut inconnu
		this.healthState = PlayerLifeStatusEnum.STATUS_ALIVE_AND_KICKING;
		this.mapsTravelled = 1;
		setBreed(breed);
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
	
	public void loadSpellList(Vector<SpellItem> spells) {
		spellList = new HashMap<Integer, SpellItem>();
		for(SpellItem spellItem : spells)
			spellList.put(spellItem.spellId, spellItem);
	}
	
	public int getBreed() {
		return this.breed;
	}
	
	private void setBreed(int breed) {
		this.breed = breed;
		if(this.breed == BreedEnum.Cra) { // crâ
			this.attackSpell = 161; // flèche magique
			this.attackSpellActionPoints = 4;
			this.element = Elements.intelligence;
		}
		else if(this.breed == BreedEnum.Sadida) { // sadida
			this.attackSpell = 183; // ronce
			this.attackSpellActionPoints = 3;
			this.element = Elements.strength;
		}
		else
			throw new FatalError("Unhandled breed character.");
	}
}