package messages;

import utilities.Log;

public class IdentificationFailedMessage extends Message {
	public IdentificationFailedMessage(byte[] content) {
		super(20, (short) 0, 0, null);
		deserialize(content);
	}

	private void deserialize(byte[] content) {
		byte reason = content[0];
		Log.p("Authentification failed for reason " + reason);
	}
}
