package messages.fight;

import game.fight.GameFightCharacterInformations;
import game.fight.GameFightFighterInformations;
import game.fight.GameFightMonsterInformations;

import java.util.Vector;

import messages.Message;
import utilities.ByteArray;

public class GameFightSynchronizeMessage extends Message{
	public static final int Id = 5921;

	public Vector<GameFightFighterInformations> fighters;

	public GameFightSynchronizeMessage(Message msg)
	{
		super(msg);
		this.fighters = new Vector<GameFightFighterInformations>();

	}


	public void deserialize()
	{
		ByteArray buffer = new ByteArray(this.content);
		int loc4 = 0;
		GameFightFighterInformations loc5 = null;
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc4 = buffer.readShort();
			switch(loc4){
			case 29: 
				loc5=new GameFightMonsterInformations(buffer);
				break;
			case 46:
				loc5=new GameFightCharacterInformations(buffer);
				break;
			}
			
			this.fighters.add(loc5);
			loc3++;
		}
	}
}
