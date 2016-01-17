package messages;

public class IdentificationSuccessMessage extends Message {
	public static final int ID = 22;
	
	public IdentificationSuccessMessage(ReceivedMessage msg) {
		super(ID, msg.getLenOfSize(), msg.getSize(), msg.getContent());
	}
}
