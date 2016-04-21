package main;

import gui.CharacterFrame;

import java.io.PrintWriter;
import java.util.Date;

import messages.Message;

public class Log {
	private static final boolean DEBUG = false;
	private static final int WRITE_INTERVAL = 10;
	private static final String LOG_PATH = System.getProperty("user.dir") + "/Ressources/Logs/";
	private static final String EOL = System.getProperty("line.separator");
	private PrintWriter writer;
	private String writeBuffer;
	private int writeCounter;
	public CharacterFrame graphicalFrame;
	
	public Log(String characterName, CharacterFrame graphicalFrame) {
		try {
			writer = new PrintWriter(LOG_PATH + characterName + ".txt", "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.writeBuffer = "";
		this.writeCounter = 0;
		this.graphicalFrame = graphicalFrame;
	}
	
	public void p(String msgDirection, Message msg) { // pour la réception et l'envoi de messages
		if(Thread.currentThread().isInterrupted())
			return;
		
		String str = "";
		int id = msg.getId();
		String name = Message.get(id);
		//int lenofsize = msg.getLenOfSize();
		//int size = msg.getSize();
		if(msgDirection == "r" || msgDirection == "reception")
			str += "Receiving message " + id + " (" + name + ")";
		else if(msgDirection == "s" || msgDirection == "sending")
			str += "Sending message " + id + " (" + name + ")";
		/*
		if(lenofsize > 1)
			str += "Length of size : " + lenofsize + " bytes" + EOL;
		else
			str += "Length of size : " + lenofsize + " byte" + EOL;
		if(size > 1)
			str += "Size : " + size + " bytes" + EOL;
		else
			str += "Size : " + size + " byte" + EOL;
		*/
		if(name == null && this.graphicalFrame != null)
			this.graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str);
		writeIntoLogFile("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str + EOL);
	}
	
	public void p(String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		
		if(this.graphicalFrame != null) {
			if(DEBUG)
				graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str + EOL);
			else
				graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str);
		}
		writeIntoLogFile("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str + EOL);
	}
	
	public static void err(String msg) {
		syso("ERROR : " + msg);
	}
	
	public static void info(String msg) {
		syso("INFO : " + msg);
	}
	
	private synchronized static void syso(String msg) {
		System.out.println(msg);
	}
	
	public void flushBuffer() {
		this.writer.print(this.writeBuffer);
		this.writer.flush();
		this.writeBuffer = "";
		this.writeCounter = 0;
	}
	
	private void writeIntoLogFile(String msg) {
		this.writeBuffer += msg;
		if(++this.writeCounter == WRITE_INTERVAL)
			flushBuffer();
	}
}