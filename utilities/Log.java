package utilities;

import java.io.PrintWriter;

import messages.Message;

public class Log {
	private static final boolean DEBUG = false;
	private static String LOG_PATH = System.getProperty("user.dir") + "/Ressources/log.txt";
	private static String EOL = System.getProperty("line.separator");
	private static PrintWriter writer;
	
	static {
		try {
			writer = new PrintWriter(LOG_PATH, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void p(String msgDirection, Message msg) {
		String str = "";
		int id = msg.getId();
		String name = Message.get(id);
		int lenofsize = msg.getLenOfSize();
		int size = msg.getSize();
		if(msgDirection == "r" || msgDirection == "reception")
			str += "Receiving message " + id + " (" + name + ")" + EOL;
		else if(msgDirection == "s" || msgDirection == "sending")
			str += "Sending message " + id + " (" + name + ")" + EOL;
		if(lenofsize > 1)
			str += "Length of size : " + lenofsize + " bytes" + EOL;
		else
			str += "Length of size : " + lenofsize + " byte" + EOL;
		if(size > 1)
			str += "Size : " + size + " bytes" + EOL;
		else
			str += "Size : " + size + " byte" + EOL;
		if(name == null || DEBUG)
			System.out.println(str);
		writer.println(str);
		writer.flush();
	}
	
	public synchronized static void p(String str) {
		if(DEBUG)
			System.out.println(str + EOL);
		else
			System.out.println(str);
		writer.println(str + EOL);
		writer.flush();
	}
}