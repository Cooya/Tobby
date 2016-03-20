package messages.fights;

import java.util.Vector;

import utilities.ByteArray;
import messages.Message;

public class GameActionFightSpellCastMessage extends AbstractGameActionFightTargetedAbilityMessage {
    public int spellId = 0;
    public int spellLevel = 0;
    public Vector<Integer> portalsIds;
	
	public GameActionFightSpellCastMessage(Message msg) {
		super(msg);
		this.portalsIds = new Vector<Integer>();
		deserialize();
	}
	
	private void deserialize() {
		ByteArray buffer = new ByteArray(this.content);
		super.deserialize(buffer);
        this.spellId = buffer.readVarShort();
        this.spellLevel = buffer.readByte();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.portalsIds.add(buffer.readShort());
	}
}