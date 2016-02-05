package main;

import java.util.Vector;

import roleplay.currentmap.GameRolePlayActorInformations;

public class RoleplayContext {
	private Vector<GameRolePlayActorInformations> actors;
	
	public RoleplayContext() {
		
	}
	
	public void setContextActors(Vector<GameRolePlayActorInformations> actors) {
		this.actors = actors;
	}
	
	public void updateActorPosition(double actorId, int position) {
		for(GameRolePlayActorInformations actor : actors)
			if(actor.contextualId == actorId)
				actor.disposition.cellId = position;
	}
}
