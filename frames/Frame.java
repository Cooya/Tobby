package frames;

import controller.characters.Character;
import main.Instance;

public abstract class Frame {
	protected Instance instance;
	protected Character character;
	
	public Frame(Instance instance, Character character) {
		this.instance = instance;
		this.character = character;
	}
}