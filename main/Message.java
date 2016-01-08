package main;

public class Message {
	private short id;
	private short lenofsize;
	private int size;
	private byte[] content;
	private int bytesAvailables;
	private boolean complete;
	
	public Message(short id, int size, short lenofsize, byte[] content, int bytesAvailables) {
		assert bytesAvailables > size;
		this.id = id;
		this.lenofsize = lenofsize;
		this.size = size;
		this.content = content;
		this.bytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
		
		System.out.println("Message creation => id : " + id + " (" + MessageName.get(id) + "), lenofsize : " + lenofsize + ", size : " + size + ", complete : " + this.complete);
	}
	
	public short getId() {
		return this.id;
	}

	public short getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getTotalSize() {
		return this.bytesAvailables + 2 + this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	public boolean isComplete() {
		return this.complete;
	}
	
	public int appendContent(byte[] buffer) {
		int read = 0;
		for(; read < this.size - this.bytesAvailables; ++read)
			this.content[this.bytesAvailables + read] = buffer[read];
		this.bytesAvailables += read;
		this.complete = bytesAvailables == size;
		return read;
	}
}