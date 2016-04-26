package messages.fights;

public class AbstractGameActionFightTargetedAbilityMessage extends AbstractGameActionMessage {
    public double targetId = 0;
    public int destinationCellId = 0;
    public int critical = 1;
    public boolean silentCast = false;
    
    @Override
	public void serialize() {
		// not implemented yet
	}
    
    @Override
    public void deserialize() {
    	super.deserialize();
        this.targetId = this.content.readDouble();
        this.destinationCellId = this.content.readShort();
        this.critical = this.content.readByte();
        this.silentCast = this.content.readBoolean();
    }
}