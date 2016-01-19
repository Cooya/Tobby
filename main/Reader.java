package main;

import java.util.LinkedList;

import messages.Message;
import utilities.ByteArray;

public class Reader {
	private static Message incompleteMsg; // message incomplet qui attend d'être complété
	
	public static LinkedList<Message> processBuffer(ByteArray buffer) {
		LinkedList<Message> msgStack = new LinkedList<Message>();
		
		if(incompleteMsg != null) {
			buffer.readBytes(incompleteMsg.appendContent(buffer.bytes()));
			if(incompleteMsg.isComplete()) {
				msgStack.add(incompleteMsg);
				incompleteMsg = null;
			}
			else
				return msgStack; // si le message est incomplet, cela signifie qu'il n'y a plus rien à lire dans le buffer
		}
		while(!buffer.endOfArray()) {
			Message msg = extractMsgFromBuffer(buffer.bytesFromPos());
			if(msg.isComplete())
				msgStack.add(msg);
			else
				incompleteMsg = msg;
			buffer.readBytes(msg.getTotalSize());
		}
		return msgStack;
	}
	
	private static Message extractMsgFromBuffer(byte[] buffer) {
		char[] cbuffer = new char[buffer.length];
		for(int i = 0; i < buffer.length; ++i)
			cbuffer[i] = (char) (buffer[i] & 0xFF);
		
		int header = cbuffer[0] << 8 | cbuffer[1];
		short id = (short) (header >> 2);
		short lenofsize = (short) (header & 3);
		int size;
		if(lenofsize == 0)
	        size = 0;
	    else if(lenofsize == 1)
	        size = cbuffer[2];
	    else if(lenofsize == 2)
	        size = cbuffer[2] << 8 | cbuffer[3];
	    else // lenofsize = 3
	        size = (cbuffer[2] << 16 | cbuffer[3] << 8) | cbuffer[4];	
		int bytesAvailable = size > buffer.length - 2 - lenofsize ? buffer.length - 2 - lenofsize : size;
		byte[] content = new byte[bytesAvailable];
		int counter = 0;
		for(int i = 2 + lenofsize; i < bytesAvailable + 2 + lenofsize; ++i, ++counter)
			content[counter] = buffer[i];
	    return new Message(id, lenofsize, size, content, counter);		
	}
}
