package main;

import java.util.Iterator;
import java.util.Vector;

import roleplay.currentmap.GameRolePlayActorInformations;
import roleplay.currentmap.GameRolePlayGroupMonsterInformations;
import roleplay.currentmap.GameRolePlayNamedActorInformations;
import roleplay.fight.GameFightFighterInformations;
import roleplay.fight.GameFightMonsterInformations;

public class RoleplayContext {
	private CharacterController CC;
	private Vector<GameRolePlayActorInformations> actors;
	
	
	
	private static Vector<GameFightFighterInformations> FightersInformations;
	public boolean fight=false;
	public boolean turn=false;
	public boolean inAction=false;
	public GameFightFighterInformations selfInfo;
	public int nbMonstersAlive;
	public int skip;
	public boolean moveMonsters;
	
	public RoleplayContext(CharacterController CC) {
		this.CC = CC;
	}
	
	public void newContextActors(Vector<GameRolePlayActorInformations> actors) {
		this.actors = actors;
		
		double characterId = CC.characterId;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == characterId)
				CC.characterName = ((GameRolePlayNamedActorInformations) actor).name;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == characterId) {
				CC.currentCellId = actor.disposition.cellId;
				CC.currentDirection = actor.disposition.direction;
			}
	}
	
	public void updateContextActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == actorId)
				actor.disposition.cellId = position;
	}
	
	public void addContextActor(GameRolePlayActorInformations actor) {
		actors.add(actor);
	}
	
	public void removeContextActor(double actorId) {
		Iterator<GameRolePlayActorInformations> it = this.actors.iterator();
		while(it.hasNext())
			if(it.next().contextualId == actorId)
				it.remove();
	}
	
	public Vector<Integer> getCellIdsTakenByMonsters() {
		Vector<Integer> cellIds = new Vector<Integer>();
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				cellIds.add(actor.disposition.cellId);		
		return cellIds;
	}
	
	public double getActorIdByName(String name) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayNamedActorInformations && ((GameRolePlayNamedActorInformations) actor).name == name)
				return actor.contextualId;
		return -1;
	}
	
	public Vector<GameFightFighterInformations> getFighters(){
		return FightersInformations;
	}
	
	public void newContextFightersInformations(Vector<GameFightFighterInformations> infos){
		FightersInformations=infos;
		for(GameFightFighterInformations info: FightersInformations)
			if(info.contextualId==CC.characterId)
				selfInfo=info;
	}
	
	public Vector<GameRolePlayGroupMonsterInformations> getMonsters() {
		Vector<GameRolePlayGroupMonsterInformations> monsters = new Vector<GameRolePlayGroupMonsterInformations>();
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				monsters.add((GameRolePlayGroupMonsterInformations) actor);	
		return monsters;
	}
	
	public Vector<GameFightFighterInformations> getAliveMonsters() {
		Vector<GameFightFighterInformations> monsters = new Vector<GameFightFighterInformations>();
		for(GameFightFighterInformations fighter : FightersInformations){
			if(fighter instanceof GameFightMonsterInformations && fighter.alive)
				monsters.add(fighter);		
		}
		return monsters;
	}
	
	public GameRolePlayActorInformations getActorById(double id){
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId==id){
				return actor;
			}
		return null;
	}
	
	
	public int lifeToRegen(){
		if(selfInfo==null)
			return 0;
		return selfInfo.stats.baseMaxLifePoints - selfInfo.stats.lifePoints;
	}
}