package geom;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
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
	public void draw(PGraphics g) {
		g.pushStyle();
		applyStyle(g);
		
		/**
		 * Assumes the center of the cylinder is halfway between the top and
		 * bottom of of the cylinder.
		 * 
		 * Based off of the algorithm defined on Vormplus blog at:
		 * http://vormplus.be/blog/article/drawing-a-cylinder-with-processing
		 */
		float halfHeight = height / 2,
				diameter = 2 * radius;

		g.translate(0f, 0f, halfHeight);
		// Draw top of the cylinder
		g.ellipse(0f, 0f, diameter, diameter);
		g.translate(0f, 0f, -height);
		// Draw bottom of the cylinder
		g.ellipse(0f, 0f, diameter, diameter);
		g.translate(0f, 0f, halfHeight);

		g.beginShape(PConstants.TRIANGLE_STRIP);
		/* Draw a string of triangles around the circumference of the Cylinders
		 * top and bottom. */
		for (int degree = 0; degree <= 360; ++degree) {
			float pos_x = PApplet.cos(PConstants.DEG_TO_RAD * degree) * radius,
					pos_y = PApplet.sin(PConstants.DEG_TO_RAD * degree) * radius;

			g.vertex(pos_x, pos_y, halfHeight);
			g.vertex(pos_x, pos_y, -halfHeight);
		}

		g.endShape();
		
		g.popStyle();
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