package gamedata.connection;

import utilities.ByteArray;

public class VersionExtended extends Version {
	public int install = 0;
	public int technology = 0;

	public VersionExtended(int major, int minor, int release, int revision, int patch, int buildType, int install, int technology) {
		super(major, minor, release, revision, patch, buildType);
		this.install = install;
		this.technology = technology;
	}
	
	public void serialize(ByteArray buffer) {
		super.serialize(buffer);
		buffer.writeByte(this.install);
		buffer.writeByte(this.technology);
	}
}