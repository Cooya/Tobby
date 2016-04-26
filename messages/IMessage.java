package messages;

import utilities.ByteArray;

public interface IMessage { // TODO
	public void deserialize(ByteArray buffer);
}