package messages.fights;

import java.util.Vector;

public class GameActionFightSpellCastMessage extends AbstractGameActionFightTargetedAbilityMessage {
    public int spellId = 0;
    public int spellLevel = 0;
    public Vector<Integer> portalsIds;
    
    @Override
	public void serialize() {
		// not implemented yet
	}

    @Override
	public void deserialize() {
		super.deserialize();
		this.portalsIds = new Vector<Integer>();
        this.spellId = this.content.readVarShort();
        this.spellLevel = this.content.readByte();
        int nb = this.content.readShort();
        for(int i = 0; i < nb; ++i)
        	this.portalsIds.add(this.content.readShort());
	}
}