package roleplay.movement.ankama;

import utilities.ByteArray;

public class SoundElement extends BasicElement {
	public int soundId;
	public int minDelayBetweenLoops;
	public int maxDelayBetweenLoops;
	public int baseVolume;
	public int fullVolumeDistance;
	public int nullVolumeDistance;
	
	public SoundElement(Cell cell) {
		super(cell);
	}
	
	public int getElementType() {
		return SOUND;
	}

	public void fromRaw(ByteArray raw, int mapVersion) {
		this.soundId = raw.readInt();
		this.baseVolume = raw.readShort();
		this.fullVolumeDistance = raw.readInt();
		this.nullVolumeDistance = raw.readInt();
		this.minDelayBetweenLoops = raw.readShort();
		this.maxDelayBetweenLoops = raw.readShort();
	}
}
