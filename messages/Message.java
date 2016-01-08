package messages;

public abstract class Message {
	protected short id;
	protected short lenofsize;
	protected int size;
	protected byte[] content;
	
	public Message(short id, short lenofsize, int size, byte[] content) {
		this.id = id;
		this.lenofsize = lenofsize;
		this.size = size;
		this.content = content;
	}
	
	public short getId() {
		return this.id;
	}

	public short getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	static short getLenOfSize(int size) {
	    if(size > 65535)
	        return 3;
	    else if(size > 255)
	        return 2;
	    else if(size > 0)
	        return 1;
	    else
	        return 0;
	}
}