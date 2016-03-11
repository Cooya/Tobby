package main;

import gui.CharacterFrame;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Date;

import messages.Message;

public class Log {
	private static final boolean DEBUG = false;
	private static String LOG_PATH = System.getProperty("user.dir") + "/Ressources/";
	private static String EOL = System.getProperty("line.separator");
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
	
	public void p(String msgDirection, Message msg) { // pour la réception et l'envoi de messages
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
		if(name == null)
			graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str, Color.BLACK);
		writer.println("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str);
		writer.flush();
	}
	
	public void p(String str) {
		if(DEBUG)
			graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str + EOL, Color.BLACK);
		else
			graphicalFrame.appendText("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str, Color.BLACK);
		writer.println("[" + Main.DATE_FORMAT.format(new Date()) + "] " + str);
		writer.flush();
	}
}