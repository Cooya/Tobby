package frames;

import controller.characters.Character;

public abstract class Frame {
	protected Character character;
	
	public Frame(Character character) {
		this.character = character;
	}
}