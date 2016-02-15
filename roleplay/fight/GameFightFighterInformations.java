package roleplay.fight;

import java.util.Vector;

import roleplay.ProtocolTypeManager;
import roleplay.currentmap.EntityDispositionInformations;
import roleplay.currentmap.EntityLook;
import roleplay.currentmap.GameContextActorInformations;
import utilities.ByteArray;

public class GameFightFighterInformations extends GameContextActorInformations{
	public int teamId = 2;

	public int wave = 0;

	public boolean alive = false;

	public GameFightMinimalStats stats;

	public Vector<Integer> previousPositions;

	public GameFightFighterInformations(ByteArray buffer) {

		super(buffer);
		int loc5 = 0;
		this.previousPositions = new Vector<Integer>();
		this.teamId = buffer.readByte();
		if(this.teamId < 0)
		{
			throw new Error("Forbidden value (" + this.teamId + ") on element of GameFightFighterInformations.teamId.");
		}
		this.wave = buffer.readByte();
		if(this.wave < 0)
		{
			throw new Error("Forbidden value (" + this.wave + ") on element of GameFightFighterInformations.wave.");
		}
		this.alive = buffer.readBoolean();
		int type = buffer.readShort();
		System.out.println("type= "+type);
		switch(type){
		case 31: 
			this.stats=new GameFightMinimalStats(buffer);
			break;
		case 360:
			this.stats=new GameFightMinimalStatsPreparation(buffer);
			break;
		}
		int loc3 = buffer.readShort();
		int loc4 = 0;
		while(loc4 < loc3)
		{
			loc5 = buffer.readVarShort();
			if(loc5 < 0 || loc5 > 559)
			{
				throw new Error("Forbidden value (" + loc5 + ") on elements of previousPositions.");
			}
			this.previousPositions.add(loc5);
			loc4++;
		}
	}

}
