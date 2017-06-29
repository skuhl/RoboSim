package undo;

import geom.WorldObject;

/**
 * Defines a previous state of a world object, to which the world object can be
 * reverted.
 * 
 * @author Joshua Hooker
 */
public abstract class WOUndoState {
	
	/**
	 * A reference to a world object in the active scenario.
	 */
	protected WorldObject woRef;
	
	public WOUndoState(WorldObject ref) {
		woRef = ref;
	}
	
	/**
	 * Reverts the world object related to this undo state to the previous
	 * state defined by this object.
	 */
	public abstract void undo();
}
