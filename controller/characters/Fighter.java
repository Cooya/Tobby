package controller.characters;

import controller.informations.FightContext;
import main.Instance;

public abstract class Fighter extends Character {
	public FightContext fightContext;
	public Mule mule;
	
	public Fighter(Instance instance, String login, String password, int serverId, int breed) {
		super(instance, login, password, serverId, breed);
		this.fightContext = new FightContext(this);
	}
}