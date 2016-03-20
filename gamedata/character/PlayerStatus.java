package gamedata.character;

import utilities.ByteArray;

public class PlayerStatus {
     public int statusId = 1;
     
     public PlayerStatus(ByteArray buffer) {
    	 this.statusId = buffer.readByte();
     }
}