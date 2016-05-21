package gamedata.d2o.modules;

import gamedata.d2i.I18n;
import gamedata.d2o.GameData;
import gamedata.d2o.GameDataFileAccessor;

public class Effect {
	public static final String MODULE = "Effects";

	static {
		GameDataFileAccessor.getInstance().init(MODULE);
	}

	public int id;
	public int descriptionId;
	public int iconId;
	public int characteristic;
	public int category;
	public String operator;
	public boolean showInTooltip;
	public boolean useDice;
	public boolean forceMinMax;
	public boolean boost;
	public boolean active;
	public int oppositeId;
	public int theoreticalDescriptionId;
	public int theoreticalPattern;
	public boolean showInSet;
	public int bonusType;
	public boolean useInFight;
	public int effectPriority;
	public int elementId;
	private String _description;
	private String _theoricDescription;

	public static Effect getEffectById(int id) {
		return (Effect) GameData.getObject(MODULE, id);
	}

	public String getDescription() {
		if(this._description == null)
			this._description = I18n.getText(this.descriptionId);
		return this._description;
	}
	public String getTheoreticalDescription() {
		if(this._theoricDescription == null)
			this._theoricDescription = I18n.getText(this.theoreticalDescriptionId);
		return this._theoricDescription;
	}
}