package main;

import gui.Controller;

public class FatalError extends Error {
	private static final long serialVersionUID = -293840118247954992L;
	
	public FatalError(String str) {
		super(str);
		Controller.getInstance().deconnectCurrentInstance(str, true, true);
	}
	
	public FatalError(Exception e) {
		super(e);
		Controller.getInstance().deconnectCurrentInstance(e.getMessage(), true, true);
	}
}