package messages;


public class ReceivedMessage extends Message {
	private int bytesAvailables;
	private boolean complete;

	public ReceivedMessage(short id, short lenofsize, int size, byte[] content, int bytesAvailables) {
		super(id, lenofsize, size, content);
		assert bytesAvailables > size;
		this.bytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
		
		System.out.println("Received message => id : " + id + " (" + MessageName.get(id) + "), lenofsize : " + lenofsize + ", size : " + size + ", complete : " + this.complete);
	}
	
	public boolean isComplete() {
		return this.complete;
	}
	
	public int getTotalSize() {
		return this.bytesAvailables + 2 + this.lenofsize;
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
