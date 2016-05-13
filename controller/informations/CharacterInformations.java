package controller.informations;

import java.util.HashMap;
import java.util.Vector;

import main.FatalError;
import gamedata.character.CharacterCharacteristicsInformations;
import gamedata.d2p.ankama.Map;
import gamedata.enums.BreedEnum;
import gamedata.enums.ElementEnum;
import gamedata.enums.PlayerLifeStatusEnum;
import gamedata.inventory.SpellItem;

public class CharacterInformations {
	private boolean isInGame;
	private boolean firstSelection;
	private String login;
	private String password;
	private int serverId;
	private String characterName;
	private double characterId;
	private int breed;
	private int healthState;
	private int currentCellId;
	private Map currentMap;
	private int regenRate;
	private int weight;
	private int weightMax;
	private int level;
	private int lifePoints;
	private int maxLifePoints;
	private int energyPoints;
	private int kamas;
	private double experience;
	private double experienceNextLevelFloor;
	private int statsPoints;
	private int strengthBase;
	private int chanceBase;
	private int agilityBase;
	private int intelligenceBase;
	private int spellsPoints;
	private HashMap<Integer, SpellItem> spellList;
	private int attackSpell;
	private int attackSpellActionPoints;
	private int element;
	private int fightsWonCounter;
	private int fightsLostCounter;
	private int mapsTravelledCounter;
	
	public CharacterInformations(String login, String password, int serverId, int breed) {
		this.firstSelection = false;
		this.isInGame = false;
		this.login = login;
		this.password = password;
		this.serverId = serverId;
		this.healthState = PlayerLifeStatusEnum.STATUS_ALIVE_AND_KICKING;
		this.mapsTravelledCounter = 1;
		setBreed(breed);
	}
	
	public int missingLife() {
		return this.maxLifePoints - this.lifePoints;
	}

	public boolean weightMaxAlmostReached() {
		if(this.weightMax - this.weight < this.weight * 0.05) // moins de 5% restant
			return true;
		return false;
	}
	
	// determine si l'inventaire est plein ou pas selon le pourcentage donné
	public boolean inventoryIsFull(float percentage) { // percentage < 1
		if(this.weight > this.weightMax * percentage)
			return true;
		return false;
	}
	
	// retourne le nombre de points de caractéristique maxmimal qu'il est possible d'augmenter 
	public int calculateMaxStatsPoints() {
		return this.statsPoints - (this.statsPoints % (getElementInfoById() / 100 + 1));
	}
	
	public boolean isInGame() {
		return this.isInGame;
	}
	
