package frames;

import main.CharacterController;
import main.NetworkInterface;
import messages.Message;

public abstract class Frame {
	protected NetworkInterface net;
	protected CharacterController CC;
	
	public Frame(NetworkInterface net, CharacterController CC) {
		this.net = net;
		this.CC = CC;
	}
	
	public abstract void processMessage(Message msg);
}
