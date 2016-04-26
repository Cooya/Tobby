package messages;

import utilities.ByteArray;

public interface IMessage {
	public void deserialize(ByteArray buffer);
}