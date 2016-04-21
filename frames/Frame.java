package frames;

import controller.characters.Character;
import main.Instance;
import messages.Message;

public abstract class Frame {
	public boolean isActive; // TODO à supprimer
	protected Instance instance;
	protected Character character;
	
	public Frame(Instance instance, Character character) {
		this.isActive = false;
		this.instance = instance;
		this.character = character;
	}
	
	public abstract boolean processMessage(Message msg); // TODO
}