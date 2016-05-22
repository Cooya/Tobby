package controller.informations;

import gamedata.d2o.modules.SubArea;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	// contient les différents identifiants des aires de combat gérées
	private static final int[] fightAreasId = {92, 95, 97, 98, 101, 173, 442, 443, 444, 445, 450};
	
	// contient les paliers de niveau pour chaque aire de combat gérée
	private static final int[][] fightAreasRawParameters = {
		{20, 40, 80, 120, 160, 200, 250, 300},
		{20, 40, 80, 120, 160, 200, 250, 300},
		{30, 60, 100, 140, 180, 220, 270, 320},
		{20, 40, 80, 120, 160, 200, 250, 300},
		{15, 30, 60, 100, 150, 200, 250, 300},
		{20, 40, 80, 120, 160, 200, 250, 300},
		{1, 10, 15},
		{10, 15, 20},
		{1, 10, 15},
		{1, 10, 15},
		{1, 1, 5}
	};
	
	// représente les différentes aires où le perso combattra pour xp le plus rapidement possible
	private static final int[][] xpPath = {
		{450, 10}, // jusqu'au niveau 10 -> route des âmes d'Incarnam
		{445, 20}, // jusqu'au niveau 20 -> pâturages d'Incarnam
		{443, 30}, // jusqu'au niveau 30 -> forêt d'Incarnam
		{92, 9999} // à partir du niveau 30 -> contour d'Astrub
	};
	
	// associe les identifiants des aires de combat à leurs paliers de niveau
	private static final Map<Integer, int[]> fightAreasParameters = new HashMap<Integer, int[]>();
	
	// associe les identifiants des aires de combat à leur nom
	private static final BiMap<Integer, String> areaTable = new BiMap<Integer, String>(Integer.class, String.class);
	
	static {
		for(int i = 0; i < fightAreasId.length; ++i)
			fightAreasParameters.put(fightAreasId[i], fightAreasRawParameters[i]);
		
		SubArea subArea;
		for(int subAreaId : fightAreasId) {
			subArea = SubArea.getSubAreaById(subAreaId);
			areaTable.put(subArea.id, new String(((subArea.getName() + " (" + subArea.getArea().getName() + ")").getBytes()), StandardCharsets.UTF_8));
		}
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
	
	public static void displayFightAreas() {
		for(int fightAreaId : FightOptions.fightAreasId)
			System.out.println(fightAreaId + " -> \"" + areaTable.get(fightAreaId) + "\"");
	}
	
	public static boolean isHandledFightArea(int id) {
		return fightAreasParameters.containsKey(id);
	}

	private int fixedFightAreaId;
	private int fightAreaId;
	private int[] fightAreaParameters;
	private int monsterGroupMaxSize;

	public FightOptions(int fightAreaId) {
		this.fixedFightAreaId = fightAreaId;
		this.fightAreaId = 0;
		this.fightAreaParameters = fightAreasParameters.get(fightAreaId);
		this.monsterGroupMaxSize = 1;
	}

	public int getFightAreaId() {
		return this.fightAreaId;
	}

	public int getMonsterGroupMaxSize() {
		return this.monsterGroupMaxSize;
	}
	
	// n'ayant pas le niveau du perso au moment de la création de l'objet FightOptions, 
	// cette méthode intervient plus tard
	public void setFightArea(int level) {
		// si l'aire de combat n'est pas fixée ou si le niveau minimal requis pour cette aire n'est pas atteint
		// on définit l'aire comme l'aire adaptée au niveau du perso selon le parcours d'xp
		if(this.fixedFightAreaId == 0 || fightAreasParameters.get(this.fixedFightAreaId)[0] > level) {
			for(int i = 0; i < xpPath.length; ++i)
				if(level < xpPath[i][1]) {
					this.fightAreaId = xpPath[i][0];
					this.fightAreaParameters = fightAreasParameters.get(this.fightAreaId);
					break;
				}
		}
		else {
			this.fightAreaId = this.fixedFightAreaId;
			this.fightAreaParameters = fightAreasParameters.get(fightAreaId);
		}
		for(int i = 1; i < this.fightAreaParameters.length; ++i)
			if(level < this.fightAreaParameters[i]) {
				this.monsterGroupMaxSize = i;
				return;
			}
		this.monsterGroupMaxSize = 8;
	}
}