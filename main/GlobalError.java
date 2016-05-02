package main;

public class GlobalError extends Error {
	private static final long serialVersionUID = -1146726044276055362L;
	
	public GlobalError(String msg, boolean reconnection) {
		super(msg);
		//Controller.getInstance().deconnectAllCharacters(msg, true, reconnection); // TODO
	}
}