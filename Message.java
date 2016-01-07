public class Message {
	private int id;
	private int size;
	private int lenofsize;
	private byte[] content;
	private int bytesAvailables;
	private boolean complete;
	
	public Message(int id, int size, int lenofsize, byte[] content, int bytesAvailables) {
		assert bytesAvailables > size;
		this.id = id;
		this.size = size;
		this.lenofsize = lenofsize;
		this.content = content;
		this.bytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getTotalSize() {
		return this.bytesAvailables + 2 + this.lenofsize;
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

