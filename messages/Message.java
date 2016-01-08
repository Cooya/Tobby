package messages;

import java.nio.ByteBuffer;

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
	
	static byte[] writeHeader(Message msg) {
		short lenofsize = msg.getLenOfSize();
		int size = msg.getSize();
		short header =  (short) (msg.getId() << 2 | lenofsize);
		ByteBuffer buffer = ByteBuffer.wrap(null);
		buffer.putShort(header);
		switch(lenofsize) {
			case 0 : break; // impossible
			case 1 : buffer.put((byte) size); break;
			case 2 : buffer.putShort((short) size); break;
			case 3 : buffer.put((byte) (size >> 16 & 255)); buffer.putShort((short) (size & 65535)); break;
		}
		return buffer.array();
	}
}