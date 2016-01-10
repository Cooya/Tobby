package main;

import java.io.OutputStream;

import utilities.ByteArray;
import utilities.Log;
import messages.Message;

public class Sender implements Runnable {
	private static Sender sender = null;
	private OutputStream outputStream;
	private byte[] toSend;
	
	private Sender(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public static void create(OutputStream outputStream) {			
		sender = new Sender(outputStream);
	}

	public static Sender getInstance() {
		return sender;
	}
	
	public void send(Message msg) {
		int id = msg.getId();
		short lenofsize = msg.getLenOfSize();
		int size = msg.getSize();
		byte[] content = msg.getContent();
		assert(lenofsize == 0 || size == 0 || content == null);
		
		byte[] header = makeHeader(id, lenofsize, size);
		toSend = new byte[header.length + size];
		for(int i = 0; i < header.length; ++i)
			toSend[i] = header[i];
		for(int i = 0; i < size; ++i)
			toSend[i + header.length] = content[i];
		Log.p("s", msg);
		run();
	}
	
	static byte[] makeHeader(int id, short lenofsize, int size) {
		short header =  (short) (id << 2 | lenofsize);
		ByteArray array = new ByteArray();
		array.writeShort(header);
		switch(lenofsize) {
			case 0 : return null; // impossible
			case 1 : array.writeByte((byte) size); break;
			case 2 : array.writeShort((short) size); break;
			case 3 : array.writeByte((byte) (size >> 16 & 255)); array.writeShort((short) (size & 65535)); break;
		}
		return array.bytes();
	}
	
	public void run() {
		try {
			outputStream.write(this.toSend);
			outputStream.flush();
			this.toSend = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
