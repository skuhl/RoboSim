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
	 * The value used to group multiple undo states together. When placed in
	 * the undo stack, adjacent undo states with the same group value are
	 * considered as a group when the user triggers an undo action.
	 */
	private int groupNum;
	
	/**
	 * A reference to a world object in the active scenario.
	 */
	protected WorldObject woRef;
	
	public WOUndoState(int groupNum, WorldObject ref) {
		this.groupNum = groupNum;
		woRef = ref;
	}
	
	/**
	 * Returns the group number of this undo state.
	 * 
	 * @return	This undo state's group number
	 */
	public int getGroupNum() {
		return groupNum;
	}
	
	/**
	 * Reverts the world object related to this undo state to the previous
	 * state defined by this object.
	 */
	public abstract void undo();
}
