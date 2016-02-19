package informations;

import game.currentmap.GameRolePlayActorInformations;
import game.currentmap.GameRolePlayGroupMonsterInformations;
import game.currentmap.GameRolePlayNamedActorInformations;

import java.util.Iterator;
import java.util.Vector;

import main.CharacterController;
import main.Event;
import utilities.Log;

public class RoleplayContext {
	private CharacterController CC;
	private Vector<GameRolePlayActorInformations> actors;
	
	public RoleplayContext(CharacterController CC) {
		this.CC = CC;
	}
	
	public void newContextActors(Vector<GameRolePlayActorInformations> actors) {
		this.actors = actors;
		
		double characterId = CC.infos.characterId;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == characterId)
				CC.infos.characterName = ((GameRolePlayNamedActorInformations) actor).name;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == characterId) {
				CC.infos.currentCellId = actor.disposition.cellId;
				CC.infos.currentDirection = actor.disposition.direction;
			}
	}
	
	public void updateContextActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == actorId) {
				if(actor instanceof GameRolePlayGroupMonsterInformations)
					Log.p("Monster group is moving from cell id " + actor.disposition.cellId + " to " + position + ".");
				actor.disposition.cellId = position;
			}
	} 
	
	public void addContextActor(GameRolePlayActorInformations actor) {
		actors.add(actor);
		if(actor instanceof GameRolePlayGroupMonsterInformations)
			this.CC.emit(Event.MONSTER_GROUP_RESPAWN);
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
	
	public GameRolePlayActorInformations getActorById(double id) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == id)
				return actor;
		return null;
	}
	
	public Vector<GameRolePlayGroupMonsterInformations> getMonsterGroups() {
		Vector<GameRolePlayGroupMonsterInformations> monsters = new Vector<GameRolePlayGroupMonsterInformations>();
		for(GameRolePlayActorInformations actor : actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				monsters.add((GameRolePlayGroupMonsterInformations) actor);	
		return monsters;
	}
	
	public int getMonsterGroupCellId(GameRolePlayGroupMonsterInformations monsterGroup) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == monsterGroup.contextualId)
				return actor.disposition.cellId;
		return -1;
	}
}