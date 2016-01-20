package messages.connection;


import messages.Message;

public class CheckIntegrityMessage extends Message {
	public static final int ID = 6372;

	public CheckIntegrityMessage(Message msg) {
		super(msg);
	}
}
