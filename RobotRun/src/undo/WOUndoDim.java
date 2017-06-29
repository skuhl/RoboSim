package undo;

import geom.ComplexShape;
import geom.DimType;
import geom.Part;
import geom.RBox;
import geom.RCylinder;
import geom.RShape;
import geom.WorldObject;

/**
 * Defines undo states for a world object's dimensions.
 * 
 * @author Joshua Hooker
 */
public class WOUndoDim extends WOUndoState {
	
	/**
	 * A set of dimensions for a previous state of the shape.
	 */
	private Object[] dims;
	
	public WOUndoDim(WorldObject ref) {
		super(ref);
		// Sets the dims list based on the world object's shape
		RShape form = ref.getForm();
		
		if (form instanceof RBox) {
			dims = new Object[] {
					new Float(form.getDim(DimType.LENGTH)),
					new Float(form.getDim(DimType.HEIGHT)),
					new Float(form.getDim(DimType.WIDTH)),
			};
			
		} else if (form instanceof RCylinder) {
			dims = new Object[] {
					new Float(form.getDim(DimType.RADIUS)),
					new Float(form.getDim(DimType.HEIGHT)),
			};
			
		} else if (form instanceof ComplexShape) {
			dims = new Object[] {
					new Float(form.getDim(DimType.SCALE))
			};
		}
	}
	
	@Override
	public void undo() {
		/* Reset the dimensions of world object's shape to the values
		 * defined by this undo state */
		RShape form = woRef.getForm();
		
		if (form instanceof RBox) {
			form.setDim((Float)dims[0], DimType.LENGTH);
			form.setDim((Float)dims[1], DimType.HEIGHT);
			form.setDim((Float)dims[2], DimType.WIDTH);
			
		} else if (form instanceof RCylinder) {
			form.setDim((Float)dims[0], DimType.RADIUS);
			form.setDim((Float)dims[1], DimType.HEIGHT);
			
		} else if (form instanceof ComplexShape) {
			form.setDim((Float)dims[0], DimType.SCALE);
		}
		
		if (woRef instanceof Part) {
			// Update the bounding box dimension of a part as well
			((Part) woRef).updateOBBDims();
		}
	}

}
