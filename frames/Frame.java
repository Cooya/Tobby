package frames;

import messages.Message;

public interface Frame { // sert juste � unifier les frames
	public void processMessage(Message msg);
}
