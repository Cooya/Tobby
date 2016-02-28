package messages.fight;

import gamedata.fight.GameFightCharacterInformations;
import gamedata.fight.GameFightFighterInformations;
import gamedata.fight.GameFightMonsterInformations;
import messages.Message;
import utilities.ByteArray;

public class GameFightShowFighterMessage extends Message{

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
