package messages.synchronisation;

import utilities.ByteArray;
import main.Emulation;
import messages.Message;

public class BasicLatencyStatsMessage extends Message {
    public int latency = 0;
    public int sampleCount = 0;
    public int max = 0;

    public BasicLatencyStatsMessage() {
    	super();
    }
    
    public void serialize(int latency, int sampleCount, int max, int instanceId) {
    	this.latency = latency;
    	this.sampleCount = sampleCount;
    	this.max = max;
    	
    	ByteArray buffer = new ByteArray();
    	buffer.writeShort((short) this.latency);
    	buffer.writeVarShort(this.sampleCount);
    	buffer.writeVarShort(this.max);
    	
    	completeInfos(Emulation.hashMessage(buffer, instanceId));
    }
}