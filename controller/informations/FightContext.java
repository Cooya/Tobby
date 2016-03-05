package controller.informations;

import gamedata.fight.GameFightFighterInformations;
import gamedata.fight.GameFightMonsterInformations;

import java.util.Vector;

import controller.CharacterController;

public class FightContext {
	private CharacterController CC;
	public Vector<GameFightFighterInformations> fighters;
	public GameFightFighterInformations self;
	public boolean lastFightOutcome;
	
	public FightContext(CharacterController CC) {
		this.CC = CC;
	}
	
	public void setFightContext(Vector<GameFightFighterInformations> fighters) {
		this.fighters = fighters;
		for(GameFightFighterInformations fighter : this.fighters)
			if(fighter.contextualId == this.CC.infos.characterId)
				this.self = fighter;
	}

	public Vector<GameFightMonsterInformations> getAliveMonsters() {
		Vector<GameFightMonsterInformations> monsters = new Vector<GameFightMonsterInformations>();
		for(GameFightFighterInformations fighter : this.fighters)
			if(fighter instanceof GameFightMonsterInformations && fighter.alive)
				monsters.add((GameFightMonsterInformations) fighter);		
		return monsters;
	}
}