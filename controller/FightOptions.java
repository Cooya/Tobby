package controller;

import java.util.Hashtable;

import main.FatalError;

// 92 -> contour d'Astrub
// 95 -> pious d'Astrub
// 442 -> lac d'Incarnam
// 443 -> forêt d'Incarnam
// 445 -> pâturages d'Incarnam
// 450 -> route des âmes d'Incarnam

public class FightOptions {
	private static final int[] fightAreasId = {92, 95, 442, 443, 445, 450};
	private static final int[][] fightAreasRawParameters = {
		{40, 80, 120, 160, 200, 250, 300},
		{40, 80, 120, 160, 200, 250, 300},
		{10, 15},
		{15, 20},
		{10, 15},
		{1, 5}
	};
	private static int[][] xpPath = {
		{450, 10},
		{445, 20},
		{443, 30},
		{92, 9999}
	};
	private static Hashtable<Integer, int[]> fightAreasParameters = new Hashtable<Integer, int[]>();
	static {
		for(int i = 0; i < fightAreasId.length; ++i)
			fightAreasParameters.put(fightAreasId[i], fightAreasRawParameters[i]);
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