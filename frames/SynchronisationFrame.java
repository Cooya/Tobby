package frames;

import main.Instance;
import messages.Message;
import messages.synchronisation.BasicNoOperationMessage;
import messages.synchronisation.BasicStatMessage;
import messages.synchronisation.SequenceNumberMessage;

public class SynchronisationFrame implements Frame {
	private Instance instance;
	
	public SynchronisationFrame(Instance instance) {
		this.instance = instance;
	}
	
	public void processMessage(Message msg) {
		switch(msg.getId()) {
			case 153 :
				BasicStatMessage BSM = new BasicStatMessage();
				BSM.serialize();
				instance.outPush(BSM);
				break;
			case 6316 :
				SequenceNumberMessage SNM = new SequenceNumberMessage();
				SNM.serialize();
				instance.outPush(SNM);
				break;
			case 176 :
				new BasicNoOperationMessage(msg);
				if(BasicNoOperationMessage.getCounter() % 10 == 0) {
					BSM = new BasicStatMessage();
					BSM.serialize();
					instance.outPush(BSM);
				}
				break;
		}
	}
}