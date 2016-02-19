package game.character;

import utilities.ByteArray;

public class PlayerStatus {
     public int statusId = 1;

     public void serialize(ByteArray buffer, int statusId) {
    	 this.statusId = statusId;
    	 buffer.writeByte((byte) statusId);
     }

     public void deserialize(ByteArray buffer) {
        this.statusId = buffer.readByte();
     }
}