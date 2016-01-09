package main;

import java.io.OutputStream;

import messages.Message;
import messages.MessageName;

public class Sender implements Runnable {
	private static Sender sender = null;
	private OutputStream outputStream;
	private Message currentMsg = null;
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
		this.currentMsg = msg;
		
		byte[] header = makeHeader(id, lenofsize, size);
		toSend = new byte[header.length + size];
		for(int i = 0; i < header.length; ++i)
			toSend[i] = header[i];
		for(int i = 0; i < size; ++i)
			toSend[i + header.length] = content[i];
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
			System.out.println();
			System.out.println("Sending message " + currentMsg.getId() + " (" + MessageName.get(currentMsg.getId()) + ")");
			if(currentMsg.getLenOfSize() > 1)
				System.out.println("Length of size : " + currentMsg.getLenOfSize() + " bytes");
			else
				System.out.println("Length of size : " + currentMsg.getLenOfSize() + " byte");
			System.out.println("Size : " + currentMsg.getSize() + " bytes");
			outputStream.write(this.toSend);
			outputStream.flush();
			this.toSend = null;
			this.currentMsg = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
