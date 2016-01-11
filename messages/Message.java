package messages;

public abstract class Message {
	protected int id;
	protected int lenofsize;
	protected int size;
	protected byte[] content;
	
	public Message(int id, int lenofsize, int size, byte[] content) {
		this.id = id;
		this.lenofsize = lenofsize;
		this.size = size;
		if(content != null) {
			if(content.length == size)		
				this.content = content;
			else {
				this.content = new byte[size];
				for(int i = 0; i < content.length; ++i)
					this.content[i] = content[i];
			}
		}
	}
	
	public int getId() {
		return this.id;
	}

	public int getLenOfSize() {
		return this.lenofsize;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	static short computeLenOfSize(int size) {
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