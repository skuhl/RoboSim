package geom;
import processing.core.PApplet;
import processing.core.PConstants;
import robot.RobotRun;

/**
 * Defines the radius and height to draw a uniform cylinder
 */
public class Cylinder extends Shape {
	private float radius, height;

	public Cylinder() {
		super();
		radius = 10f;
		height = 10f;
	}

	public Cylinder(int fill, int strokeVal, float rad, float hgt) {
		super(fill, strokeVal);
		radius = rad;
		height = hgt;
	}

	public Cylinder(RobotRun robotRun, int strokeVal, float rad, float hgt) {
		super(strokeVal, strokeVal);
		radius = rad;
		height = hgt;
	}

	@Override
	public Cylinder clone() {
		return new Cylinder(getFillValue(), getStrokeValue(), radius, height);
	}

	@Override
	public float getDim(DimType dim) {
		switch(dim) {
		case RADIUS:  return radius;
		case HEIGHT:  return height;
		// Invalid dimension
		default:      return -1f;
		}
	}
	
	@Override
	public float[] getDimArray() {
		float[] dims = new float[3];
		dims[0] = 2*getDim(DimType.RADIUS);
		dims[1] = getDim(DimType.HEIGHT);
		dims[2] = 2*getDim(DimType.RADIUS);
		return dims;
	}

	@Override
	public void setDim(Float newVal, DimType dim) {
		switch(dim) {
		case RADIUS:
			// Update radius
			radius = newVal;
			break;

		case HEIGHT:
			// Update height
			height = newVal;
			break;

		default:
		}
	}

	@Override
	public int getID() {
		return -1;
	}
}