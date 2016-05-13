package controller.informations;

import gamedata.context.GameRolePlayActorInformations;
import gamedata.context.GameRolePlayGroupMonsterInformations;
import gamedata.context.GameRolePlayNamedActorInformations;
import gamedata.context.GameRolePlayNpcInformations;
import gamedata.context.InteractiveElement;

import java.util.Iterator;
import java.util.Vector;

import controller.characters.Character;

public class RoleplayContext {
	private Character character;
	private Vector<GameRolePlayActorInformations> actors;
	private Vector<InteractiveElement> interactives;
	public double actorDemandingExchange;
	public boolean lastFightOutcome;
	public boolean lastExchangeDemandOutcome;
	public boolean lastExchangeOutcome;
	
	public RoleplayContext(Character CC) {
		this.character = CC;
	}
	
	public synchronized void newContextActors(Vector<GameRolePlayActorInformations> actors) {
		this.actors = actors;
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == this.character.infos.getCharacterId())
				this.character.infos.setCurrentCellId(actor.disposition.cellId);
	}
	
	public void newContextInteractives(Vector<InteractiveElement> interactives) {
		this.interactives = interactives;
	}

	public synchronized void updateContextActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == actorId)
				actor.disposition.cellId = position;
	} 
	
	public synchronized void addContextActor(GameRolePlayActorInformations actor) {
		this.actors.add(actor);
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
	
	public synchronized double getNpcContextualId(int npcId) {
		GameRolePlayNpcInformations npc;
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor instanceof GameRolePlayNpcInformations) {
				npc = (GameRolePlayNpcInformations) actor;
				if(npc.npcId == npcId)
					return npc.contextualId;
			}
		return 0;
	}
	
	public int getInteractiveSkillInstanceUid(int elementId) {
		for(InteractiveElement interactive : this.interactives)
			if(interactive.elementId == elementId)
				return interactive.enabledSkills.firstElement().skillInstanceUid;
		return 0;
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