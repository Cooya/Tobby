package messages.synchronisation;

import messages.Message;

public class BasicNoOperationMessage extends Message {
	private static int counter = 0;
	
	public BasicNoOperationMessage(Message msg) {
		super(msg);
		counter++;
	}
	
	public static int getCounter() {
		return counter;
	}
}
