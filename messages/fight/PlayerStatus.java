package messages.fight;

import utilities.ByteArray;

public class PlayerStatus {
     public int statusId = 1;
     
     public PlayerStatus()
     {
        super();
     }

     public void deserialize(ByteArray buffer)
     {
        this.statusId = buffer.readByte();
        if(this.statusId < 0)
        {
           throw new Error("Forbidden value (" + this.statusId + ") on element of PlayerStatus.statusId.");
        }
     }
}
