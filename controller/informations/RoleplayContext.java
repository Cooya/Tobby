package controller.informations;

import gamedata.context.FightCommonInformations;
import gamedata.context.FightTeamInformations;
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
	private Vector<FightCommonInformations> fights;
	private InteractiveElement[] interactives;
	public double actorDemandingExchange;
	public boolean lastFightOutcome;
	public boolean lastExchangeDemandOutcome;
	public boolean lastExchangeOutcome;
	
	public RoleplayContext(Character CC) {
		this.character = CC;
		this.actors = new Vector<GameRolePlayActorInformations>(10);
		this.fights = new Vector<FightCommonInformations>(3);
	}
	
	public synchronized void newContextActors(GameRolePlayActorInformations[] actors) {
		this.actors.clear();
		for(GameRolePlayActorInformations actor : actors) {
			this.actors.add(actor);
			if(actor.contextualId == this.character.infos.getCharacterId())
				this.character.infos.setCurrentCellId(actor.disposition.cellId);
		}
	}
	
	public synchronized void newContextFights(FightCommonInformations[] fights) {
		this.fights.clear();
		for(FightCommonInformations fight : this.fights)
			this.fights.add(fight);
	}
	
	public synchronized void newContextInteractives(InteractiveElement[] interactives) {
		this.interactives = interactives;
	}

	public synchronized void updateContextActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == actorId)
				actor.disposition.cellId = position;
	} 
	
	public synchronized void updateMapFightCount(int fightCount) {
		if(fightCount == 0)
			this.fights.clear();
	}
	
	public synchronized void addContextActor(GameRolePlayActorInformations actor) {
		this.actors.add(actor);
	}
	
	public synchronized void addContextFight(FightCommonInformations fight) {
		this.fights.add(fight);
	}
	
	public synchronized void removeContextActor(double actorId) {
		Iterator<GameRolePlayActorInformations> it = this.actors.iterator();
		while(it.hasNext())
			if(it.next().contextualId == actorId)
				it.remove();
	}
	
	public synchronized Vector<Integer> getCellIdsTakenByMonsters() {
		Vector<Integer> cellIds = new Vector<Integer>();
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor instanceof GameRolePlayGroupMonsterInformations)
				cellIds.add(actor.disposition.cellId);		
		return cellIds;
	}
	
	public synchronized double getActorIdByName(String name) {
		for(GameRolePlayActorInformations actor : this.actors)
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
	
	public synchronized int getInteractiveSkillInstanceUid(int elementId) {
		for(InteractiveElement interactive : this.interactives)
			if(interactive.elementId == elementId)
				return interactive.enabledSkills[0].skillInstanceUid;
		return 0;
	}
	
	public synchronized GameRolePlayActorInformations getActorById(double id) {
		for(GameRolePlayActorInformations actor : this.actors)
			if(actor.contextualId == id)
				return actor;
		return null;
	}
	
	public synchronized Vector<GameRolePlayGroupMonsterInformations> getMonsterGroups() {
		Vector<GameRolePlayGroupMonsterInformations> monsters = new Vector<GameRolePlayGroupMonsterInformations>(this.actors.size());
		for(GameRolePlayActorInformations actor : this.actors)
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
	
	public synchronized boolean monsterGroupIsInFight(double monsterGroupId) {
		for(FightCommonInformations fight : this.fights)
			for(FightTeamInformations team : fight.fightTeams)
				if(team.leaderId == monsterGroupId)
					return true;
		return false;
	}
}