package undo;

import geom.CoordinateSystem;
import geom.RMatrix;
import geom.WorldObject;
import processing.core.PVector;

/**
 * Defines undo states for a world object's position and orientation.
 * 
 * @author Joshua Hooker
 */
public class WOUndoCurrent extends WOUndoState {
	
	/**
	 * A previous position and orientation of the world object.
	 */
	private CoordinateSystem prevCoord;
	
	public WOUndoCurrent(int groupNum, WorldObject ref) {
		super(groupNum, ref);
		
		PVector defPosition = ref.getLocalCenter().copy();
		RMatrix defOrientation = ref.getLocalOrientation().copy();
		prevCoord = new CoordinateSystem(defPosition, defOrientation);
	}
	
	public WOUndoCurrent(int groupNum, WorldObject ref,
			CoordinateSystem prevCoord) {
		
		super(groupNum, ref);
		this.prevCoord = prevCoord;
	}
	
	@Override
	public void undo() {
		/* Reset the position and orientation of the world object to the
		 * previous coordinate state */
		woRef.setLocalCoordinates(prevCoord.getOrigin(), prevCoord.getAxes());
	}
}
