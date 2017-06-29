package geom;

public class WOUndoCoord extends WOUndoState {
	
	private CoordinateSystem prevCoord;
	
	public WOUndoCoord(WorldObject ref, CoordinateSystem prevCoord) {
		super(ref);
		this.prevCoord = prevCoord;
	}
	
	public void undo() {
		
	}
}
