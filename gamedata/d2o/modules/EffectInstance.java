package gamedata.d2o.modules;

@SuppressWarnings("unused")
public class EffectInstance {
	private static final String UNKNOWN_NAME = "???";
	private static final int UNDEFINED_CATEGORY = -2;
	private static final int UNDEFINED_SHOW = -1;
	private static final String UNDEFINED_DESCRIPTION = "undefined";

	public int effectUid;
	public int effectId;
	public int targetId;
	public String targetMask;
	public int duration;
	public int delay;
	public int random;
	public int group;
	public int modificator;
	public boolean trigger;
	public String triggers;
	public boolean visibleInTooltip = true;
	public boolean visibleInBuffUi = true;
	public boolean visibleInFightLog = true;
	public Object zoneSize;
	public int zoneShape;
	public Object zoneMinSize;
	public Object zoneEfficiencyPercent;
	public Object zoneMaxEfficiency;
	public Object zoneStopAtTarget;
	public int effectElement;
	private Effect _effectData;
	private int _durationStringValue;
	private int _delayStringValue;
	private String _durationString;
	private int _order = 0;
	private int _bonusType = -2;
	private int _oppositeId = -1;
	private int _category = -2;
	private String _description = "undefined";
	private String _theoricDescription = "undefined";
	private int _showSet = -1;
	private boolean _rawZoneInit;
	private String rawZone;
}