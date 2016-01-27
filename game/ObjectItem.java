package game;

import java.util.Vector;

import utilities.ByteArray;

public class ObjectItem {

	public static final int protocolId = 37;

	public int position = 63;

	public int objectGID = 0;

	public Vector<ObjectEffect> effects;

	public int objectUID = 0;

	public int quantity = 0;

	public ObjectItem()
	{
		this.effects = new Vector<ObjectEffect>();
	}

	public int getTypeId() 
	{
		return 37;
	}

	public ObjectItem initObjectItem(int pos , int GID , Vector<ObjectEffect> effcts, int UID , int Quantity) 
	{

		this.position = pos;
		this.objectGID = GID;
		this.effects = effcts;
		this.objectUID = UID;
		this.quantity = Quantity;
		return this;
	}

	public void reset() 
	{
		this.position = 63;
		this.objectGID = 0;
		this.effects = new Vector<ObjectEffect>();
		this.objectUID = 0;
		this.quantity = 0;
	}





	public void deserialize(ByteArray buffer) 
	{
		int loc4 = 0;
		ObjectEffect loc5 = null;
		this.position = buffer.readByte();
		if(this.position < 0 || this.position > 255)
		{
			throw new Error("Forbidden value (" + this.position + ") on element of ObjectItem.position.");
		}
		this.objectGID = buffer.readVarShort();
		System.out.println("Id de l'item: "+this.objectGID);
		if(this.objectGID < 0)
		{
			throw new Error("Forbidden value (" + this.objectGID + ") on element of ObjectItem.objectGID.");
		}
		int loc2 = buffer.readShort();
		int loc3 = 0;
		while(loc3 < loc2)
		{
			loc4 = buffer.readShort();
			System.out.println("Pendant le tour:"+loc3+"/"+loc2+" loc4 vaut:"+loc4);
			switch(loc4){
			case 76:
				loc5=new ObjectEffect();
				break;
			case 70:
				loc5= new ObjectEffectInteger();
				break;
			case 72:
				loc5=new ObjectEffectDate();
				break;
			case 73:
				loc5=new ObjectEffectDice();
				break;
			case 71:
				loc5=new ObjectEffectCreature();
				break;
			case 75:
				loc5=new ObjectEffectDuration();
				break;
			case 81:
				loc5=new ObjectEffectLadder();
				break;
			case 82:
				loc5=new ObjectEffectMinMax();
				break;
			case 179:
				loc5=new ObjectEffectMount();
				break;
			case 74:
				loc5=new ObjectEffectString();
				break;
			default: 
				loc5=new ObjectEffectInteger();
            	break;
			}
			System.out.println("Classe: "+loc5.getClass()+" "+loc5.getTypeId());
			loc5.deserialize(buffer);
			this.effects.addElement(loc5);
			loc3++;
		}
		this.objectUID = buffer.readVarInt();
		if(this.objectUID < 0)
		{
			throw new Error("Forbidden value (" + this.objectUID + ") on element of ObjectItem.objectUID.");
		}
		this.quantity = buffer.readVarInt();
		System.out.println("Quantity="+quantity+"\n");
		if(this.quantity < 0)
		{
			throw new Error("Forbidden value (" + this.quantity + ") on element of ObjectItem.quantity.");
		}
	}
}

