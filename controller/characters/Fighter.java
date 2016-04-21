package controller.characters;

import controller.CharacterState;
import controller.informations.FightContext;
import main.Instance;

public abstract class Fighter extends Character {
	public FightContext fightContext;
	private Mule mule;
	
	public Fighter(Instance instance, String login, String password, int serverId, int breed) {
		super(instance, login, password, serverId, breed);
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