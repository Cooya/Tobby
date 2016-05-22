package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import utilities.Processes;
import messages.NetworkMessage;

public class Log {
	private static final String EOL = System.getProperty("line.separator");
	private static final StringBuilder globalLog = new StringBuilder();
	private String logFilepath;
	private PrintWriter writer;
	private StringBuilder logFrameString;
	private StringBuilder logFileBuffer;
	private long lastFlushDate;
	private ReentrantLock stringLock;
	private ReentrantLock bufferLock;
	private CharacterFrame graphicalFrame;
	
	public Log(String login, CharacterFrame graphicalFrame) {
		if(!Processes.dirExists(Main.LOG_PATH))
			new File(Main.LOG_PATH).mkdir();
		this.logFilepath = Main.LOG_PATH + login + ".txt";
		try {
			this.writer = new PrintWriter(this.logFilepath, "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.logFrameString = new StringBuilder();
		this.logFileBuffer = new StringBuilder();
		this.lastFlushDate = new Date().getTime();
		this.stringLock = new ReentrantLock();
		this.bufferLock = new ReentrantLock();
		this.graphicalFrame = graphicalFrame;
	}
	
	public void p(String msgDirection, NetworkMessage msg) { // pour la réception et l'envoi de messages
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
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] INFO : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		System.out.println(msg);
	}
	
	public static void warn(String msg) {
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] WARNING : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		System.out.println(msg);
	}
	
	public static void err(String msg) {
		msg = "[" + Main.DATE_FORMAT.format(new Date()) + "] ERROR : " + msg;
		globalLog.append(msg);
		globalLog.append(EOL);
		System.out.println(msg);
	}
	
	public static void displayGlobalLog() {
		System.out.println(globalLog);
	}
	
	public void displayLog(int linesNumber) {
		System.out.println(tail(new File(this.logFilepath), linesNumber));
	}
	
	public void flushBuffer() {
		this.bufferLock.lock();
		this.writer.print(this.logFileBuffer);
		this.writer.flush();
		this.logFileBuffer.setLength(0);
		this.bufferLock.unlock();
		this.lastFlushDate = new Date().getTime();
	}
	
	private void writeIntoLogFile(StringBuilder msg) {
		this.bufferLock.lock();
		this.logFileBuffer.append(msg);
		this.bufferLock.unlock();
		if(new Date().getTime() - this.lastFlushDate > 10000) // 10 secondes
			flushBuffer();
	}
	
	// fonction récupérée sur le net
	private String tail(File file, int lines) {
	    java.io.RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = 
	            new java.io.RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();
	        int line = 0;

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	             if( readByte == 0xA ) {
	                if (filePointer < fileLength) {
	                    line = line + 1;
	                }
	            } else if( readByte == 0xD ) {
	                if (filePointer < fileLength-1) {
	                    line = line + 1;
	                }
	            }
	            if (line >= lines) {
	                break;
	            }
	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    }
	    finally {
	        if (fileHandler != null )
	            try {
	                fileHandler.close();
	            } catch (IOException e) {
	            }
	    }
	}
}