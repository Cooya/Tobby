package controller.modules;

import java.util.Vector;

import controller.CharacterState;
import controller.characters.Character;
import controller.pathfinding.Pathfinding;
import controller.pathfinding.Pathfinding.Direction;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;
import messages.UnhandledMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;

public class MovementAPI {
	public static final int ASTRUB_BANK_INSIDE_MAP_ID = 83887104;
	public static final int ASTRUB_BANK_OUTSIDE_MAP_ID = 84674566;
	public static final int ASTRUB_TAVERN_OUTSIDE_MAP_ID = 84675077;
	public static final int ASTRUB_TAVERN_INSIDE_MAP_ID = 83890176;
	public static final int ASTRUB_BID_HOUSE_MAP_ID = 84674565;
	public static final int ASTRUB_FECA_STATUE_MAP_ID = 84674054;
	public static final int INCARNAM_CELESTIAL_TEMPLE_FIRST_ROOM_MAP_ID = 153092354;
	public static final int INCARNAM_CELESTIAL_TEMPLE_SECOND_ROOM_MAP_ID = 152043521;
	public static final int INCARNAM_NPC_MAP_ID = 153880835;
	public static final int INCARNAM_FIRST_SUBAREA = 422;
	public static final int INCARNAM_LAST_SUBAREA = 450;
	
	private Character character;
	private Pathfinding pathfinding;
	
	public MovementAPI(Character character) {
		this.character = character;
		this.pathfinding = new Pathfinding();
	}
	
	// refresh des infos par le thread de traitement
	public void updatePosition(Map map, int cellId) {
		this.pathfinding.updatePosition(map, cellId);
	}

	// refresh des infos par le thread de traitement
	public void updatePosition(int cellId) {
		this.pathfinding.updatePosition(cellId);
	}
	
	 // changement de cellule
	public boolean moveTo(int targetId) {
		int currentCellId = this.character.infos.getCurrentCellId();
		if(currentCellId == targetId) { // déjà sur la cellule cible
			this.character.log.p("Already on the target cell id.");
			return true;
		}
		
		this.character.log.p("Moving from cell " + currentCellId + " to " + targetId + ".");
		
		Vector<Integer> path = pathfinding.getCellsPathTo(targetId);
		if(path == null)
			return false;
		
		this.character.log.p("Sending movement request.");
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.keyMovements = path;
		GMMRM.mapId = this.character.infos.getCurrentMap().id;
		this.character.net.send(GMMRM);
		
		int duration = this.pathfinding.getCellsPathDuration();
		this.character.log.p("Movement duration : " + duration + " ms.");
		try {
			Thread.sleep(duration); // on attend d'arriver à destination
		} catch(InterruptedException e) {
			this.character.interrupt();
			return false;
		}
		
		this.character.log.p("Target cell reached.");
		this.character.net.send(new UnhandledMessage("GameMapMovementConfirmMessage"));
		return true;
	}
	
	// déplacement vers une cible fixe ou non (booléen "dynamic")
	public void goTo(int mapId, boolean dynamic) {
		Map currentMap = this.character.infos.getCurrentMap();
		
		// si le perso est dans le temple céleste, il en sort
		if(mapIsInCelestialTemple(currentMap))
			goOutFromCelestialTemple();
		
		// si le perso est dans la banque d'Astrub, il en sort
		if(this.character.infos.getCurrentMap().id == ASTRUB_BANK_INSIDE_MAP_ID)
			goOutAstrubBank();
		
		// si le perso est dans la taverne d'Astrub, il en sort
		if(this.character.infos.getCurrentMap().id == ASTRUB_TAVERN_INSIDE_MAP_ID)
			goOutAstrubTavern();
		
		// si le perso est à Incarnam et qu'il doit aller à Astrub, il descend
		// si le perso est à Astrub et qu'il doit aller à Incarnam, il monte
		boolean isInIncarnam = mapIsInIncarnam(currentMap);
		if(mapIsInIncarnam(MapsCache.loadMap(mapId))) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		// si le perso est déjà sur la map cible, il ne fait rien
		if(currentMap.id == mapId) {
			this.character.log.p("Already on the target map.");
			return;
		}
		
		// la map cible est modifiée
		this.pathfinding.setTargetMap(mapId);
		this.character.log.p("Going from map " + currentMap.id + " to map " + mapId + ".");
		
		// le perso se dirige vers cette map cible (progressivement ou non)
		if(dynamic)
			changeMap(this.pathfinding.nextDirectionForReachTarget());
		else {
			Direction direction;
			while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
				changeMap(direction);
		}
	}
	
