package gamedata.d2o.modules;

import gamedata.d2i.I18n;
import gamedata.d2o.GameData;
import gamedata.d2o.GameDataFileAccessor;

import java.util.Arrays;
import java.util.Vector;

public class Item {
	public static final String MODULE = "Items";
	//private static final int[] SUPERTYPE_NOT_EQUIPABLE = {9, 14, 15, 16, 17, 18, 6, 19, 21, 20, 8, 22};
	private static final boolean[] FILTER_EQUIPEMENT = {false, true, true, true, true, true, false, true, true, false, true, true, true, true, false, false, false, false, false, false, false, false, true, true};
	private static final boolean[] FILTER_CONSUMABLES = {false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	private static final boolean[] FILTER_RESSOURCES = {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	private static final boolean[] FILTER_QUEST = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false};
	public static final int EQUIPEMENT_CATEGORY = 0;
	public static final int CONSUMABLES_CATEGORY = 1;
	public static final int RESSOURCES_CATEGORY = 2;
	public static final int QUEST_CATEGORY = 3;
	public static final int OTHER_CATEGORY = 4;
	public static final int MAX_JOB_LEVEL_GAP = 100;
	//private static Dictionary _censoredIcons;
	
	static {
		GameDataFileAccessor.getInstance().init(MODULE);
	}

	public int id;
	public int nameId;
	public int typeId;
	public int descriptionId;
	public int iconId;
	public int level;
	public int realWeight;
	public boolean cursed;
	public int useAnimationId;
	public boolean usable;
	public boolean targetable;
	public boolean exchangeable;
	public double price;
	public boolean twoHanded;
	public boolean etheral;
	public int itemSetId;
	public String criteria;
	public String criteriaTarget;
	public boolean hideEffects;
	public boolean enhanceable;
	public boolean nonUsableOnAnother;
	public int appearanceId;
	public boolean secretRecipe;
	public Vector<Integer> dropMonsterIds;
	public int recipeSlots;
	public Vector<Integer> recipeIds;
	public boolean bonusIsSecret;
	public Vector<EffectInstance> possibleEffects;
	public Vector<Integer> favoriteSubAreas;
	public int favoriteSubAreasBonus;
	public int craftXpRatio;
	public boolean needUseConfirm;
	public boolean isDestructible;
	public Vector<Vector<Double>> nuggetsBySubarea;
	public Vector<Integer> containerIds;
	public Vector<Vector<Integer>> resourcesBySubarea;
	private String _name;
	//private String _undiatricalName;
	private String _description;
	private ItemType _type;
	private int _weight;
	private ItemSet _itemSet;
	//private TiphonEntityLook _appearance;
	//private GroupItemCriterion _conditions;
	//private GroupItemCriterion _conditionsTarget;
	//private List<Recipe> _recipes;
	//private Dictionary _craftXpByJobLevel;
	private double _nuggetsQuantity = 0;

	public static Item getItemById(int id, boolean b) {
		Item item = (Item) GameData.getObject(MODULE, id);
		if(item != null || !b)
			return item;
		return (Item) GameData.getObject(MODULE, 666);
	}

	public static Item[] getItems() {
		Object[] objArray = GameData.getObjects(MODULE);
		return Arrays.copyOf(objArray, objArray.length, Item[].class);
	}

	public String getName() {
		if(this._name == null)
			this._name = I18n.getText(this.nameId);
		return this._name;
	}

	/*
	public String getUndiatricalName() {
		if(this._undiatricalName == null)
			this._undiatricalName = StringUtils.noAccent(this.name).toLowerCase();
		return (this._undiatricalName);
	}
	*/

	public String getDescription() {
		if(this._description == null)
			if(this.etheral)
				this._description = I18n.getUiText("ui.common.etherealWeaponDescription", null, "%");
			else
				this._description = I18n.getText(this.descriptionId);
		return this._description;
	}

	public int getWeight() {
		return this._weight;
	}

	public void setWeight(int weight) {
		this._weight = weight;
	}

	public ItemType getType() {
		if(this._type == null)
			this._type = ItemType.getItemTypeById(this.typeId);
		return this._type;
	}

	public boolean isWeapon() {
		return false;
	}

	public ItemSet getItemSet() {
		if(this._itemSet == null)
			this._itemSet = ItemSet.getItemSetById(this.itemSetId);
		return this._itemSet;
	}
	
	/*
	public TiphonEntityLook getAppearance() {
		Appearance appearance;
		if(this._appearance == null) {
			appearance = Appearance.getAppearanceById(this.appearanceId);
			if(appearance != null)
				this._appearance = TiphonEntityLook.fromString(appearance.data);
		}
		return this._appearance
	}
	*/
	
	/*
	public List<Recipe> getRecipes() {
		if(this._recipes == null) {
			this._recipes = new Vector<Recipe>();
			Recipe recipe;
			for(int i = 0; i < this.recipeIds.length; ++i) {
				recipe = Recipe.getRecipeByResultId(this.recipeIds.get(i));
				if(recipe != null)
					this._recipes.add(recipe);
			}
		}
		return this._recipes;
	}
	*/
	
	public int getCategory() {
		ItemType type = getType();
		if(FILTER_EQUIPEMENT[type.superTypeId])
			return EQUIPEMENT_CATEGORY;
		else if(FILTER_CONSUMABLES[type.superTypeId])
			return CONSUMABLES_CATEGORY;
		else if(FILTER_RESSOURCES[type.superTypeId])
			return RESSOURCES_CATEGORY;
		else if(FILTER_QUEST[type.superTypeId])
			return QUEST_CATEGORY;
		else
			return OTHER_CATEGORY;
	}
	
	/*
	public boolean isEquipable() {
		return SUPERTYPE_NOT_EQUIPABLE[getType().superTypeId];
	}
	*/
	
	/*
	public boolean canEquip() {
		PlayedCharacterManager character = PlayedCharacterManager.getInstance();
		if(!isEquipable())
			return false;
		if(character != null && character.infos.level <= this.level)
			return false;
		return this._conditions.isRespected;
	}
	*/
	
	/*
	public GroupItemCriterion getConditions() {
		if(this.criteria == null)
			return null;
		if(this._conditions == null)
			this._conditions = new GroupItemCriterion(this.criteria);
		return this._conditions;
	}
	*/
	
	/*
	public GroupItemCriterion getTargetConditions() {
		if(this.criteriaTarget == null)
			return null;
		if(this._conditionsTarget == null)
			this._conditionsTarget = new GroupItemCriterion(this.criteriaTarget);
		return this._conditionsTarget;
	}
	*/
	
	public int getCraftXpByJobLevel(int jobLevel) {
		// not implemented
		return 0;
	}
	
	public double getNuggetsQuantity() {
		if(this._nuggetsQuantity == 0)
			for(Vector<Double> nuggets : this.nuggetsBySubarea)
				this._nuggetsQuantity += nuggets.get(1);
		return this._nuggetsQuantity;
	}
	
	public void copy(Item item1, Item item2) {
		// not implemented
	}
	
	
	public void postInit() {
		// not implemented yet
	}
}