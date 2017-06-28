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
}
