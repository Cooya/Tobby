
public class Message {
	private int id;
	private int size;
	private int lenofsize;
	private byte[] content;
	private boolean complete;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getLenofsize() {
		return lenofsize;
	}
	public void setLenofsize(int lenofsize) {
		this.lenofsize = lenofsize;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public void Init_Content(){
		content= new byte[size];
		complete=false;
	}
	
	public boolean deparseBuffer(byte[] buffer, int buffer_size){
		short header =(short) (buffer[0] <<8 | buffer[1]);
		this.id= header >> 2;
		lenofsize=header &3;
		if(lenofsize == 0)
	        size = 0;
	    else if(lenofsize  == 1)
	        size = buffer[2];
	    else if(lenofsize == 2)
	        size = (buffer[2] << 8 | buffer[3]);
	    else //P.lenofsize = 3
	        size = ((buffer[2]<<16|buffer[3]<<8)|buffer[4]);
	    if (size>(buffer_size-lenofsize-2))
	    {
	        complete=false;
	        return false;
	    }
	    complete=true;
	    return true;		
	}
	
	
	
	
}

