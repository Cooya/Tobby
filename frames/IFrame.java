package frames;

import messages.Message;

public interface IFrame { // sert juste à unifier les frames
	public boolean processMessage(Message msg);
}
