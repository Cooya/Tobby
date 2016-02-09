package utilities;

import messages.Message;

public class Log {
	private static final boolean DEBUG = false;
	
	public synchronized static void p(String msgDirection, Message msg) {
		int id = msg.getId();
		String name = Message.get(id);
		if(name == null || DEBUG) {
			int lenofsize = msg.getLenOfSize();
			int size = msg.getSize();
			if(msgDirection == "r" || msgDirection == "reception")
				System.out.println("Receiving message " + id + " (" + name + ")");
			else if(msgDirection == "s" || msgDirection == "sending")
				System.out.println("Sending message " + id + " (" + name + ")");
			if(lenofsize > 1)
				System.out.println("Length of size : " + lenofsize + " bytes");
			else
				System.out.println("Length of size : " + lenofsize + " byte");
			if(size > 1)
				System.out.println("Size : " + size + " bytes\n");
			else
				System.out.println("Size : " + size + " byte\n");
		}
	}
	
	public synchronized static void p(String str) {
		if(DEBUG)
			System.out.println(str + '\n');
		else
			System.out.println(str);
	}
}
