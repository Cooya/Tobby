package frames;

import main.Instance;
import main.Latency;
import messages.Message;
import messages.synchronisation.BasicLatencyStatsMessage;
import messages.synchronisation.BasicStatMessage;
import messages.synchronisation.SequenceNumberMessage;

public class SynchronisationFrame implements IFrame {
	private Instance instance;
	private int sequenceNumber;
	private int basicNoOperationMsgCounter;
	
	public SynchronisationFrame(Instance instance) {
		this.instance = instance;
		this.sequenceNumber = 1;
		this.basicNoOperationMsgCounter = 0;
	}
	
	public boolean processMessage(Message msg) {
		switch(msg.getId()) {
			case 153 :
				BasicStatMessage BSM = new BasicStatMessage();
				BSM.serialize();
				instance.outPush(BSM);
				return true;
			case 5816 :
				BasicLatencyStatsMessage BLSM = new BasicLatencyStatsMessage();
				Latency latency = instance.getLatency();
				BLSM.serialize(latency.latencyAvg(), latency.latencySamplesCount(), latency.latencySamplesMax(), instance.getInstanceId());
				instance.outPush(BLSM);
				return true;
			case 6316 :
				SequenceNumberMessage SNM = new SequenceNumberMessage();
				SNM.serialize(this.sequenceNumber++);
				instance.outPush(SNM);
				return true;
			case 176 :
				if(this.basicNoOperationMsgCounter++ % 10 == 0) {
					BSM = new BasicStatMessage();
					BSM.serialize();
					instance.outPush(BSM);
				}
				return true;
		}
		return false;
	}
}