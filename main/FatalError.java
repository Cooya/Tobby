package main;

import gui.Controller;

public class FatalError extends Error {
	private static final long serialVersionUID = -293840118247954992L;
	
	public FatalError(String str) {
		new Exception(str).printStackTrace();
		Controller.restartInstance();
	}
	
	public FatalError(Exception e) {
		e.printStackTrace();
		Controller.restartInstance();
	}
}