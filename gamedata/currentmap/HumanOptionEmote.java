package gamedata.currentmap;

import utilities.ByteArray;

public class HumanOptionEmote extends HumanOption {
    public int emoteId = 0;
    public double emoteStartTime = 0;
	
	public HumanOptionEmote(ByteArray buffer) {
		super(buffer);
		this.emoteId = buffer.readByte();
		this.emoteStartTime = buffer.readDouble();
	}
}