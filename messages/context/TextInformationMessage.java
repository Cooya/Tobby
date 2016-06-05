package messages.context;

import messages.NetworkMessage;

public class TextInformationMessage extends NetworkMessage {
	public int msgType = 0;
	public int msgId = 0;
	public String[] parameters;

	@Override
	public void serialize() {
		// not implemented yet
	}

	@Override
	public void deserialize() {
		this.msgType = this.content.readByte();
		this.msgId = this.content.readVarShort();
		int nb = this.content.readShort();
		this.parameters = new String[nb];
		for(int i = 0; i < nb; ++i)
			this.parameters[i] = this.content.readUTF();
	}
}