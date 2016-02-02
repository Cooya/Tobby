package messages.fight;

import messages.Message;
import roleplay.fight.GameFightFighterInformations;
import utilities.ByteArray;

public class GameFightShowFighterMessage extends Message{
	public static final int Id = 5864;

	public GameFightFighterInformations informations;

	public GameFightShowFighterMessage(Message msg)
	{
		super(msg);
		this.informations = new GameFightFighterInformations();
	}

	public void deserialize() 
	{
		ByteArray buffer=new ByteArray(this.getContent());
		int loc2 = buffer.readShort();
		switch(loc2){
		case 29: 
			this.informations=new GameFightMonsterInformations();
			break;
		case 46:
			this.informations=new GameFightCharacterInformations();
			break;
		}
		this.informations.deserialize(buffer);
	}
}
