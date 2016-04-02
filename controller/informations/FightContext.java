package controller.informations;

import gamedata.fight.GameFightFighterInformations;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.characters.Character;

public class FightContext {
	private Character character;
	public GameFightFighterInformations self;
	public Vector<GameFightFighterInformations> fighters;
	public Vector<Integer> positionsForChallengers;
	public Vector<Integer> positionsForDefenders;
	
	public FightContext(Character character) {
		this.character = character;
	}
	
	public synchronized void clearFightContext() {
		this.self = null;
		this.fighters = null;
		this.positionsForChallengers = null;
		this.positionsForDefenders = null;
	}
	
	// phase de préparation du combat
	public synchronized void newFighter(GameFightFighterInformations fighter) {
		if(this.fighters == null)
			this.fighters = new Vector<GameFightFighterInformations>();
		this.fighters.add(fighter);
	}
	
	// appelée à chaque synchronisation (nouveau tour)
	public synchronized void setFightContext(Vector<GameFightFighterInformations> fighters) {
		this.fighters = fighters;
		for(GameFightFighterInformations fighter : this.fighters)
			if(fighter.contextualId == this.character.infos.characterId)
				this.self = fighter;
	}

	public synchronized Vector<GameFightMonsterInformations> getAliveMonsters() {
		Vector<GameFightMonsterInformations> monsters = new Vector<GameFightMonsterInformations>();
		for(GameFightFighterInformations fighter : this.fighters)
			if(fighter instanceof GameFightMonsterInformations && fighter.alive)
				monsters.add((GameFightMonsterInformations) fighter);		
		return monsters;
	}
	
	public synchronized void setPossiblePositions(Vector<Integer> positionsForChallengers, Vector<Integer> positionsForDefenders) {
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