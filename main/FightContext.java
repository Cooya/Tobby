package main;

import java.util.Vector;

import roleplay.fight.GameFightFighterInformations;
import roleplay.fight.GameFightMonsterInformations;

public class FightContext {
	
	private CharacterController CC;
	
	private static Vector<GameFightFighterInformations> fighters;
	public boolean fight=false;
	public boolean turn=false;
	public boolean inAction=false;
	public GameFightFighterInformations selfInfo;
	public int nbMonstersAlive;
	public int skip;
	public boolean moveMonsters;
	
	
	public FightContext(CharacterController CC) {
		this.CC=CC;
	}

	public Vector<GameFightFighterInformations> getFighters(){
		return fighters;
	}
	
	public void newContextFightersInformations(Vector<GameFightFighterInformations> infos){
		fighters=infos;
		for(GameFightFighterInformations info: fighters)
			if(info.contextualId==CC.characterId)
				selfInfo=info;
	}
	

	public Vector<GameFightFighterInformations> getAliveMonsters() {
		Vector<GameFightFighterInformations> monsters = new Vector<GameFightFighterInformations>();
		for(GameFightFighterInformations fighter : fighters){
			if(fighter instanceof GameFightMonsterInformations && fighter.alive)
				monsters.add(fighter);		
		}
		return monsters;
	}
	
	
	
	public int lifeToRegen(){
		if(selfInfo==null)
			return 0;
		return selfInfo.stats.baseMaxLifePoints - selfInfo.stats.lifePoints;
	}
	
	
}
