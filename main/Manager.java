package main;
import messages.IdentificationMessage;
import messages.ReceivedMessage;

public class Manager {
	public static void processMessage(ReceivedMessage msg) {
		switch(msg.getId()) {
			case 3 : Sender.getInstance().send(new IdentificationMessage(msg.getContent())); break;
			default : return;
		}
	}
}
