package roleplay;

import java.util.Vector;

import main.Main;
import messages.EmptyMessage;
import messages.currentmap.GameMapMovementRequestMessage;
import roleplay.movement.D2pReader;
import roleplay.movement.Pathfinder;
import roleplay.movement.ankama.Map;
import roleplay.movement.ankama.MapMovementAdapter;
import roleplay.movement.ankama.MapPoint;
import roleplay.movement.ankama.MovementPath;

public class CharacterController {
	private String login;
	private String password;
	private String characterName;
	private double characterId;
	private int currentCellId;
	private int currentDirection;
	private Map currentMap;
	
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
		
		try {
			Thread.sleep(Pathfinder.getPathTime());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		EmptyMessage EM = new EmptyMessage("GameMapMovementConfirmMessage");
		Main.sendMessage(EM);
	}
}