	// déplacement vers la plus proche map d'une aire
	public void goToArea(int areaId) {
		Map currentMap = this.character.infos.getCurrentMap();
		
		// si le perso est dans le temple céleste, il en sort
		if(mapIsInCelestialTemple(currentMap))
			goOutFromCelestialTemple();
		
		// si le perso est dans la banque d'Astrub, il en sort
		if(this.character.infos.getCurrentMap().id == ASTRUB_BANK_INSIDE_MAP_ID)
			goOutAstrubBank();
				
		// si le perso est dans la taverne d'Astrub, il en sort
		if(this.character.infos.getCurrentMap().id == ASTRUB_TAVERN_INSIDE_MAP_ID)
			goOutAstrubTavern();
		
		// si le perso est à Incarnam et qu'il doit aller à Astrub, il descend
		// si le perso est à Astrub et qu'il doit aller à Incarnam, il monte
		boolean isInIncarnam = mapIsInIncarnam(currentMap);
		if(areaIsInIncarnam(areaId)) {
			if(!isInIncarnam)
				goUpToIncarnam();
		}
		else {
			if(isInIncarnam)
				goDownToAstrub();
		}
		
		// si le perso est déjà sur l'aire, il ne fait rien
		if(this.character.infos.getCurrentMap().subareaId == areaId) {
			this.pathfinding.setArea(areaId); // pas besoin du calcul du chemin
			this.character.log.p("Already on the target area.");
			return;
		}
		
		// définition de l'aire cible
		this.pathfinding.setArea(areaId);
		this.character.log.p("Going from map " + currentMap.id + " to area " + areaId + ".");
		
		// le perso se dirige vers cette aire cible
		Direction direction;
		while((direction = this.pathfinding.nextDirectionForReachTarget()) != null)
			changeMap(direction);
	}
	
	// fonction surcouche réservée aux parcours d'aires
	public boolean changeMap() {
		return changeMap(this.pathfinding.nextDirectionInArea());
	}
	
	// déplace le personnage d'une map selon une direction
	public boolean changeMap(Direction direction) {
		if(direction == null || !moveTo(direction.outgoingCellId))
			return false;
		
		this.character.log.p("Moving to " + Map.directionToString(direction.direction) + " map.");
		
		int nextMapId = this.character.infos.getCurrentMap().getNeighbourMapFromDirection(direction.direction);
		this.character.log.p("Sending map changement request. Next map id : " + nextMapId + ".");
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.mapId = nextMapId;
		this.character.net.send(CMM);
			
		this.character.updateState(CharacterState.IS_LOADED, false); // chargement de map
		this.character.waitState(CharacterState.IS_LOADED);
		this.character.infos.incMapsTravelledCounter();
		return true;
	}
	
	// déplace le perso vers la map à l'intérieur de la banque d'Astrub
	public void goToInsideBank() {
		if(this.character.infos.getCurrentMap().id == ASTRUB_BANK_INSIDE_MAP_ID) // déjà dans la banque
			return;
		
		goTo(ASTRUB_BANK_OUTSIDE_MAP_ID, false);
		this.character.interaction.useInteractive(317, 465440, true); // porte de la banque
	}
	
