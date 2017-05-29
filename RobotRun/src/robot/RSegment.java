package robot;

import geom.MyPShape;
import processing.core.PGraphics;

/**
 * TODO comment this
 * 
 * @author Joshua Hooker
 */
public class RSegment {
	
	/**
	 * The robot segment's model.
	 */
	protected MyPShape model;
	
	public RSegment(MyPShape model) {
		this.model = model;
	}
	
	/**
	 * Draws the model associated with this segment
	 * 
	 * @param g	The
	 */
	public void draw(PGraphics g) {
		g.shape(model);
	}
}
