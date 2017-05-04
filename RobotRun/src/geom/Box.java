package geom;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Defines the length, width, height values to draw a box.
 */
public class Box extends Shape {

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
	public Box() {
		super();
		dimensions = new PVector(10f, 10f, 10f);
	}

	/**
	 * Create an empty cube with the given color and dinemsion.
	 */
	public Box(int strokeVal, float edgeLen) {
		super(null, strokeVal);
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	/**
	 * Create an empty box with the given color and dinemsions.
	 */
	public Box(int strokeVal, float len, float hgt, float wdh) {
		super(null, strokeVal);
		dimensions = new PVector(len, hgt, wdh);
	}

	/**
	 * Create a cube with the given colors and dinemsion.
	 */
	public Box(int fill, int strokeVal, float edgeLen) {
		super(fill, strokeVal);
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	/**
	 * Create a box with the given colors and dinemsions.
	 */
	public Box(int fill, int strokeVal, float len, float hgt, float wdh) {
		super(fill, strokeVal);
		dimensions = new PVector(len, hgt, wdh);
	}

	@Override
	public Object clone() {
		return new Box(getFillValue(), getStrokeValue(), dimensions.x, dimensions.y, dimensions.z);
	}

	@Override
	public void draw() {
		// Apply colors
		applyColors();
		RobotRun.getInstance().box(dimensions.x, dimensions.y, dimensions.z);
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
}