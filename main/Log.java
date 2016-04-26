package main;

import gui.CharacterFrame;

import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import messages.Message;

public class Log {
	private static final int WRITE_INTERVAL = 10;
	private static final String LOG_PATH = System.getProperty("user.dir") + "/Ressources/Logs/";
	private static final String EOL = System.getProperty("line.separator");
	private PrintWriter writer;
	private StringBuilder logString;
	private StringBuilder writeBuffer;
	private int writeCounter;
	private ReentrantLock lock;
	public CharacterFrame graphicalFrame;
	
	public Log(String characterName, CharacterFrame graphicalFrame) {
		try {
			this.writer = new PrintWriter(LOG_PATH + characterName + ".txt", "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.logString = new StringBuilder();
		this.writeBuffer = new StringBuilder();
		this.writeCounter = 0;
		this.lock = new ReentrantLock();
		this.graphicalFrame = graphicalFrame;
	}
	
	public void p(String msgDirection, Message msg) { // pour la réception et l'envoi de messages
		if(Thread.currentThread().isInterrupted())
			return;
		
		// ajout de la date et de l'heure
		this.logString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ");
		
		//int lenofsize = msg.getLenOfSize();
		//int size = msg.getSize();
		if(msgDirection == "r" || msgDirection == "reception")
			this.logString.append("Receiving message ").append(msg.getId()).append(" (").append(msg.getName()).append(")");
		else if(msgDirection == "s" || msgDirection == "sending")
			this.logString.append("Sending message " + msg.getId() + " (" + msg.getName() + ")");
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
		this.logString.append(EOL);
		writeIntoLogFile(this.logString);
		this.logString.setLength(0);
	}
	
	public void p(String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		
		this.logString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ").append(str).append(EOL);
		if(this.graphicalFrame != null)
			graphicalFrame.appendText(this.logString);
		writeIntoLogFile(this.logString);
		this.logString.setLength(0);
	}
	
	public static void info(String msg) {
		System.out.println("[" + Main.DATE_FORMAT.format(new Date()) + "] INFO : " + msg);
	}
	
	public static void warn(String msg) {
		System.out.println("[" + Main.DATE_FORMAT.format(new Date()) + "] WARNING : " + msg);
	}
	
	public static void err(String msg) {
		System.out.println("[" + Main.DATE_FORMAT.format(new Date()) + "] ERROR : " + msg);
	}
	
	public void flushBuffer() {
		this.writer.print(this.writeBuffer);
		this.writer.flush();
		this.writeBuffer.setLength(0);
		this.writeCounter = 0;
	}
	
	private void writeIntoLogFile(StringBuilder msg) {
		this.lock.lock();
		this.writeBuffer.append(msg);
		if(++this.writeCounter == WRITE_INTERVAL)
			flushBuffer();
		this.lock.unlock();
	}
}