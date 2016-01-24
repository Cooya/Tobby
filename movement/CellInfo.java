package movement;

import java.util.Vector;

public class CellInfo {
    public double heuristic;
    public Vector<Object> parent;
    public boolean opened;
    public boolean closed;
    public int movementCost;

    public CellInfo(double heuristic, Vector<Object> parent, boolean opened, boolean closed) {
        this.heuristic = heuristic;
        this.parent = parent;
        this.opened = opened;
        this.closed = closed;
    }
}
