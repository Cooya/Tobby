package controller.informations;

import gamedata.context.GameRolePlayActorInformations;
import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.context.GameRolePlayNamedActorInformations;

import java.util.Iterator;
import java.util.Vector;

import controller.CharacterController;

public class RoleplayContext {
	private CharacterController character;
	private Vector<GameRolePlayActorInformations> actors;
	public double actorDemandingExchange;
	public boolean lastFightOutcome;
	public boolean lastExchangeOutcome;
	public int currentCaptainFightId;
	
	public RoleplayContext(CharacterController CC) {
		this.character = CC;
	}
	
	public synchronized void newContextActors(Vector<GameRolePlayActorInformations> actors) {
		this.actors = actors;
		
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == this.character.infos.characterId)
				this.character.infos.characterName = ((GameRolePlayNamedActorInformations) actor).name;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == this.character.infos.characterId) {
				this.character.infos.currentCellId = actor.disposition.cellId;
				this.character.infos.currentDirection = actor.disposition.direction;
			}
	}
	
	public synchronized void updateContextActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == actorId) {
				//if(actor instanceof GameRolePlayGroupMonsterInformations)
					//Instance.log("Monster group is moving from cell id " + actor.disposition.cellId + " to " + position + ".");
				actor.disposition.cellId = position;
			}
	} 
	
	public synchronized void addContextActor(GameRolePlayActorInformations actor) {
		actors.add(actor);
	}
	
	public synchronized void removeContextActor(double actorId) {
		Iterator<GameRolePlayActorInformations> it = this.actors.iterator();
		while(it.hasNext())
			if(it.next().contextualId == actorId)
				it.remove();
	}
	
	public synchronized Vector<Integer> getCellIdsTakenByMonsters() {
		Vector<Integer> cellIds = new Vector<Integer>();
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				cellIds.add(actor.disposition.cellId);		
		return cellIds;
	}
	
	public synchronized double getActorIdByName(String name) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayNamedActorInformations && ((GameRolePlayNamedActorInformations) actor).name == name)
				return actor.contextualId;
		return -1;
	}
	
	public synchronized GameRolePlayActorInformations getActorById(double id) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == id)
				return actor;
		return null;
	}
	
	public synchronized Vector<GameRolePlayGroupMonsterInformations> getMonsterGroups() {
		Vector<GameRolePlayGroupMonsterInformations> monsters = new Vector<GameRolePlayGroupMonsterInformations>();
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				monsters.add((GameRolePlayGroupMonsterInformations) actor);	
		return monsters;
	}
	
	public synchronized int getMonsterGroupCellId(GameRolePlayGroupMonsterInformations monsterGroup) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == monsterGroup.contextualId)
				return actor.disposition.cellId;
		return -1;
	}
	
	public synchronized boolean actorIsOnMap(double actorId) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == actorId)
				return true;
		return false;
	}
}