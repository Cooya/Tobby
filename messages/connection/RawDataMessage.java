package messages.connection;

import java.io.FileOutputStream;

import messages.Message;
import utilities.ByteArray;
import utilities.Log;

public class RawDataMessage extends Message {
	
	public RawDataMessage(Message msg) {
		super(msg);
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
