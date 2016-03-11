package gamedata.context;

import java.util.Vector;

import utilities.ByteArray;

public class GameRolePlayNpcQuestFlag {
    public Vector<Integer> questsToValidId;
    public Vector<Integer> questsToStartId;
    
    public GameRolePlayNpcQuestFlag(ByteArray buffer) {
    	questsToValidId = new Vector<Integer>();
    	questsToStartId = new Vector<Integer>();
    	
    	int nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.questsToValidId.add(buffer.readVarShort());
    	nb = buffer.readShort();
    	for(int i = 0; i < nb; ++i)
    		this.questsToStartId.add(buffer.readVarShort());
    }
}
