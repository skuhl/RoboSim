package geom;
import robot.CoordinateSystem;
import robot.RobotRun;

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 */
public class Fixture extends WorldObject {

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Fixture(String n, int fill, int strokeVal, float edgeLen) {
		super(n, new Box(fill, strokeVal, edgeLen));
	}

	/**
	 * Creates a cylinder object with the given colors and dimensions.
	 */
	public Fixture(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new Cylinder(fill, strokeVal, rad, hgt));
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Fixture(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new Box(fill, strokeVal, len, hgt, wdh));
	}

	/**
	 * Creates a fixture with the given name and shape.
	 */
	public Fixture(String n, ModelShape model) {
		super(n, model);
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 */
	public Fixture(String n, Shape s, CoordinateSystem cs) {
		super(n, s, cs);
	}

	@Override
	public Object clone() {
		return new Fixture(getName(), (Shape)getForm().clone(), (CoordinateSystem)localOrientation.clone());
	}

	/**
	 * Applies the inverse of this Fixture's Coordinate System's transformation matrix to the matrix stack.
	 */
	public void removeCoordinateSystem() {
		RMatrix tMatrix = RMath.transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
		tMatrix = RMath.invertHCMatrix(tMatrix);
		
		RobotRun.getInstance().applyMatrix(tMatrix);
	}
}