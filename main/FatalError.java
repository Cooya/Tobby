package main;

import controller.characters.Character;

public class FatalError extends Error {
	private static final long serialVersionUID = -293840118247954992L;
	
	public FatalError(String msg) {
		super(msg);
		deconnectCurrentCharacter(msg);
	}
	
	public FatalError(Exception e) {
		super(e);
		deconnectCurrentCharacter(e.getMessage());
	}
	
	private void deconnectCurrentCharacter(String msg) {
		Character character = CharactersManager.getInstance().getCurrentCharacter();
		CharactersManager.getInstance().deconnectCharacter(character, msg, true, true);
		character.threadTerminated();
	}
}