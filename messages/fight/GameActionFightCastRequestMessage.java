package messages.fight;

import messages.Message;
import utilities.ByteArray;

public class GameActionFightCastRequestMessage extends Message{

	public static final int Id = 1005;

	public int spellId = 0;

	public short cellId = 0;

	public GameActionFightCastRequestMessage()
	{
		super();
	}

	public void serialize(int spell,short cell)
	{
		ByteArray buffer=new ByteArray();
		
		if(this.spellId < 0)
		{
			throw new Error("Forbidden value (" + this.spellId + ") on element spellId.");
		}
		buffer.writeVarShort(spell);
		if(this.cellId < -1 || this.cellId > 559)
		{
			throw new Error("Forbidden value (" + this.cellId + ") on element cellId.");
		}
		buffer.writeShort(cell);
		completeInfos(buffer);
	}

}
