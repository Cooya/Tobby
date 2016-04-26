package controller;

import gamedata.d2o.modules.SubArea;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Hashtable;

import utilities.BiMap;
import main.FatalError;

// 92 -> contour d'Astrub
// 95 -> cité d'Astrub
// 97 -> forêt d'Astrub
// 98 -> champs d'Astrub
// 101 -> coin des tofus
// 173 -> prairies d'Astrub
// 442 -> lac d'Incarnam
// 443 -> forêt d'Incarnam
// 444 -> champs d'Incarnam
// 445 -> pâturages d'Incarnam
// 450 -> route des âmes d'Incarnam

public class FightOptions {
	private static BiMap<Integer, String> areaTable = new BiMap<Integer, String>(Integer.class, String.class);
	private static final int[] fightAreasId = {92, 95, 97, 98, 101, 173, 442, 443, 444, 445, 450};
	private static final int[][] fightAreasRawParameters = {
		{40, 80, 120, 160, 200, 250, 300},
		{40, 80, 120, 160, 200, 250, 300},
		{60, 100, 140, 180, 220, 270, 320},
		{40, 80, 120, 160, 200, 250, 300},
		{30, 60, 100, 150, 200, 250, 300},
		{40, 80, 120, 160, 200, 250, 300},
		{10, 15},
		{15, 20},
		{10, 15},
		{10, 15},
		{1, 5}
	};
	private static int[][] xpPath = {
		{450, 10}, // jusqu'au niveau 10 -> route des âmes d'Incarnam
		{445, 20}, // jusqu'au niveau 20 -> pâturages d'Incarnam
		{443, 30}, // jusqu'au niveau 30 -> forêt d'Incarnam
		{98, 9999} // à partir du niveau 30 -> champs d'Astrub
	};
	private static Hashtable<Integer, int[]> fightAreasParameters = new Hashtable<Integer, int[]>();
	static {
		for(int i = 0; i < fightAreasId.length; ++i)
			fightAreasParameters.put(fightAreasId[i], fightAreasRawParameters[i]);
		fillAreaTable();
	}
	
	public static String getAreaNameFromId(int areaId) {
		String areaName = (String) areaTable.get(areaId);
		if(areaName == null)
			throw new FatalError("Unhandled area.");
		return areaName;
	}
	
	public static int getAreaIdFromName(String areaName) {
		int areaId = (int) areaTable.get(areaName);
		if(areaId == 0) // équivalent de null
			throw new FatalError("Unhandled area.");
		return areaId;
	}
	
	public static Collection<String> getAreaNames() {
		return areaTable.values();
	}
	
	private static void fillAreaTable() {
		SubArea subArea;
		for(int subAreaId : fightAreasId) {
			subArea = SubArea.getSubAreaById(subAreaId);
			areaTable.put(subArea.id, new String(((subArea.getName() + " (" + subArea.getArea().getName() + ")").getBytes()), StandardCharsets.UTF_8));
		}
	}

	private boolean areaIsFixed;
	private int fightAreaId;
	private int[] fightAreaParameters;
	private int monsterGroupMinSize;
	private int monsterGroupMaxSize;

	public FightOptions(int fightAreaId, int monsterGroupMinSize) {
		this.areaIsFixed = fightAreaId != 0;
		this.fightAreaId = fightAreaId;
		this.fightAreaParameters = fightAreasParameters.get(fightAreaId);
		if(this.fightAreaId != 0 && this.fightAreaParameters == null)
			throw new FatalError("Unhandled fight area.");
		this.monsterGroupMinSize = monsterGroupMinSize;
		this.monsterGroupMaxSize = 1;
	}

	public FightOptions(int fightAreaId) {
		this(fightAreaId, 1);
	}

	public FightOptions() {
		this(0, 1);
	}

	public int getFightAreaId() {
		return this.fightAreaId;
	}

	public int getMonsterGroupMinSize() {
		return this.monsterGroupMinSize;
	}

	public int getMonsterGroupMaxSize() {
		return this.monsterGroupMaxSize;
	}

	// n'ayant pas le niveau du perso au moment de la création de l'objet FightOptions, cette méthode intervient plus tard
	public void updateFightArea(int level) {
		if(!this.areaIsFixed)
			setFightArea(level);
		setMonsterGroupMaxSize(level);
	}

	private void setFightArea(int level) {
		for(int i = 0; i < xpPath.length; ++i)
			if(level < xpPath[i][1]) {
				this.fightAreaId = xpPath[i][0];
				this.fightAreaParameters = fightAreasParameters.get(this.fightAreaId);
				return;
			}
	}

	private void setMonsterGroupMaxSize(int level) {
		for(int i = 0; i < fightAreaParameters.length; ++i)
			if(level < fightAreaParameters[i]) {
				this.monsterGroupMaxSize = i + 1;
				return;
			}
		this.monsterGroupMaxSize = 8;
	}
}