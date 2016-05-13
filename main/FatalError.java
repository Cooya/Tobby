package main;


public class FatalError extends Error {
	private static final long serialVersionUID = -293840118247954992L;
	
	public FatalError(String msg) {
		super(msg);
		Controller.getInstance().deconnectCurrentCharacter(msg, true, true);
	}
	
	public FatalError(Exception e) {
		super(e);
		Controller.getInstance().deconnectCurrentCharacter(e.getMessage(), true, true);
	}
}