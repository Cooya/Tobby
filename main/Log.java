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
	private StringBuilder logFrameString;
	private StringBuilder logFileBuffer;
	private int writeCounter;
	private ReentrantLock stringLock;
	private ReentrantLock bufferLock;
	public CharacterFrame graphicalFrame;
	
	public Log(String characterName, CharacterFrame graphicalFrame) {
		try {
			this.writer = new PrintWriter(LOG_PATH + characterName + ".txt", "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.logFrameString = new StringBuilder();
		this.logFileBuffer = new StringBuilder();
		this.writeCounter = 0;
		this.stringLock = new ReentrantLock();
		this.bufferLock = new ReentrantLock();
		this.graphicalFrame = graphicalFrame;
	}
	
	public void p(String msgDirection, Message msg) { // pour la réception et l'envoi de messages
		if(Thread.currentThread().isInterrupted())
			return;
		this.stringLock.lock();
		this.logFrameString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ");
		if(msgDirection == "r" || msgDirection == "reception")
			this.logFrameString.append("Receiving message ").append(msg.getId()).append(" (").append(msg.getName()).append(")");
		else if(msgDirection == "s" || msgDirection == "sending")
			this.logFrameString.append("Sending message " + msg.getId() + " (" + msg.getName() + ")");
		this.logFrameString.append(EOL);
		writeIntoLogFile(this.logFrameString);
		this.logFrameString.setLength(0);
		this.stringLock.unlock();
	}
	
	public void p(String str) {
		if(Thread.currentThread().isInterrupted())
			return;
		this.stringLock.lock();
		this.logFrameString.append("[").append(Main.DATE_FORMAT.format(new Date())).append("] ").append(str).append(EOL);
		if(this.graphicalFrame != null)
			this.graphicalFrame.appendText(this.logFrameString);
		writeIntoLogFile(this.logFrameString);
		this.logFrameString.setLength(0);
		this.stringLock.unlock();
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
		this.bufferLock.lock();
		this.writer.print(this.logFileBuffer);
		this.writer.flush();
		this.logFileBuffer.setLength(0);
		this.bufferLock.unlock();
		this.writeCounter = 0;
	}
	
	private void writeIntoLogFile(StringBuilder msg) {
		this.bufferLock.lock();
		this.logFileBuffer.append(msg);
		this.bufferLock.unlock();
		if(++this.writeCounter == WRITE_INTERVAL)
			flushBuffer();
	}
}