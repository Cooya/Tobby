package controller.informations;

import gamedata.fight.GameFightFighterInformations;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.characters.Character;

public class FightContext {
	private Character character;
	public GameFightFighterInformations self;
	public Vector<GameFightFighterInformations> fighters;
	public int[] positionsForChallengers;
	public int[] positionsForDefenders;
	
	public FightContext(Character character) {
		this.character = character;
		this.fighters = new Vector<GameFightFighterInformations>(16);
	}
	
	public synchronized void clearFightContext() {
		this.self = null;
		this.fighters.clear();
		this.positionsForChallengers = null;
		this.positionsForDefenders = null;
	}
	
	// phase de préparation du combat
	public synchronized void newFighter(GameFightFighterInformations fighter) {
		this.fighters.add(fighter);
	}
	
	// appelée à chaque synchronisation (nouveau tour)
	public synchronized void setFightContext(GameFightFighterInformations[] fighters) {
		this.fighters.clear();
		double characterId = this.character.infos.getCharacterId();
		for(GameFightFighterInformations fighter : fighters) {
			this.fighters.add(fighter);
			if(fighter.contextualId == characterId)
				this.self = fighter;
		}
	}

	public synchronized Vector<GameFightMonsterInformations> getAliveMonsters() {
		Vector<GameFightMonsterInformations> monsters = new Vector<GameFightMonsterInformations>(this.fighters.size());
		for(GameFightFighterInformations fighter : this.fighters)
			if(fighter instanceof GameFightMonsterInformations && fighter.alive)
				monsters.add((GameFightMonsterInformations) fighter);		
		return monsters;
	}
	
	public synchronized void setPossiblePositions(int[] positionsForChallengers, int[] positionsForDefenders) {
		this.positionsForChallengers = positionsForChallengers;
		this.positionsForDefenders = positionsForDefenders;
	}
	
	public synchronized boolean inFight(double characterId) {
		if(this.fighters != null) {
			for(GameFightFighterInformations fighter : this.fighters)
				if(fighter.contextualId == characterId)
					return true;
		}
		return false;
	}
}