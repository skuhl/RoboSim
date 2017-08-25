package undo;

import geom.Fixture;
import geom.Part;
import global.Fields;

/**
 * Defines undo states for a part's fixture reference.
 * 
 * @author Joshua Hooker
 */
public class PartUndoParent extends WOUndoState {
	
	/**
	 * A reference to a fixture in the active scenario.
	 */
	private Fixture prevParent;
	
	public PartUndoParent(int groupNum, Part partRef) {
		super(groupNum, partRef);
		this.prevParent = partRef.getParent();
	}
	
	public PartUndoParent(int groupNum, Part partRef, Fixture prevParent) {
		super(groupNum, partRef);
		this.prevParent = prevParent;
	}
	
	@Override
	public void undo() {
		/* Reset the part's fixture references to the fixture defined by this
		 * undo state */
		if (woRef instanceof Part) {
			Fields.setWODependency(prevParent, (Part)woRef);
		}
	}

}
