package messages.fight;

import main.Emulation;
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

	public void serialize(int spell, short cell, int instanceId)
	{
		ByteArray buffer=new ByteArray();
		buffer.writeVarShort(spell);
		buffer.writeShort(cell);
		completeInfos(Emulation.hashMessage(buffer, instanceId));
	}

}
