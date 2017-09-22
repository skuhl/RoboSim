package geom;

import global.Fields;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * A ray object used for mouse interactions with world objects in the scene.
 * 
 * @author Joshua Hooker
 */
public class RRay {
	
	/**
	 * The direction vector of the ray.
	 */
	private PVector direction;
	
	/**
	 * The length, from the origin, of the portion of the ray, which will be
	 * drawn.
	 */
	private float drawLength;
	
	/**
	 * The origin of the ray.
	 */
	private PVector origin;
	
	/**
	 * The color, with which the ray will be drawn.
	 */
	private int strokeCVal;
	
	/**
	 * Creates a ray pointing in the position xyz direction starting at the
	 * coordinate system origin.
	 */
	public RRay() {
		origin = new PVector(0f, 0f, 0f);
		direction = new PVector(1f, 1f, 1f);
		strokeCVal = Fields.BLACK;
		drawLength = 5000f;
	}
	
	/**
	 * Create a ray from the given origin and another point on the ray, with
	 * the specific color and draw length.
	 * 
	 * @param origin		The origin of the ray
	 * @param pointOnRay	A point on the ray
	 * @param drawnLen		How much of the ray is drawn
	 * @param color			The color with which the ray will be drawn
	 */
	public RRay(PVector origin, PVector pointOnRay, float drawnLen, int color) {
		this.origin = origin;
		direction = PVector.sub(pointOnRay, origin);
		direction.normalize();
		this.strokeCVal = color;
		drawLength = drawnLen;
	}
	
	/**
	 * Create a ray from the given origin, direction vector, color and draw
	 * length.
	 * 
	 * @param origin		The origin of the ray
	 * @param direct		The direction vector of the ray
	 * @param drawnLen		How much of the ray is drawn
	 * @param color			The color with which the ray will be drawn
	 */
	public RRay(PVector origin, PVector direct, int color, float drawnLen) {
		this.origin = origin;
		direction = direct;
		this.strokeCVal = color;
		drawLength = drawnLen;
	}
	
	@Override
	public RRay clone() {
		return new RRay(origin.copy(), direction.copy(), strokeCVal,
				drawLength);
	}
	
	/**
	 * Draws the ray with the given graphics.
	 * 
	 * @param g	the graphics used to draw the ray
	 */
	public void draw(PGraphics g) {
		g.pushStyle();
		g.stroke(strokeCVal);
		g.noFill();
		
		// Define the endpoint of the line
		PVector endpoint = PVector.mult(direction, drawLength);
		endpoint.add(origin);
		g.line(origin.x, origin.y, origin.z, endpoint.x, endpoint.y,
				endpoint.z);
		
		g.popStyle();
	}
	
	// Getter and setter methods
	
	public int getColor() {
		return strokeCVal;
	}
	
	public PVector getDirection() {
		return direction;
	}
	
	public float getDrawLength() {
		return drawLength;
	}
	
	public PVector getOrigin() {
		return origin;
	}
	
	public void setColor(int newColor) {
		strokeCVal = newColor;
	}
	
	public PVector setDirection(PVector newDirect) {
		PVector limbo = direction;
		direction = newDirect;
		return limbo;
	}
	
	public void setDrawLength(int newLen) {
		drawLength = newLen;
	}
	
	public PVector setOrigin(PVector newOrigin) {
		PVector limbo = origin;
		origin = newOrigin;
		return limbo;
	}
	
	@Override
	public String toString() {
		return String.format("(origin=%s direct=%s length=%9.3f color=%d)",
				origin, direction, drawLength, strokeCVal);
	}
}
