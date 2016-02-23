package controller.pathfinding;

import gamedata.d2p.Cell;
import gamedata.d2p.ankama.Map;

import java.util.Vector;

public class MapsAnalyser {
	public static Vector<Vector<Cell>> getZones(Map map) {
		CellsPathfinder pathfinder = new CellsPathfinder(map);
		Vector<Vector<Cell>> zones = new Vector<Vector<Cell>>();
		@SuppressWarnings("unchecked")
		Vector<Cell> cells = (Vector<Cell>) map.cells.clone();
		Vector<Cell> buffer = new Vector<Cell>();
		Cell currentCell;
		Vector<Cell> neighbours;
		Vector<Cell> currentZone;
		
		// tant qu'il reste encore des cellules à traiter parmi toutes les cellules de la map
		while(cells.size() > 0) {
			currentCell = cells.firstElement(); // on pourrait aussi faire un pop
			
			// si c'est une cellule obstacle, on ne la considère pas
			if(!currentCell.isAccessibleDuringRP()) {
				cells.remove(currentCell);
				continue;
			}
			
			// on crée une nouvelle zone que l'on va remplir
			currentZone = new Vector<Cell>();
			
			buffer.add(currentCell);
			while(buffer.size() > 0) { // tant qu'il reste des cellules dans le buffer, on les traite
				currentCell = buffer.firstElement();
				
				//System.out.print(currentCell.id + ", neighbours : ");
				
				// on ajoute cette cellule dans la zone courante puis on la retire du buffer et des cellules de la map
				currentZone.add(currentCell);
				buffer.remove(currentCell);
				cells.remove(currentCell);
				
				// on ajoute les cellules voisines dans le buffer de cellules à traiter
				neighbours = pathfinder.getNeighboursCell(currentCell.id);
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
				zones.add(currentZone);
		}
		return zones;
	}
}
