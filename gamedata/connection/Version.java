package gamedata.connection;

import utilities.ByteArray;

public class Version {
	public int major = 0;
	public int minor = 0;
	public int release = 0;
	public int revision = 0;
	public int patch = 0;
	public int buildType = 0;
	
	public Version(int major, int minor, int release, int revision, int patch, int buildType) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.revision = revision;
		this.patch = patch;
		this.buildType = buildType;
	}
	
	public void serialize(ByteArray buffer) {
		buffer.writeByte(this.major);
		buffer.writeByte(this.minor);
		buffer.writeByte(this.release);
		buffer.writeInt(this.revision);
		buffer.writeByte(this.patch);
		buffer.writeByte(this.buildType);
	}
}