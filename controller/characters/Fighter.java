package controller.characters;

import main.Log;
import controller.CharacterState;
import controller.informations.FightContext;

public abstract class Fighter extends Character {
	public FightContext fightContext;
	private Mule mule;
	
	public Fighter(int id, String login, String password, int serverId, int breed, Log log) {
		super(id, login, password, serverId, breed, log);
		this.fightContext = new FightContext(this);
	}
	
	public Mule getMule() {
		return this.mule;
	}
	
	public void setMule(Mule mule) {
		updateState(CharacterState.MULE_ONLINE, mule != null);
		this.mule = mule;
	}
}