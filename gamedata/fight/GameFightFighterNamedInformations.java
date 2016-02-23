package gamedata.fight;

import gamedata.character.PlayerStatus;
import utilities.ByteArray;

public class GameFightFighterNamedInformations extends GameFightFighterInformations{

	public String name = "";

	public PlayerStatus status;

	public GameFightFighterNamedInformations(ByteArray buffer)
	{
		super(buffer);
		this.name = buffer.readUTF();
		this.status = new PlayerStatus();
		this.status.deserialize(buffer);
	}


}
