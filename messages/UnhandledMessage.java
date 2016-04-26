package messages;

import main.Log;

public class UnhandledMessage extends Message {
	
	public UnhandledMessage() {
		
	}

	public UnhandledMessage(String msgName) {
		super(msgName);
	}
	
	@Override
	public void serialize() {
		Log.err("Forbidden serialize method called.");
	}

	@Override
	public void deserialize() {
		Log.err("Forbidden deserialize method called.");
	}
}