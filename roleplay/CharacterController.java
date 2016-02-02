package roleplay;

import java.util.Vector;

import main.Main;
import messages.EmptyMessage;
import messages.currentmap.ChangeMapMessage;
import messages.currentmap.GameMapMovementRequestMessage;
import messages.roleplay.GameRolePlayAttackMonsterRequestMessage;
import roleplay.movement.D2pReader;
import roleplay.movement.Pathfinder;
import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapMovementAdapter;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;
import roleplay.paths.PathsManager;

public class CharacterController {
	private String login;
	private String password;
	private String characterName;
	private double characterId;
	private int currentCellId;
	private int currentDirection;
	private Map currentMap;
	private String currentPathName;
	
	public CharacterController(String login, String password) {
		this.login = login;
		this.password = password;
	}
	
	public String getLogin() {
		return this.login;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getCharacterName() {
		return this.characterName;
	}
	
	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}
	
	public double getCharacterId() {
		return this.characterId;
	}
	
	public void setCharacterId(double characterId) {
		this.characterId = characterId;
	}
	
	public int getCurrentCellId() {
		return this.currentCellId;
	}
	
	public void setCurrentCellId(int cellId) {
		this.currentCellId = cellId;
	}
	
	public int getCurrentDirection() {
		return this.currentDirection;
	}
	
	public void setCurrentDirection(int direction) {
		this.currentDirection = direction;
	}
	
	public int getCurrentMapId() {
		return this.currentMap.id;
	}
	
	public void setCurrentMap(int mapId) {
		this.currentMap = new Map(D2pReader.getBinaryMap(mapId));
		Pathfinder.initMap(this.currentMap);
	}
	
	public String getCurrentPathName() {
		return this.currentPathName;
	}
	
	public void moveTo(int cellId) {
		MapPoint src = MapPoint.fromCellId(this.currentCellId);
		MapPoint dest = MapPoint.fromCellId(cellId);
		
		MovementPath path = Pathfinder.compute(this.currentCellId, cellId);
		path.setStart(src);
		path.setEnd(dest);
		
		Vector<Integer> vector = MapMovementAdapter.getServerMovement(path);
		GameMapMovementRequestMessage GMMRM = new GameMapMovementRequestMessage();
		GMMRM.serialize(vector, this.currentMap.id);
		Main.sendMessage(GMMRM);
		
		Pathfinder.printPath();
		for(int i : vector)
			System.out.print(i + " ");
		System.out.println("\n");
		
		try {
			Thread.sleep(Pathfinder.getPathTime());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		Main.sendMessage(EM);
		
		this.currentCellId = cellId;
	}
	
	public void changeMap(int direction) {
		moveTo(Pathfinder.getChangementMapCell(direction));
		ChangeMapMessage CMM = new ChangeMapMessage();
		CMM.serialize(this.currentMap.getNeighbourMapFromDirection(direction));
		Main.sendMessage(CMM);
	}
	
	public void lunchFight(int position,double id){
		this.moveTo(position);
		GameRolePlayAttackMonsterRequestMessage GRPAMRM=new GameRolePlayAttackMonsterRequestMessage();
		GRPAMRM.serialize(id);
		Main.sendMessage(GRPAMRM);
	}
	
	public void runPath(String pathName) {
		this.currentPathName = pathName;
		if(PathsManager.getCurrentMapId(pathName) != this.currentMap.id)
			throw new Error("Impossible to run this path, invalid character position.");
		changeMap(PathsManager.nextMap(pathName));
	}
}
