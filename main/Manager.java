package main;
import messages.IdentificationMessage;

public class Manager {

	public static void processMessage(Message msg) {
		switch(msg.getId()) {
			case 3 : new IdentificationMessage(msg);
		}
	}
}