	// déplace jusqu'à l'intérieur de la taverne d'Astrub
	public void goToInsideTavern() {
		if(this.character.infos.getCurrentMap().id == ASTRUB_TAVERN_INSIDE_MAP_ID) // déjà dans la taverne
			return;
		
		goTo(ASTRUB_TAVERN_OUTSIDE_MAP_ID, false);
		this.character.interaction.useInteractive(368, 465435, true); // porte de la taverne
	}
	
	// déplace le perso vers la map extérieure de la banque d'Astrub
	public void goToOutsideBank() {
		goTo(ASTRUB_BANK_OUTSIDE_MAP_ID, false);
	}
	
	// déplace le perso vers la map où se situe le hdv ressources d'Astrub
	public void goToBidHouse() {
		goTo(ASTRUB_BID_HOUSE_MAP_ID, false);
	}
	
	// fait sortir le perso de la banque d'Astrub
	private void goOutAstrubBank() {
		// on sort de la banque
		moveTo(396);
		
		// et on attend la fin du changement de map
		this.character.updateState(CharacterState.IS_LOADED, false);
		this.character.waitState(CharacterState.IS_LOADED);
	}
	
	// fait sortir le perso de la taverne d'Astrub
	private void goOutAstrubTavern() {
		// on sort de la taverne
		moveTo(366);
		
		// et on attend la fin du changement de map
		this.character.updateState(CharacterState.IS_LOADED, false);
		this.character.waitState(CharacterState.IS_LOADED);
	}
	
	// fait descendre le personnage à Astrub depuis Incarnam
	private void goDownToAstrub() {
		this.character.log.p("Going down to Astrub.");
		goTo(INCARNAM_NPC_MAP_ID, false);
		
		// on parle au pnj
		this.character.interaction.talkToNpc(2889, 3, true);
		
		// on sélectionne la première réponse
		this.character.interaction.answerToNpc(25209);
		
		try {
			Thread.sleep(1000); // on attend la seconde question
		} catch (InterruptedException e) {
			this.character.interrupt();
			return;
		}
		
		// on sélectionne la seconde réponse
		this.character.interaction.answerToNpc(25207);
		
		// et on attend la fin du changement de map
		this.character.updateState(CharacterState.IS_LOADED, false);
		this.character.waitState(CharacterState.IS_LOADED);
	}
	
	// fait monter le personnage à Incarnam depuis Astrub
	private void goUpToIncarnam() {
		this.character.log.p("Going up to Incarnam.");
		goTo(ASTRUB_FECA_STATUE_MAP_ID, false);
		
		// utilisation de la statue Féca
		this.character.interaction.useInteractive(375, 489378, true);
	}
	
	// fait sortir le personnage du temple céleste d'Incarnam
	private void goOutFromCelestialTemple() {
		this.character.log.p("Going out from celestial temple of Incarnam.");
		if(this.character.infos.getCurrentMap().id == INCARNAM_CELESTIAL_TEMPLE_FIRST_ROOM_MAP_ID)
			this.character.interaction.useInteractive(396, 489318, true); // escalier
		changeMap(new Direction(Map.RIGHT, 531)); // on sort du temple
	}
	
	// vérifie si la map donnée est à Incarnam ou non
	private static boolean mapIsInIncarnam(Map map) {
		return (map.subareaId >= INCARNAM_FIRST_SUBAREA && map.subareaId <= INCARNAM_LAST_SUBAREA);
	}
	
	// vérifie si la map donnée est dans le temple céleste ou non
	private static boolean mapIsInCelestialTemple(Map map) {
		return map.id == INCARNAM_CELESTIAL_TEMPLE_FIRST_ROOM_MAP_ID || map.id == INCARNAM_CELESTIAL_TEMPLE_SECOND_ROOM_MAP_ID;
	}
	
	// vérifie si l'aire donnée est à Incarnam ou non 
	private static boolean areaIsInIncarnam(int areaId) {
		return (areaId >= INCARNAM_FIRST_SUBAREA && areaId <= INCARNAM_LAST_SUBAREA);
	}
}