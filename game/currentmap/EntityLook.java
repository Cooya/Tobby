package game.currentmap;

import java.util.Vector;

import utilities.ByteArray;

public class EntityLook {
    public int bonesId = 0;
    public Vector<Integer> skins;
    public Vector<Integer> indexedColors;
    public Vector<Integer> scales;
    public Vector<SubEntity> subentities;
    
    public EntityLook(ByteArray buffer) {
        this.skins = new Vector<Integer>();
        this.indexedColors = new Vector<Integer>();
        this.scales = new Vector<Integer>();
        this.subentities = new Vector<SubEntity>();
        
        this.bonesId = buffer.readVarShort();
        int nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.skins.add(buffer.readVarShort());
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.indexedColors.add(buffer.readInt());
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.scales.add(buffer.readVarShort());
        nb = buffer.readShort();
        for(int i = 0; i < nb; ++i)
        	this.subentities.add(new SubEntity(buffer));
    }
}
