package messages.synchronisation;

import messages.NetworkMessage;

public class BasicLatencyStatsMessage extends NetworkMessage {
    public int latency = 0;
    public int sampleCount = 0;
    public int max = 0;
    
    @Override
    public void serialize() {
    	this.content.writeShort(this.latency);
    	this.content.writeVarShort(this.sampleCount);
    	this.content.writeVarShort(this.max);
    }
    
    @Override
	public void deserialize() {
		// not implemented yet
	}
}