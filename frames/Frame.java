package frames;

import messages.Message;

public abstract class Frame {
	public boolean isActive;
	
	public Frame() {
		this.isActive = false;
	}
	
	public abstract boolean processMessage(Message msg);
}