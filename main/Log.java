package main;

import gui.CharacterFrame;

import java.awt.Color;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import messages.Message;

public class Log {
	private static final boolean DEBUG = false;
	private static String LOG_PATH = System.getProperty("user.dir") + "/Ressources/";
	private static String EOL = System.getProperty("line.separator");
    private static SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss:SSS");
	private PrintWriter writer;
	public CharacterFrame graphicalFrame;
	
	public Log(String characterName, CharacterFrame graphicalFrame) {
		try {
			writer = new PrintWriter(LOG_PATH + characterName + "_log.txt", "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.graphicalFrame = graphicalFrame;
	}
	
	public void p(String msgDirection, Message msg) {
		if(Thread.currentThread().isInterrupted())
			return;
		
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
			graphicalFrame.appendText("[" + date.format(new Date()) + "] " + str, Color.BLACK);
		writer.println("[" + date.format(new Date()) + "] " + str);
		writer.flush();
	}
	
	public void p(String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		
		if(DEBUG)
			graphicalFrame.appendText("[" + date.format(new Date()) + "] " + str + EOL, Color.BLACK);
		else
			graphicalFrame.appendText("[" + date.format(new Date()) + "] " + str, Color.BLACK);
		writer.println("[" + date.format(new Date()) + "] " + str + EOL);
		writer.flush();
	}
	
	public void p(Status status, String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		
		/*
		switch(status) {
			case INFO : break;
			case WARNING : str = ANSI_YELLOW + str + ANSI_RESET; break;
			case ERROR : str = ANSI_RED + str + ANSI_RESET; break;
			default : throw new FatalError("Invalid log status.");
		}
		*/
		if(status == Status.CONSOLE)
			System.out.println(str);
		else
			p(str);
	}

	public enum Status {
		INFO,
		WARNING,
		ERROR,
		CONSOLE
	}
}