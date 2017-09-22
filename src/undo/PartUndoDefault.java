package undo;

import geom.CoordinateSystem;
import geom.Part;
import geom.RMatrix;
import processing.core.PVector;

/**
 * Defines undo states for a part's default position or orientation.
 * 
 * @author Joshua Hooker
 */
public class PartUndoDefault extends WOUndoState {
	
	/**
	 * A previous default position and orientation of the part.
	 */
	private CoordinateSystem prevCoord;
	
	public PartUndoDefault(int groupNum, Part ref) {
		super(groupNum, ref);
		
		PVector defPosition = ref.getDefaultCenter().copy();
		RMatrix defOrientation = ref.getDefaultOrientation().copy();
		prevCoord = new CoordinateSystem(defPosition, defOrientation);
	}
	
	public PartUndoDefault(int groupNum, Part ref, CoordinateSystem prevCoord) {
		super(groupNum, ref);
		this.prevCoord = prevCoord;
	}
	
	@Override
	public void undo() {
		/* Reset the default position and orientation of the part to the
		 * previous coordinate state */
		if (woRef instanceof Part) {
			Part partRef = (Part)woRef;
			partRef.setDefaultCenter(prevCoord.getOrigin());
			partRef.setDefaultOrientation(prevCoord.getAxes());
		}
	}

}
