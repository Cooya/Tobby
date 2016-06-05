package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

class MapZones {
	private Vector<Vector<Cell>> zones;
	
	protected MapZones(Map map) {
		retrieveZones(map);
	}
	
	protected boolean inSameZone(int mapId, int cellId1, int cellId2) {
		return getZone(cellId1) == getZone(cellId2);		
	}
	
	protected Vector<Cell> getZone(int cellId) {
		Vector<Cell> currentZone = null;
		boolean found = false;
		for(Vector<Cell> zone : this.zones)
			if(!found)
				for(Cell cell : zone)
					if(cell.id == cellId) {
						currentZone = zone;
						found = true;
						break;
					}
		return currentZone;
	}
	
	private void retrieveZones(Map map) {
		this.zones = new Vector<Vector<Cell>>();
		LightMapNode mapNode = new LightMapNode(map);
		Vector<Cell> cells = new Vector<Cell>(map.cells.length);
		for(Cell cell : map.cells)
			cells.add(cell);
		Vector<Cell> buffer = new Vector<Cell>();
		Cell currentCell;
		Vector<Cell> neighbours;
		Vector<Cell> currentZone;
		
		// tant qu'il reste encore des cellules � traiter parmi toutes les cellules de la map
		while(cells.size() > 0) {
			currentCell = cells.firstElement(); // on pourrait aussi faire un pop
			
			// si c'est une cellule obstacle, on ne la consid�re pas
			if(!currentCell.isAccessibleDuringRP()) {
				cells.remove(currentCell);
				continue;
			}
			
			// on cr�e une nouvelle zone que l'on va remplir
			currentZone = new Vector<Cell>();
			
			buffer.add(currentCell);
			while(buffer.size() > 0) { // tant qu'il reste des cellules dans le buffer, on les traite
				currentCell = buffer.firstElement();
				
				//System.out.print(currentCell.id + ", neighbours : ");
				
				// on ajoute cette cellule dans la zone courante puis on la retire du buffer et des cellules de la map
				currentZone.add(currentCell);
				buffer.remove(currentCell);
				cells.remove(currentCell);
				
				// on ajoute les cellules voisines dans le buffer de cellules � traiter
				neighbours = mapNode.getNeighboursCell(currentCell.id);
				for(Cell neighbourCell : neighbours)
					if(!currentZone.contains(neighbourCell) && !buffer.contains(neighbourCell) && neighbourCell.isAccessibleDuringRP()) {
						buffer.add(neighbourCell);
						//System.out.print(neighbourCell.id + " ");
					}
				//System.out.println();
			}
			//System.out.println();
			//System.out.println();
			
			if(currentZone.size() > 0)
				this.zones.add(currentZone);
		}
	}
}