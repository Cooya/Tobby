package frames;

import network.Latency;
import controller.characters.Character;
import messages.synchronisation.BasicAckMessage;
import messages.synchronisation.BasicLatencyStatsMessage;
import messages.synchronisation.BasicLatencyStatsRequestMessage;
import messages.synchronisation.SequenceNumberMessage;
import messages.synchronisation.SequenceNumberRequestMessage;

public class SynchronisationFrame extends Frame {
	private int sequenceNumber;
	//private int basicNoOperationMsgCounter;
	
	public SynchronisationFrame(Character character) {
		super(character);
		this.sequenceNumber = 1;
	}
	
	protected void process(BasicAckMessage BAM) {
		this.character.net.acknowledgeMessage(BAM.lastPacketId);
		this.character.log.p("Acknowledgement received for message with id = " + BAM.lastPacketId + ".");
	}
	
	protected void process(SequenceNumberRequestMessage SNRM) {
		SequenceNumberMessage SNM = new SequenceNumberMessage();
		SNM.number = this.sequenceNumber++;
		this.character.net.send(SNM);
	}
	
	protected void process(BasicLatencyStatsRequestMessage BLSRM) {
		BasicLatencyStatsMessage BLSM = new BasicLatencyStatsMessage();
		Latency latency = character.net.getLatency();
		BLSM.latency = latency.latencyAvg();
		BLSM.sampleCount = latency.latencySamplesCount();
		BLSM.max = latency.latencySamplesMax();
		this.character.net.send(BLSM);
	}
	
	/*
	protected void process(BasicNoOperationMessage BNOM) {
		if(this.basicNoOperationMsgCounter++ % 10 == 0) {
			BasicStatMessage BSM = new BasicStatMessage();
			this.character.net.send(BSM);
		}
	}
	*/
}