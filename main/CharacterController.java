package main;

import java.util.Vector;

import messages.EmptyMessage;
import messages.character.EmotePlayRequestMessage;
import messages.context.ChangeMapMessage;
import messages.context.GameMapMovementRequestMessage;
import messages.context.GameRolePlayAttackMonsterRequestMessage;
import messages.fight.GameActionFightCastRequestMessage;
import messages.fight.GameFightReadyMessage;
import messages.fight.GameFightTurnFinishMessage;
import roleplay.currentmap.GameRolePlayGroupMonsterInformations;
import roleplay.d2o.modules.MapPosition;
import roleplay.d2p.MapsCache;
import roleplay.d2p.ankama.Map;
import roleplay.d2p.ankama.MapPoint;
import roleplay.d2p.ankama.MovementPath;
import roleplay.fight.GameFightFighterInformations;
import roleplay.pathfinding.CellsPathfinder;
import roleplay.pathfinding.Path;
import roleplay.pathfinding.Pathfinder;
import utilities.Log;

public class CharacterController extends Thread {
	private Instance instance;
	public String login;
	public String password;
	public int serverId;
	public String characterName;
	public double characterId;
	public int currentCellId;
	public int currentDirection;
	public Map currentMap;
	public String currentPathName;
	public boolean isAccessible;
	public CellsPathfinder pathfinder;
	public RoleplayContext rcontext;
	public FightContext fcontext;
	public int kamasNumber;

	public CharacterController(Instance instance, String login, String password, int serverId) {
		this.instance = instance;
		this.login = login;
		this.password = password;
		this.serverId = serverId;
		this.isAccessible = false;
		this.rcontext = new RoleplayContext(this);
		this.fcontext = new FightContext(this);
	}

	public void setCurrentMap(int mapId) {
		this.currentMap = MapsCache.loadMap(mapId);
		this.pathfinder = new CellsPathfinder(this.currentMap);
	}

	public synchronized void makeCharacterAccessible() {
		this.isAccessible = true;
		notify();
	}

	public synchronized void makeCharacterInaccessible() {
		this.isAccessible = false;
	}

	public synchronized void waitCharacterAccessibility() {
		if(!this.isAccessible)
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void moveTo(int cellId, boolean changeMap) {
		waitCharacterAccessibility();

		if(this.currentCellId == cellId) // déjà sur la cellule cible
			return;

		pathfinder = new CellsPathfinder(this.currentMap);
		Path path = pathfinder.compute(this.currentCellId, cellId);
		MovementPath mvPath = CellsPathfinder.movementPathFromArray(path.toVector());
		mvPath.setStart(MapPoint.fromCellId(this.currentCellId));
		mvPath.setEnd(MapPoint.fromCellId(cellId));

		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(mvPath.getServerMovement(), this.currentMap.id, instance.getInstanceId());
		instance.outPush(GMMRM);

		try {
			Thread.sleep(path.getCrossingDuration());
		} catch(Exception e) {
			e.printStackTrace();
		}

		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		instance.outPush(EM);

		this.currentCellId = cellId;
	}

	public void moveTo(int x, int y, boolean changeMap) {
		Vector<Integer> mapIds = MapPosition.getMapIdByCoord(x, y);
		if(mapIds.size() == 0)
			throw new Error("Invalid map coords.");
		moveTo(mapIds.get(0), changeMap);
	}

	public void changeMap(int direction) {
		waitCharacterAccessibility();

		Log.p("Move to " + Pathfinder.directionToString(direction) + " map.");

		moveTo(pathfinder.getChangementMapCell(direction), true);
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(this.currentMap.getNeighbourMapFromDirection(direction));
		instance.outPush(CMM);

		this.isAccessible = false; // on attend la fin du changement de map
	}

	public boolean launchFight(int position, double id) {
		moveTo(position, false);
		if(checkCharacterOverMonster()){
			GameRolePlayAttackMonsterRequestMessage GRPAMRM = new GameRolePlayAttackMonsterRequestMessage();
			GRPAMRM.serialize(id);
			instance.outPush(GRPAMRM);
			return true;
		}
		return false;
	}

	private boolean checkCharacterOverMonster() {
		for(int cell : rcontext.getCellIdsTakenByMonsters())
			if(cell==this.currentCellId)
				return true;
		return false;
	}

	
	
	
	public void run() {
		while(true){
			try {
				Thread.sleep(2000);
				waitCharacterAccessibility();
				if(!fcontext.fight){
					Thread.sleep(5000);
					checkRegenLife();  //regen
					System.out.println("Recherche de combat");
					tryLaunchFight();
				}
				else{
					if(fcontext.turn && !fcontext.inAction){
						if(fcontext.selfInfo.stats.actionPoints<4 || fcontext.skip==fcontext.nbMonstersAlive){
							endTurn();
						}
						else{
							tryToAttackMonsters();
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}





	public void checkRegenLife(){
		int regen=fcontext.lifeToRegen();
		if(regen>0){
			EmotePlayRequestMessage EPRM=new EmotePlayRequestMessage();
			EPRM.serialize((byte) 1);
			instance.outPush(EPRM);
			try {
				Thread.sleep((regen/3)*1000);
			} catch (InterruptedException e) {
			}
		}
	}


	public void tryLaunchFight(){
		int nbMonsters=rcontext.getMonsters().size();
		if(nbMonsters!=0){
			GameRolePlayGroupMonsterInformations actor=rcontext.getMonsters().get((int)(Math.random()*nbMonsters));
			if(launchFight(actor.disposition.cellId,actor.contextualId)){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				GameFightReadyMessage GFRM=new GameFightReadyMessage();
				GFRM.serialize();
				instance.outPush(GFRM);
			}
		}
	}

	public void endTurn(){
		GameFightTurnFinishMessage GFTFM=new GameFightTurnFinishMessage();
		GFTFM.serialize();
		instance.outPush(GFTFM);
		fcontext.skip=0;
	}

	public void tryToAttackMonsters(){
		System.out.println(fcontext.nbMonstersAlive+"||"+fcontext.skip);
		GameFightFighterInformations fighter=fcontext.getAliveMonsters().get(fcontext.skip);
		GameActionFightCastRequestMessage GAFCRM=new GameActionFightCastRequestMessage();
		GAFCRM.serialize(161, (short) fighter.disposition.cellId);      //A changer selon la classe
		instance.outPush(GAFCRM);
		System.out.println("Début de l'action");
		fcontext.inAction=true;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		fcontext.inAction=false;
		fcontext.skip++;
	}


}