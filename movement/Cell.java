package movement;

public class Cell {
	protected int x;
	protected int y;
	protected boolean visited;
	protected int speed;
	@SuppressWarnings("unused")
	private boolean walkableDuringFight;
	private boolean walkableDuringRP;
	
	public Cell(int x, int y, int speed, boolean nonWalkableFight, boolean nonWalkableRP) {
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.walkableDuringFight = !nonWalkableFight;
		this.walkableDuringRP = !nonWalkableRP;
		this.visited = false;
	}
	
	public boolean equals(Cell cell) {
		return this.x == cell.x && this.y == cell.y;
	}
	
	public boolean check() { // to do
		boolean isVisited = this.visited;
		this.visited = true;
		return !isVisited && walkableDuringRP;
	}
	
	public String toString() {
		return "[" + this.x + ", " + this.y + "]";
	}
}
