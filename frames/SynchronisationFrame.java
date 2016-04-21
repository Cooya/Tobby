package frames;

import controller.characters.Character;
import main.Instance;
import main.Latency;
import messages.Message;
import messages.synchronisation.BasicLatencyStatsMessage;
import messages.synchronisation.SequenceNumberMessage;

public class SynchronisationFrame extends Frame {
	private int sequenceNumber;
	//private int basicNoOperationMsgCounter;
	
	public SynchronisationFrame(Instance instance, Character character) {
		super(instance, character);
		this.instance = instance;
		this.sequenceNumber = 1;
		//this.basicNoOperationMsgCounter = 0;
	}
	
	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 5816 : // BasicLatencyStatsRequestMessage
				BasicLatencyStatsMessage BLSM = new BasicLatencyStatsMessage();
				Latency latency = instance.getLatency();
				BLSM.serialize(latency.latencyAvg(), latency.latencySamplesCount(), latency.latencySamplesMax(), instance.id);
				instance.outPush(BLSM);
				return true;
			case 6316 : // SequenceNumberRequestMessage
				SequenceNumberMessage SNM = new SequenceNumberMessage();
				SNM.serialize(this.sequenceNumber++);
				instance.outPush(SNM);
				return true;
			/*
			case 176 : // BasicNoOperationMessage
				if(this.basicNoOperationMsgCounter++ % 10 == 0) {
					BSM = new BasicStatMessage();
					BSM.serialize();
					instance.outPush(BSM);
				}
				return true;
			*/
		}
		return false;
	}
}