package messages.roleplay;

import messages.Message;
import utilities.ByteArray;

public class GameRolePlayAttackMonsterRequestMessage extends Message{
	 public static final int Id = 6191;

     public double monsterGroupId = 0;
     
     public GameRolePlayAttackMonsterRequestMessage()
     {
        super();
     }
     
public void serialize(double id)
     {
		this.monsterGroupId=id;
		ByteArray buffer=new ByteArray();
        if(this.monsterGroupId < -9.007199254740992E15 || this.monsterGroupId > 9.007199254740992E15)
        {
           throw new Error("Forbidden value (" + this.monsterGroupId + ") on element monsterGroupId.");
        }
        buffer.writeDouble(this.monsterGroupId);
        completeInfos(buffer);
     }
     

}
