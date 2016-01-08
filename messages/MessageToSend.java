package messages;

import main.Message;

public abstract class MessageToSend {
	private Sender sender;
	
	public MessageToSend(){
		sender.getInstance();
	}
	
}
