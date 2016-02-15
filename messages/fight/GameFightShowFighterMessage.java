package messages.fight;

import messages.Message;
import roleplay.fight.GameFightCharacterInformations;
import roleplay.fight.GameFightFighterInformations;
import roleplay.fight.GameFightMonsterInformations;
import utilities.ByteArray;

public class GameFightShowFighterMessage extends Message{
	public static final int Id = 5864;

	public GameFightFighterInformations informations;

	public GameFightShowFighterMessage(Message msg)
	{
		super(msg);
	}

	public void deserialize() 
	{
		ByteArray buffer=new ByteArray(this.getContent());
		int loc2 = buffer.readShort();
		switch(loc2){
		case 29: 
			this.informations=new GameFightMonsterInformations(buffer);
			break;
		case 46:
			this.informations=new GameFightCharacterInformations(buffer);
			break;
		}
	}
}
