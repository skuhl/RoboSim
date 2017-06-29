package geom;

/**
 * TODO comment this
 * 
 * @author Joshua Hooker
 */
public abstract class WOUndoState {
	
	/**
	 * A reference to the altered world object.
	 */
	private WorldObject woRef;
	
	public WOUndoState(WorldObject ref) {
		woRef = ref;
	}
	
	public WorldObject getWORef() {
		return woRef;
	}
	
	/**
	 * Reverts the change defined by this undo state.
	 */
	public abstract void undo();
}
