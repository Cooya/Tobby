package main;

import java.util.LinkedList;

import messages.Message;
import utilities.ByteArray;

public class Reader {
	private Message incompleteMsg; // message incomplet qui attend d'être complété
	private byte[] incompleteHeader; // header incomplet qui attend d'être complété
	
	public LinkedList<Message> processBuffer(ByteArray buffer) {
		LinkedList<Message> msgStack = new LinkedList<Message>();
		
		if(this.incompleteHeader != null) {
			buffer.appendBefore(this.incompleteHeader);
			this.incompleteHeader = null;
		}
		else if(this.incompleteMsg != null) {
			buffer.readBytes(this.incompleteMsg.appendContent(buffer.bytes()));
			if(this.incompleteMsg.isComplete()) {
				msgStack.add(this.incompleteMsg);
				this.incompleteMsg = null;
			}
			else
				return msgStack; // si le message est incomplet, cela signifie qu'il n'y a plus rien à lire dans le buffer
		}
		while(!buffer.endOfArray()) {
			Message msg = extractMsgFromBuffer(buffer.bytesFromPos());
			if(msg == null) { // header incomplet
				this.incompleteHeader = buffer.bytesFromPos();
				break;
			}
			else if(msg.isComplete()) // message complet
				msgStack.add(msg);
			else // message incomplet
				this.incompleteMsg = msg;
			buffer.readBytes(msg.getTotalSize());
		}
		return msgStack;
	}
	
	private static Message extractMsgFromBuffer(byte[] buffer) {
		if(buffer.length < 2)
			return null;
		char[] cbuffer = new char[buffer.length]; // étant donné que ce sont des octets signés bruts
		for(int i = 0; i < buffer.length; ++i)
			cbuffer[i] = (char) (buffer[i] & 0xFF);
		
		int header = cbuffer[0] << 8 | cbuffer[1];
		short id = (short) (header >> 2);
		short lenofsize = (short) (header & 3);
		if(buffer.length < 2 + lenofsize)
			return null;
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
