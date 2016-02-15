package main;

import java.util.Iterator;
import java.util.Vector;

import roleplay.currentmap.GameRolePlayActorInformations;
import roleplay.currentmap.GameRolePlayGroupMonsterInformations;
import roleplay.currentmap.GameRolePlayNamedActorInformations;

public class RoleplayContext {
	private CharacterController CC;
	private Vector<GameRolePlayActorInformations> actors;
	
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
}