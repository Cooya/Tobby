package messages;


public class ReceivedMessage extends Message {
	private int bytesAvailables;
	private boolean complete;

	public ReceivedMessage(short id, short lenofsize, int size, byte[] content, int bytesAvailables) {
		super(id, lenofsize, size, content);
		assert bytesAvailables > size;
		this.bytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
		
		System.out.println();
		System.out.println("Receiving message " + id + " (" + MessageName.get(id) + ")");
		if(lenofsize > 1)
			System.out.println("Length of size : " + lenofsize + " bytes");
		else
			System.out.println("Length of size : " + lenofsize + " byte");
		System.out.println("Size : " + size + " bytes");
		System.out.println("Complete : " + complete);
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
