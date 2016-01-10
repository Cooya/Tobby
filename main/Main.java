package main;

import java.io.InputStream;
import java.net.Socket;
import java.util.Stack;

import utilities.ByteArray;
import utilities.Log;
import messages.IdentificationMessage;
import messages.ReceivedMessage;

public class Main {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("213.248.126.39", 5555);
			Sender.create(socket.getOutputStream());
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[8192];
			
			Log.p("Connecting to server, waiting response...");
			int bytesReceived = 0;
			while(true) {
				bytesReceived = is.read(buffer);
				if(bytesReceived == -1)
					break;
				System.out.println();
				System.out.println(bytesReceived + " bytes received.");
				processMsgStack(Reader.processBuffer(new ByteArray(buffer, bytesReceived)));
			}
			Log.p("Deconnected from authentification server.");

			is.close();
			socket.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void processMsgStack(Stack<ReceivedMessage> msgStack) {
		ReceivedMessage msg;
		while(!msgStack.empty()) {
			msg = msgStack.pop();
			switch(msg.getId()) {
				case 3 : Sender.getInstance().send(new IdentificationMessage(msg.getContent())); break;
				default : return;
			}
		}
	}
}