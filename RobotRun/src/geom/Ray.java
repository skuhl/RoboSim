package geom;

import global.Fields;
import processing.core.PVector;

/**
 * A ray object used for mouse interactions with world objects in the scene.
 * 
 * @author Joshua Hooker
 */
public class Ray {
	
	/**
	 * The origin of the ray.
	 */
	private PVector origin;
	
	/**
	 * The direction vector of the ray.
	 */
	private PVector direction;
	
	/**
	 * The color, with which the ray will be drawn.
	 */
	private int color;
	
	/**
	 * The length, from the origin, of the portion of the ray, which will be
	 * drawn.
	 */
	private float drawLength;
	
	/**
	 * Creates a ray pointing in the position xyz direction starting at the
	 * coordinate system origin.
	 */
	public Ray() {
		origin = new PVector(0f, 0f, 0f);
		direction = new PVector(1f, 1f, 1f);
		color = Fields.BLACK;
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
	public Ray(PVector origin, PVector pointOnRay, float drawnLen, int color) {
		this.origin = origin;
		direction = PVector.sub(pointOnRay, origin);
		direction.normalize();
		this.color = color;
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
	public Ray(PVector origin, PVector direct, int color, float len) {
		this.origin = origin;
		direction = direct;
		this.color = color;
		drawLength = len;
	}
	
	@Override
	public Ray clone() {
		return new Ray(origin.copy(), direction.copy(), color, drawLength);
	}
	
	// Getter and setter methods
	
	public int getColor() {
		return color;
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
		color = newColor;
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
				origin, direction, drawLength, color);
	}
}
