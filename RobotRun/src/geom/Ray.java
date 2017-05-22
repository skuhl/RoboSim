package geom;
import global.Fields;
import processing.core.PVector;
import robot.RobotRun;

/**
 * TODO comment
 * 
 * @author Joshua Hooker
 */
public class Ray {

	private PVector origin;
	private PVector direction;
	private int color;
	private float length;

	public Ray() {
		origin = new PVector(0f, 0f, 0f);
		direction = new PVector(1f, 1f, 1f);
		color = Fields.BLACK;
		length = 5000f;
	}

	public Ray(PVector origin, PVector pointOnRay, int color, float len) {
		this.origin = origin.copy();
		direction = pointOnRay.sub(origin);
		direction.normalize();
		this.color = color;
		length = len;
	}
	
	public int getColor() {
		return color;
	}
	
	public PVector getDirection() {
		return direction;
	}
	
	public float getLength() {
		return length;
	}
	
	public PVector getOrigin() {
		return origin;
	}
	
	public void setColor(int newColor) {
		color = newColor;
	}
	
	public void setLength(int newLen) {
		length = newLen;
	}
	
	@Override
	public String toString() {
		return String.format("origin=%s direct=%s", origin, direction);
	}
}