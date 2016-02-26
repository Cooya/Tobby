package main;

public class FatalError extends Error {
	private static final long serialVersionUID = -293840118247954992L;
	
	public FatalError(String str) {
		new Exception(str).printStackTrace();
		Instance.killCurrentInstance();
	}
	
	public FatalError(Exception e) {
		e.printStackTrace();
		Instance.killCurrentInstance();
	}
}