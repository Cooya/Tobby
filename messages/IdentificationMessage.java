package messages;

import main.Message;

public class IdentificationMessage extends MessageToSend implements Runnable {
	public static final int id = 4;
	public String Account;
	public String Password;
	
	public IdentificationMessage(Message msg,String Account,String Password) {
		super();
		this.Account=Account;
		this.Password=Password;
		
		
		// create response message
		run();
	}

	public void run() {
		// send to server
	}
}
