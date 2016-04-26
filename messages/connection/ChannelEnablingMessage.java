package messages.connection;

import utilities.ByteArray;
import messages.Message;

public class ChannelEnablingMessage extends Message {
    public int channel = 7;
    public boolean enable = false;

	public ChannelEnablingMessage() {
		super();
	}

	public void serialize() {
		ByteArray buffer = new ByteArray();
		buffer.writeByte(this.channel);
		buffer.writeBoolean(this.enable);
		
		super.completeInfos(buffer);
	}
}