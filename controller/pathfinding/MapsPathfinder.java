package controller.pathfinding;

import gamedata.d2o.modules.MapPosition;
import gamedata.d2p.Cell;
import gamedata.d2p.MapsCache;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

import main.FatalError;

class MapsPathfinder extends Pathfinder {
	private int startCellId;
	
	protected MapsPathfinder(int startCellId) {
		this.startCellId = startCellId;
	}
	
	@Override
	protected PathNode getNodeFromId(int mapId) {
		return new MapNode(mapId, -1, null, this.startCellId);
	}
	
	@Override
	protected PathNode nodeIsInList(PathNode node, Vector<PathNode> list) {
		MapNode mn = (MapNode) node;
		for(PathNode pn : list)
			if(((MapNode) pn).map == mn.map) // on peut utiliser la r�f�rence ici
				return pn;
		return null;
	}

	@Override
	protected Vector<PathNode> getNeighbourNodes(PathNode node) {
		Vector<PathNode> neighbours = new Vector<PathNode>();
		Map map;
		for(int direction = 0; direction < 8; direction += 2) {
			map = getMapFromId(((MapNode) node).map.getNeighbourMapFromDirection(direction));
			if(map != null)
				neighbours.add(new MapNode(map, direction, node, this.startCellId));
		}
		return neighbours;	
	}
	
	private Map getMapFromId(int mapId) {
		MapPosition mp = MapPosition.getMapPositionById(mapId);
		if(mp == null)
			return null;
		return MapsCache.loadMap(mapId);
	}
	
	private class MapNode extends LightMapNode {
		private Vector<Cell> outgoingPossibilities; // au maximum 4 (pour chaque direction)
		
		protected MapNode(Map map, int lastDirection, PathNode parent, int incomingCellId) {
			super(map.id, lastDirection, parent);
			this.map = map;
			this.zones = new MapZones(map);
			this.outgoingPossibilities = new Vector<Cell>();
			MapPosition mp = MapPosition.getMapPositionById(map.id);
			this.x = mp.posX;
			this.y = mp.posY;
			this.outgoingCellId = -1;
			if(parent != null)
				setParentOugoingCell();
			else { // cellule de d�part et d'arriv�e
				if(incomingCellId == -1) // cr�ation de path � distance
					this.currentZone = this.zones.getZone(getFirstAccessibleCellId()); // pas fiable � 100%
				else
					this.currentZone = this.zones.getZone(incomingCellId); // s'applique aussi au noeud de destination, mais cela ne change rien
			}
			this.isAccessible = this.currentZone != null;
			setHeuristic(destNode);
		}
		
		protected MapNode(int mapId, int lastDirection, PathNode parent, int incomingCellId) {
			this(MapsCache.loadMap(mapId), lastDirection, parent, incomingCellId);
		}
		
		// ajoute au vecteur des possibilit�s de sortie de la map parente une cellule de sortie pour 
		// acc�der � la map fille (et d�termine ainsi la zone courante de cette derni�re)
		private void setParentOugoingCell() {
			Cell cell;
			Vector<Cell> parentCurrentZone = ((MapNode) this.parent).currentZone;
			if(parentCurrentZone == null)
				throw new FatalError("Invalid parent current cell.");
			for(Cell parentCell : parentCurrentZone)
				if(parentCell.allowsChangementMap() && isOutgoingPossibility(parentCell.id, this.lastDirection) && !isForbiddenPossibility(parentCell.id)) {
					cell = getNewCellAfterMapChangement(parentCell.id, this.lastDirection);
					if(cell.isAccessibleDuringRP()) {
						//System.out.println(toString() + " " + Pathfinder.directionToString(this.lastDirection) + " " + directionCellId + " " + cell);
						this.currentZone = this.zones.getZone(cell.id);
						((MapNode) this.parent).outgoingPossibilities.add(parentCell);
						return;
					}
				}
		}
		
		// retourne l'id de la premi�re cellule accessible de la map pour les noeuds sans parent
		private int getFirstAccessibleCellId() {
			for(Cell cell : this.map.cells)
				if(cell.isAccessibleDuringRP())
					return cell.id;
			throw new FatalError("Map without available cell ! Impossible !");
		}
		
		// d�termine la cellule d'arriv�e apr�s un changement de map
		private Cell getNewCellAfterMapChangement(int srcId, int direction) {
			switch(direction) {
				case Map.RIGHT : return this.map.cells.get(srcId + 1 - (Map.WIDTH - 1));
				case Map.LEFT : return this.map.cells.get(srcId - 1 + (Map.WIDTH - 1));
				case Map.UP : return this.map.cells.get((srcId - Map.WIDTH * 2) + 560);
				case Map.DOWN : return this.map.cells.get((srcId + Map.WIDTH * 2) - 560);
			}
			throw new FatalError("Invalid direction for changing map.");
		}
		
		// d�termine la cellule de sortie de ce noeud
		@Override
		protected void setNode() {
			for(Cell cell : this.outgoingPossibilities)
				if(isOutgoingPossibility(cell.id, this.direction)) {
					this.outgoingCellId = getOutgoingCellId(this.direction);
					break;
				}
		}
		
		@Override
		protected int getCrossingDuration(boolean mode) {
			return 1;
		}
		
		@Override
		public String toString() {
			if(this.direction != -1)
				return this.id + " [" + this.x + ", " + this.y + "] " + Map.directionToString(this.direction) + " " + this.outgoingCellId;
			return this.id + " [" + this.x + ", " + this.y + "]";
		}
	}
}