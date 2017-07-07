package undo;

import core.Scenario;
import geom.WorldObject;

/**
 * Defines undo states for deleting a world object from a scenario.
 * 
 * @author Joshua Hooker
 */
public class WOUndoDelete extends WOUndoState {
	
	/**
	 * A reference to the scenario, to which the given world object belongs.
	 */
	private Scenario parent;
	
	public WOUndoDelete(WorldObject wo, Scenario parent) {
		super(wo);
		this.parent = parent;
	}
	
	@Override
	public void undo() {
		parent.addWorldObject(woRef);
	}

}
