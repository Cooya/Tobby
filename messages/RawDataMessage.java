package messages;

import java.io.FileOutputStream;

import utilities.ByteArray;
import utilities.Log;

public class RawDataMessage extends Message {
	public static final int ID = 6253;
	
	public RawDataMessage(Message msg) {
		super(ID, msg.getLenOfSize(), msg.getSize(), msg.getContent());
	
		//createSWF();
	}
	
	@SuppressWarnings("unused")
	private void createSWF() {
		ByteArray buffer = new ByteArray(this.content);
		try {
			FileOutputStream fs = new FileOutputStream("./RDM.swf");
			fs.write(buffer.readBytes(buffer.readVarInt()));
			fs.close();
			Log.p("SWF file created.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
