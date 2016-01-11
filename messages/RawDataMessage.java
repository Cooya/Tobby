package messages;

import java.io.FileOutputStream;
import utilities.ByteArray;
import utilities.Log;

public class RawDataMessage extends Message {
	private byte[] data;
	
	public RawDataMessage(Message msg) {
		super(6253, msg.getLenOfSize(), msg.getSize(), msg.getContent());
		
		deserialize();
		createSWF();
	}
	
	public void deserialize() {
		ByteArray buffer = new ByteArray(content);
		this.data = buffer.readBytes(buffer.readVarInt());
	}
	
	public void createSWF() {
		try {
			FileOutputStream fs = new FileOutputStream("./RDM.swf");
			fs.write(data);
			fs.close();
			Log.p("SWF file created.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
