package messages;

import main.Log;

public class UnknownMessage extends Message {
	
	@Override
	public void serialize() {
		Log.err("Forbidden serialize method called.");
	}

	@Override
	public void deserialize() {
		Log.err("Forbidden deserialize method called.");
	}
}