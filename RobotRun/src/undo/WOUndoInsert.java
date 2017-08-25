package undo;

import geom.Scenario;
import geom.WorldObject;

/**
 * Defines the action of adding a world object to a scenario, which can be
 * reverted.
 * 
 * @author Joshua Hooker
 */
public class WOUndoInsert extends WOUndoState {
	
	/**
	 * A reference to the scenario, to which the given world object belongs.
	 */
	private Scenario parentRef;
	
	public WOUndoInsert(int groupNum, WorldObject woRef, Scenario parentRef) {
		super(groupNum, woRef);
		this.parentRef = parentRef;
	}
	
	@Override
	public void undo() {
		// Remove the object inserted into the scenario
		parentRef.removeWorldObject(woRef);
	}

}
