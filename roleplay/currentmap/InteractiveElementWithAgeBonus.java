package roleplay.currentmap;

import utilities.ByteArray;

public class InteractiveElementWithAgeBonus extends InteractiveElement {
	public int ageBonus = 0;
	
	public InteractiveElementWithAgeBonus(ByteArray buffer) {
		super(buffer);
		this.ageBonus = buffer.readShort();
	}
}
