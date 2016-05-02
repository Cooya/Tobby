package messages;

import main.Log;

public class UnhandledMessage extends Message {

	public UnhandledMessage(String msgName) {
		super(msgName);
	}
	
	@Override
	public void serialize() {
		// message vide, rien à sérialiser
	}

	@Override
	public void deserialize() {
		Log.err("Forbidden deserialize method called (" + getName() + ").");
	}
}