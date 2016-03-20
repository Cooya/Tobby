package messages.fights;

import utilities.ByteArray;
import messages.Message;

public class AbstractGameActionFightTargetedAbilityMessage extends AbstractGameActionMessage {
    public double targetId = 0;
    public int destinationCellId = 0;
    public int critical = 1;
    public boolean silentCast = false;
    
    public AbstractGameActionFightTargetedAbilityMessage(Message msg) {
    	super(msg);
    }
    
    protected void deserialize(ByteArray buffer) {
    	super.deserialize(buffer);
        this.targetId = buffer.readDouble();
        this.destinationCellId = buffer.readShort();
        this.critical = buffer.readByte();
        this.silentCast = buffer.readBoolean();
    }
}