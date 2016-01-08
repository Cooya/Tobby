package main;
import messages.IdentificationMessage;
import messages.ReceivedMessage;

public class Manager {
	public static void processMessage(ReceivedMessage msg) {
		switch(msg.getId()) {
			case 3 : new IdentificationMessage(msg.getContent()); Sender.getInstance().send(msg); break;
			default : return;
		}
	}
}
