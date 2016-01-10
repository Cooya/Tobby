package utilities;

import messages.Message;
import messages.MessageName;


public class Log {
	public static void p(String msgDirection, Message msg) {
		if(msgDirection == "r" || msgDirection == "reception") {
			System.out.println();
			System.out.println("Receiving message " + msg.getId() + " (" + MessageName.get(msg.getId()) + ")");
			if(msg.getLenOfSize() > 1)
				System.out.println("Length of size : " + msg.getLenOfSize() + " bytes");
			else
				System.out.println("Length of size : " + msg.getLenOfSize() + " byte");
			System.out.println("Size : " + msg.getSize() + " bytes");
		}
		else if(msgDirection == "s" || msgDirection == "sending") {
			System.out.println();
			System.out.println("Sending message " + msg.getId() + " (" + MessageName.get(msg.getId()) + ")");
			if(msg.getLenOfSize() > 1)
				System.out.println("Length of size : " + msg.getLenOfSize() + " bytes");
			else
				System.out.println("Length of size : " + msg.getLenOfSize() + " byte");
			System.out.println("Size : " + msg.getSize() + " bytes");
		}
	}
	
	public static void p(String str) {
		System.out.println();
		System.out.println(str);
	}
}
