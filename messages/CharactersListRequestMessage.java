package messages;

public class CharactersListRequestMessage extends Message {
	public static final int ID = 150;
	
	public CharactersListRequestMessage() {
		super(ID);
	}
	
	public void serialize() {
		this.size = 0;
		this.lenofsize = 0;
		this.content = null;
	}
}
