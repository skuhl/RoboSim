package robot;

import geom.RShape;

/**
 * Defines a shape to be drawn with a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GDrawRShape implements DrawAction {
	
	public final RShape shape;
	
	public GDrawRShape(RShape shape) {
		this.shape = shape;
	}
}
