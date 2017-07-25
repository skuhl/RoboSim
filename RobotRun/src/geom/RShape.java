package geom;

import global.Fields;
import processing.core.PGraphics;

/**
 * A simple class that defines the stroke and fill color for a shape
 * along with some methods necessary for a shape.
 */
public abstract class RShape implements Cloneable {
	private Integer fillCVal;
	private Integer strokeCVal;

	public RShape() {
		fillCVal = Fields.BLACK;
		strokeCVal = Fields.WHITE;
	}

	public RShape(Integer fill, Integer strokeVal) {
		fillCVal = fill;
		strokeCVal = strokeVal;
	}
	
	@Override
	public abstract RShape clone();

	/**
	 * Draws the shape with its stroke and outline values.
	 * 
	 * @param g	the graphics used to render this shape
	 */
	public abstract void draw(PGraphics g);
	
	/**
	 * Returns the value of the given dimension associated with
	 * this shape. If no such dimension exists, then -1 should
	 * be returned.
	 * 
	 * @param dim  The dimension of which to get the value
	 * @returning  The value of that dimension, or -1, if no
	 *             such dimension exists
	 */
	public abstract float getDim(DimType dim);
	
	public abstract float[] getDimArray();
	
	/**
	 * Returns the lower bound for the specified dimension, if one exists. If
	 * no lower bound exists for this shape, then -1 is returned.
	 * 
	 * @param dim	The dimension for which to get the lower bound
	 * @return		The lower bound for the given dimension or -1 if no lower
	 * 				bound is specified
	 */
	public float getDimLBound(DimType dim) {
		switch (dim) {
		case LENGTH:
		case HEIGHT:
		case WIDTH:
			return 10f;
		case RADIUS:
			return 5f;
		// Scale bounds vary from model to model
		default:
			return -1f;
		}
	}
	
	/**
	 * Returns the upper bound for the specified dimension, if one exists. If
	 * no upper bound exists for this shape, then -1 is returned.
	 * 
	 * @param dim	The dimension for which to get the upper bound
	 * @return		The upper bound for the given dimension or -1 if no upper
	 * 				bound is specified
	 */
	public float getDimUBound(DimType dim) {
		switch (dim) {
		case LENGTH:
		case HEIGHT:
		case WIDTH:
			return 1000f;
		case RADIUS:
			return 500f;
		// Scale bounds vary from model to model
		default:
			return -1f;
		}
	}
	
	public Integer getFillValue() { return fillCVal; }
	public Integer getStrokeValue() { return strokeCVal; }
	
	/**
	 * Sets the value of the given dimension associated with
	 * this shape, if that dimension exists.
	 * 
	 * @param newVal  The value to which to set the dimension
	 * @param dim     The dimension of  which ro set the value
	 */
	public abstract void setDim(Float newVal, DimType dim);
	public void setFillValue(Integer newVal) { fillCVal = newVal; }
	public void setStrokeValue(Integer newVal) { strokeCVal = newVal; }
	
	/**
	 * Applies the shape's stroke and outline colors to the given graphics.
	 * 
	 * @param g	The graphics to which to apply this shape's style
	 */
	protected void applyStyle(PGraphics g) {
		
		if (fillCVal != null) {
			g.fill(fillCVal);
			
		} else {
			g.noFill();
		}
		
		if (strokeCVal != null) {
			g.stroke(strokeCVal);
			
		} else {
			g.noStroke();
		}	
	}
}