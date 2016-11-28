package geom;
import robot.RobotRun;

/**
 * Defines the radius and height to draw a uniform cylinder
 */
public class Cylinder extends Shape {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	private float radius, height;

	public Cylinder(RobotRun robotRun) {
		super(robotRun);
		this.robotRun = robotRun;
		radius = 10f;
		height = 10f;
	}

	public Cylinder(RobotRun robotRun, int fill, int strokeVal, float rad, float hgt) {
		super(robotRun, fill, strokeVal);
		this.robotRun = robotRun;
		radius = rad;
		height = hgt;
	}

	public Cylinder(RobotRun robotRun, int strokeVal, float rad, float hgt) {
		super(null, strokeVal, strokeVal);
		this.robotRun = robotRun;
		radius = rad;
		height = hgt;
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

		this.robotRun.translate(0f, 0f, halfHeight);
		// Draw top of the cylinder
		this.robotRun.ellipse(0f, 0f, diameter, diameter);
		this.robotRun.translate(0f, 0f, -height);
		// Draw bottom of the cylinder
		this.robotRun.ellipse(0f, 0f, diameter, diameter);
		this.robotRun.translate(0f, 0f, halfHeight);

		this.robotRun.beginShape(RobotRun.TRIANGLE_STRIP);
		// Draw a string of triangles around the circumference of the Cylinders top and bottom.
		for (int degree = 0; degree <= 360; ++degree) {
			float pos_x = RobotRun.cos(RobotRun.DEG_TO_RAD * degree) * radius,
					pos_y = RobotRun.sin(RobotRun.DEG_TO_RAD * degree) * radius;

			this.robotRun.vertex(pos_x, pos_y, halfHeight);
			this.robotRun.vertex(pos_x, pos_y, -halfHeight);
		}

		this.robotRun.endShape();
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
	public float getDim(DimType dim) {
		switch(dim) {
		case RADIUS:  return radius;
		case HEIGHT:  return height;
		// Invalid dimension
		default:      return -1f;
		}
	}

	@Override
	public Object clone() {
		return new Cylinder(this.robotRun, getFillValue(), getStrokeValue(), radius, height);
	}
}