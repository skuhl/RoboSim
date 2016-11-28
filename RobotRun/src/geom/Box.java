package geom;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Defines the length, width, height values to draw a box.
 */
public class Box extends Shape {
	/**
	 * 
	 */
	private RobotRun robotRun;
	/**
	 * X -> length
	 * Y -> Height
	 * Z -> Width
	 */
	private PVector dimensions;

	/**
	 * Create a cube, with an edge length of 10.
	 * @param robotRun TODO
	 */
	public Box(RobotRun robotRun) {
		super(robotRun);
		this.robotRun = robotRun;
		dimensions = new PVector(10f, 10f, 10f);
	}

	/**
	 * Create a box with the given colors and dinemsions.
	 */
	public Box(RobotRun robotRun, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(robotRun, fill, strokeVal);
		this.robotRun = robotRun;
		dimensions = new PVector(len, hgt, wdh);
	}

	/**
	 * Create an empty box with the given color and dinemsions.
	 */
	public Box(RobotRun robotRun, int strokeVal, float len, float hgt, float wdh) {
		super(robotRun, null, strokeVal);
		this.robotRun = robotRun;
		dimensions = new PVector(len, hgt, wdh);
	}

	/**
	 * Create a cube with the given colors and dinemsion.
	 */
	public Box(RobotRun robotRun, int fill, int strokeVal, float edgeLen) {
		super(robotRun, fill, strokeVal);
		this.robotRun = robotRun;
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	/**
	 * Create an empty cube with the given color and dinemsion.
	 */
	public Box(RobotRun robotRun, int strokeVal, float edgeLen) {
		super(robotRun, null, strokeVal);
		this.robotRun = robotRun;
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	public void draw() {
		// Apply colors
		applyColors();
		this.robotRun.box(dimensions.x, dimensions.y, dimensions.z);
	}

	@Override
	public void setDim(Float newVal, DimType dim) {

		switch (dim) {
		case LENGTH:
			// Update length
			dimensions.x = newVal;
			break;
		case HEIGHT:
			// Update height
			dimensions.y = newVal;
			break;

		case WIDTH:
			// Update width
			dimensions.z = newVal;
			break;
			// Invalid dimension
		default:
		}
	}

	@Override
	public float getDim(DimType dim) {    
		switch (dim) {
		case LENGTH:  return dimensions.x;
		case HEIGHT:  return dimensions.y;
		case WIDTH:   return dimensions.z;
		// Invalid dimension
		default:      return -1f;
		}
	}

	@Override
	public Object clone() {
		return new Box(this.robotRun, getFillValue(), getStrokeValue(), dimensions.x, dimensions.y, dimensions.z);
	}
}