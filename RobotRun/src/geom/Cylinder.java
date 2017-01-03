package geom;
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
	public Object clone() {
		return new Cylinder(getFillValue(), getStrokeValue(), radius, height);
	}

	/**
	 * Assumes the center of the cylinder is halfway between the top and bottom of of the cylinder.
	 * 
	 * Based off of the algorithm defined on Vormplus blog at:
	 * http://vormplus.be/blog/article/drawing-a-cylinder-with-processing
	 */
	public void draw() {
		applyColors();

		float halfHeight = height / 2,
				diameter = 2 * radius;

		RobotRun.getInstance().translate(0f, 0f, halfHeight);
		// Draw top of the cylinder
		RobotRun.getInstance().ellipse(0f, 0f, diameter, diameter);
		RobotRun.getInstance().translate(0f, 0f, -height);
		// Draw bottom of the cylinder
		RobotRun.getInstance().ellipse(0f, 0f, diameter, diameter);
		RobotRun.getInstance().translate(0f, 0f, halfHeight);

		RobotRun.getInstance().beginShape(RobotRun.TRIANGLE_STRIP);
		// Draw a string of triangles around the circumference of the Cylinders top and bottom.
		for (int degree = 0; degree <= 360; ++degree) {
			float pos_x = RobotRun.cos(RobotRun.DEG_TO_RAD * degree) * radius,
					pos_y = RobotRun.sin(RobotRun.DEG_TO_RAD * degree) * radius;

			RobotRun.getInstance().vertex(pos_x, pos_y, halfHeight);
			RobotRun.getInstance().vertex(pos_x, pos_y, -halfHeight);
		}

		RobotRun.getInstance().endShape();
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
}