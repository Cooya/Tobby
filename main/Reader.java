package main;

public class Reader {
	private static Message incompleteMsg; // message incomplet qui attend d'être complété
	
	public static void processBuffer(byte[] buffer, int bytesReceived) {
		int read = 0;
		if(incompleteMsg != null) {
			read += incompleteMsg.appendContent(buffer);
			if(incompleteMsg.isComplete()) {
				Manager.processMessage(incompleteMsg);
				incompleteMsg = null;
				buffer = shiftArray(buffer, read);
			}
			else
				return; // si le message est incomplet, cela signifie qu'il n'y a plus rien à lire dans le buffer
		}
		while(read < bytesReceived) {
			Message msg = extractMsgFromBuffer(buffer);
			if(msg.isComplete())
				Manager.processMessage(msg);
			else
				incompleteMsg = msg;
			read += msg.getTotalSize();
			buffer = shiftArray(buffer, read);
		}
	}
	
	private static Message extractMsgFromBuffer(byte[] buffer) {	
		short header = (short) (buffer[0] << 8 | buffer[1]);
		int id = header >> 2;
		int lenofsize = header & 3;
		int size;
		if(lenofsize == 0)
	        size = 0;
	    else if(lenofsize  == 1)
	        size = buffer[2];
	    else if(lenofsize == 2)
	        size = (buffer[2] << 8 | buffer[3]);
	    else // lenofsize = 3
	        size = ((buffer[2] << 16 | buffer[3] << 8) | buffer[4]);
		byte[] content = new byte[size];
		int counter = 0;
		for(int i = 2 + lenofsize; i < size + 2 + lenofsize; ++i, ++counter)
			content[counter] = buffer[i];
	    return new Message(id, size, lenofsize, content, counter);		
	}
	
	private static byte[] shiftArray(byte[] array, int from) { // suppression des octets traités dans le buffer
		byte[] newArray = new byte[array.length - from];
		for(int i = 0; i < array.length - from; ++i)
			newArray[i] = array[i + from];
		return newArray;
	}
}
