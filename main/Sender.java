package main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Sender implements Runnable {
	private static Sender sender = null;
	private Socket socket;
	private byte[] toSend;
	
	private Sender(Socket socket) {
		this.socket = socket;
	}
	
	public static void create(Socket socket) {			
		sender = new Sender(socket);
	}

	public static Sender getInstance() {
		return sender;
	}
	
	public void send(Message msg) {
		byte[] header = makeHeader(msg);
		toSend = new byte[header.length + msg.getSize()];
		for(int i = 0; i < header.length; ++i)
			toSend[i] = header[i];
		byte[] content = msg.getContent();
		for(int i = header.length; i < toSend.length; ++i)
			toSend[i] = content[i];
		run();
	}
	
	public void run() {
		try {
			OutputStream os = this.socket.getOutputStream();
			os.write(this.toSend);
			os.flush();
			this.toSend = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static byte[] makeHeader(Message msg) {
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
