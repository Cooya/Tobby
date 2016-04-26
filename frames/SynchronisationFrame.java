package frames;

import controller.characters.Character;
import main.Instance;
import main.Latency;
import messages.synchronisation.BasicLatencyStatsMessage;
import messages.synchronisation.BasicLatencyStatsRequestMessage;
import messages.synchronisation.SequenceNumberMessage;
import messages.synchronisation.SequenceNumberRequestMessage;

public class SynchronisationFrame extends Frame {
	private int sequenceNumber;
	//private int basicNoOperationMsgCounter;
	
	public SynchronisationFrame(Instance instance, Character character) {
		super(instance, character);
		this.instance = instance;
		this.sequenceNumber = 1;
	}
	
	protected void process(SequenceNumberRequestMessage SNRM) {
		SequenceNumberMessage SNM = new SequenceNumberMessage();
		SNM.serialize(this.sequenceNumber++);
		instance.outPush(SNM);
	}
	
	protected void process(BasicLatencyStatsRequestMessage BLSRM) {
		BasicLatencyStatsMessage BLSM = new BasicLatencyStatsMessage();
		Latency latency = instance.getLatency();
		BLSM.serialize(latency.latencyAvg(), latency.latencySamplesCount(), latency.latencySamplesMax(), instance.id);
		instance.outPush(BLSM);
	}
	
	/*
	protected void process(BasicNoOperationMessage BNOM) {
		if(this.basicNoOperationMsgCounter++ % 10 == 0) {
			BasicStatMessage BSM = new BasicStatMessage();
			BSM.serialize();
			instance.outPush(BSM);
		}
	}
	*/
}