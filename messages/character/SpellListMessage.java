package messages.character;

import java.util.Vector;

import gamedata.character.SpellItem;
import messages.Message;
import utilities.ByteArray;

public class SpellListMessage extends Message{


	public static final int Id = 1200;

	public boolean spellPrevisualization = false;

	public Vector<SpellItem> spells;

	public SpellListMessage(Message msg)
	{
		super(msg);
		spells=new Vector<SpellItem>();
	}

	public void deserialize(){
		ByteArray buffer=new ByteArray(content);
		SpellItem tmpSpell = null;
		this.spellPrevisualization = buffer.readBoolean();
		int nbSpell = buffer.readShort();
		int i = 0;
		while(i < nbSpell)
		{
			tmpSpell = new SpellItem(buffer);
			this.spells.add(tmpSpell);
			i++;
		}
	}

}
