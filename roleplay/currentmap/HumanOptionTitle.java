package roleplay.currentmap;

import utilities.ByteArray;

public class HumanOptionTitle extends HumanOption {
    public int titleId = 0;
    public String titleParam = "";

	public HumanOptionTitle(ByteArray buffer) {
		super(buffer);
		this.titleId = buffer.readVarShort();
		this.titleParam = buffer.readUTF();
	}
}