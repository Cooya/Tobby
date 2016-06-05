package messages.fights;

public class GameActionFightSpellCastMessage extends AbstractGameActionFightTargetedAbilityMessage {
    public int spellId = 0;
    public int spellLevel = 0;
    public int[] portalsIds;
    
    @Override
	public void serialize() {
		
	}

    @Override
	public void deserialize() {
		super.deserialize();
        this.spellId = this.content.readVarShort();
        this.spellLevel = this.content.readByte();
        int nb = this.content.readShort();
        this.portalsIds = new int[nb];
        for(int i = 0; i < nb; ++i)
        	this.portalsIds[i] = this.content.readShort();
	}
}