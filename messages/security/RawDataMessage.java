package messages.security;

import java.io.FileOutputStream;

import main.Instance;
import messages.Message;
import utilities.ByteArray;

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
			Instance.log("SWF file created.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
