package messages;

public class CheckIntegrityMessage extends Message {
	public static final int ID = 6372;

	public CheckIntegrityMessage(Message msg) {
		super(ID, msg.getLenOfSize(), msg.getSize(), msg.getContent());
	}
}
