package undo;

import geom.Fixture;
import geom.Part;

/**
 * Defines undo states for a part's fixture reference.
 * 
 * @author Joshua Hooker
 */
public class PartUndoFixRef extends WOUndoState {
	
	/**
	 * A reference to a fixture in the active scenario.
	 */
	private Fixture fixtureRef;
	
	public PartUndoFixRef(Part partRef) {
		super(partRef);
		this.fixtureRef = partRef.getFixtureRef();
	}
	
	public PartUndoFixRef(Part partRef, Fixture fixtureRef) {
		super(partRef);
		this.fixtureRef = fixtureRef;
	}
	
	@Override
	public void undo() {
		/* Reset the part's fixture references to the fixture defined by this
		 * undo state */
		if (woRef instanceof Part) {
			((Part)woRef).setFixtureRef(fixtureRef);
		}
	}

}
