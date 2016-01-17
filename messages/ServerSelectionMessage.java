package messages;

public class ServerSelectionMessage extends Message {
	public static final int ID = 40;
	
	public ServerSelectionMessage() {
		super(ID, 0, 0, null);
		
		serialize();
	}
	
	private void serialize() {
		this.size = 1;
		this.lenofsize = computeLenOfSize(this.size);
		this.content = new byte[1];
		this.content[0] = 11; // Brumaire
	}
}
