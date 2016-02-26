package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class SequenceEndMessage extends Message{
	public static final int Id = 956;

	public int actionId = 0;

	public double authorId = 0;

	public int sequenceType = 0;

	public SequenceEndMessage(Message msg)
	{
		super(msg);
	}

	public void deserialize()
	{
		ByteArray buffer=new ByteArray(this.getContent());
		this.actionId = buffer.readVarShort();
		this.authorId = buffer.readDouble();
		this.sequenceType = buffer.readByte();
	}
}
