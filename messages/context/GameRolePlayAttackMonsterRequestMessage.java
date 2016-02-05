package messages.context;

import messages.Message;
import utilities.ByteArray;

public class GameRolePlayAttackMonsterRequestMessage extends Message {
     public double monsterGroupId = 0;
     
     public GameRolePlayAttackMonsterRequestMessage() {
        super();
     }
     
     public void serialize(double monsterGroupId) {
		this.monsterGroupId = monsterGroupId;
		
		ByteArray buffer = new ByteArray();
        buffer.writeDouble(this.monsterGroupId);
        completeInfos(buffer);
     }
}