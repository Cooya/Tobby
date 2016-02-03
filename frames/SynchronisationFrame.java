package frames;

import main.CharacterController;
import main.NetworkInterface;
import messages.Message;
import messages.synchronisation.BasicNoOperationMessage;
import messages.synchronisation.BasicStatMessage;
import messages.synchronisation.SequenceNumberMessage;

public class SynchronisationFrame extends Frame {
	
	public SynchronisationFrame(NetworkInterface net, CharacterController CC) {
		super(net, CC);
	}
	
	public void processMessage(Message msg) {
		switch(msg.getId()) {
			case 153 :
				BasicStatMessage BSM = new BasicStatMessage();
				BSM.serialize();
				net.sendMessage(BSM);
				break;
			case 6316 :
				SequenceNumberMessage SNM = new SequenceNumberMessage();
				SNM.serialize();
				net.sendMessage(SNM);
				break;
			case 176 :
				new BasicNoOperationMessage(msg);
				if(BasicNoOperationMessage.getCounter() % 10 == 0) {
					BSM = new BasicStatMessage();
					BSM.serialize();
					net.sendMessage(BSM);
				}
				break;
		}
	}
}