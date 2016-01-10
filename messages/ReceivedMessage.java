package messages;


public class ReceivedMessage extends Message {
	private int contentBytesAvailables; // nombre d'octets du contenu acquis
	private boolean complete;

	public ReceivedMessage(short id, short lenofsize, int size, byte[] content, int bytesAvailables) {
		super(id, lenofsize, size, content);
		assert bytesAvailables > size;
		this.contentBytesAvailables = bytesAvailables;
		this.complete = bytesAvailables == size;
	}
	
	public boolean isComplete() {
		return this.complete;
	}
	
	public int getTotalSize() {
		return this.contentBytesAvailables + 2 + this.lenofsize;
	}
	
	public int appendContent(byte[] buffer) {
		int additionSize;
		if(buffer.length > this.size - this.contentBytesAvailables)
			additionSize = this.size - this.contentBytesAvailables;
		else
			additionSize = buffer.length;
		for(int read = 0; read < additionSize; ++read)
			this.content[this.contentBytesAvailables + read] = buffer[read];
		this.contentBytesAvailables += additionSize;
		this.complete = contentBytesAvailables == size;
		return additionSize;
	}

}
