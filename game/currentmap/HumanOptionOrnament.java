package game.currentmap;

import utilities.ByteArray;

public class HumanOptionOrnament extends HumanOption {
	 public int ornamentId = 0;
	 
	 public HumanOptionOrnament(ByteArray buffer) {
		 super(buffer);
		 this.ornamentId = buffer.readVarShort();
	 }
}