	public String getLogin() {
		return this.login;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public int getServerId() {
		return this.serverId;
	}
	
	public String getCharacterName() {
		return this.characterName;
	}
	
	public double getCharacterId() {
		return this.characterId;
	}
	
	public int getBreed() {
		return this.breed;
	}
	
	public int getHealthState() {
		return this.healthState;
	}
	
	public int getCurrentCellId() {
		return this.currentCellId;
	}
	
	public Map getCurrentMap() {
		return this.currentMap;
	}
	
	public int getRegenRate() {
		return this.regenRate;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public int getMaxLifePoints() {
		return this.maxLifePoints;
	}
	
	public int getKamas() {
		return this.kamas;
	}
	
	public int getSpellsPoints() {
		return this.spellsPoints;
	}
	
	public HashMap<Integer, SpellItem> getSpellList() {
		return this.spellList;
	}
	
	public int getAttackSpell() {
		return this.attackSpell;
	}
	
	public int getAttackSpellActionPoints() {
		return this.attackSpellActionPoints;
	}
	
	public int getElement() {
		return this.element;
	}
	
	public int getFightsWonCounter() {
		return this.fightsWonCounter;
	}
	
	public int getFightsLostCounter() {
		return this.fightsLostCounter;
	}
	
	public void inGame(boolean isInGame) {
		this.isInGame = isInGame;
	}
	
	public boolean firstSelection() {
		return this.firstSelection;
	}
	
	public void setFirstSelection(boolean firstSelection) {
		this.firstSelection = firstSelection;
	}
	
	public void setCharacterId(double characterId) {
		this.characterId = characterId;
	}
	
	public void setCharacterName(String characterName) {
		this.characterName = characterName;
		//this.graphicalFrame.setNameLabel(this.characterName, this.level);
	}
	
	public void setHealthState(int healthState) {
		this.healthState = healthState;
	}
	
	public void setCurrentCellId(int cellId) {
		this.currentCellId = cellId;
		//this.graphicalFrame.setCellLabel(String.valueOf(this.currentCellId));
	}
	
	public void setCurrentMap(Map map) {
		this.currentMap = map;
		//this.graphicalFrame.setMapLabel(String.valueOf(MapPosition.getMapPositionById(this.currentMap.id)));
	}
	
	public void setRegenRate(int rate) {
		this.regenRate = rate;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
		//this.graphicalFrame.setWeightLabel(this.weight, this.weightMax);
	}
	
	public void setWeightMax(int max) {
		this.weightMax = max;
		//this.graphicalFrame.setWeightLabel(this.weight, this.weightMax);
	}
	
	public void setLevel(int level) {
		this.level = level;
		//this.graphicalFrame.setNameLabel(this.characterName, this.level);
	}
	
	public void setLifePoints(int lifePoints) {
		this.lifePoints = lifePoints;
		//this.graphicalFrame.setLifeLabel(this.lifePoints, this.maxLifePoints);
	}
	
	public void setStats(CharacterCharacteristicsInformations stats) {
		this.lifePoints = stats.lifePoints;
		this.maxLifePoints = stats.maxLifePoints;
		this.energyPoints = stats.energyPoints;
		this.kamas = stats.kamas;
		this.experience = stats.experience;
		this.experienceNextLevelFloor = stats.experienceNextLevelFloor;
		this.statsPoints = stats.statsPoints;
		this.strengthBase = stats.strength.base;
		this.chanceBase = stats.chance.base;
		this.agilityBase = stats.agility.base;
		this.intelligenceBase = stats.intelligence.base;
		this.spellsPoints = stats.spellsPoints;
		
		//this.graphicalFrame.setLifeLabel(this.lifePoints, this.maxLifePoints);
		//this.graphicalFrame.setEnergyLabel(this.energyPoints, 10000);
		//this.graphicalFrame.setKamasLabel(this.kamas);
		//this.graphicalFrame.setExperienceLabel(this.experience, this.experienceNextLevelFloor);
	}
	
	public void setSpellList(Vector<SpellItem> spells) {
		this.spellList = new HashMap<Integer, SpellItem>();
		for(SpellItem spellItem : spells)
			this.spellList.put(spellItem.spellId, spellItem);
	}
	
	public void incFightsWonCounter() {
		++this.fightsWonCounter;
		//this.graphicalFrame.setFightsWonLabel(++this.fightsWonCounter);
	}
	
	public void incFightsLostCounter() {
		++this.fightsLostCounter;
		//this.graphicalFrame.setFightsLostLabel(++this.fightsLostCounter);
	}
	
	public void incMapsTravelledCounter() {
		++this.mapsTravelledCounter;
		//this.graphicalFrame.setMapsTravelledCounter(++this.mapsTravelledCounter);
	}
	
	private void setBreed(int breed) {
		this.breed = breed;
		if(this.breed == BreedEnum.Cra) { // crâ
			this.attackSpell = 161; // flèche magique
			this.attackSpellActionPoints = 4;
			this.element = ElementEnum.intelligence;
		}
		else if(this.breed == BreedEnum.Sadida) { // sadida
			this.attackSpell = 183; // ronce
			this.attackSpellActionPoints = 3;
			this.element = ElementEnum.strength;
		}
		else
			throw new FatalError("Unhandled breed character.");
	}
	
	private int getElementInfoById() {
		switch(this.element) {
			case 10 : return this.strengthBase;
			case 13 : return this.chanceBase;
			case 14 : return this.agilityBase;
			case 15 : return this.intelligenceBase;
		}
		return 0;
	}
}