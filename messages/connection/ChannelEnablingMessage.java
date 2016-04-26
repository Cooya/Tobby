package messages.connection;

import messages.Message;

public class ChannelEnablingMessage extends Message {
    public int channel = 0;
    public boolean enable = false;

    @Override
	public void serialize() {
		this.content.writeByte(this.channel);
		this.content.writeBoolean(this.enable);
	}
	
	@Override
	public void deserialize() {
		// not implemented yet
	}
}