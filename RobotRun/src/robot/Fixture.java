package robot;
import geom.Box;
import geom.Cylinder;
import geom.ModelShape;
import geom.Shape;
import geom.WorldObject;

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 */
public class Fixture extends WorldObject {

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Fixture(RobotRun robotRun, String n, int fill, int strokeVal, float edgeLen) {
		super(n, new Box(fill, strokeVal, edgeLen));
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Fixture(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new Box(fill, strokeVal, len, hgt, wdh));
	}

	/**
	 * Creates a cylinder object with the given colors and dimensions.
	 */
	public Fixture(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new Cylinder(fill, strokeVal, rad, hgt));
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

	/**
	 * Applies the inverse of this Fixture's Coordinate System's transformation matrix to the matrix stack.
	 */
	public void removeCoordinateSystem() {
		float[][] tMatrix = RobotRun.getInstance().transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
		tMatrix = RobotRun.getInstance().invertHCMatrix(tMatrix);

		RobotRun.getInstance().applyMatrix(tMatrix[0][0], tMatrix[1][0], tMatrix[2][0], tMatrix[0][3],
				tMatrix[0][1], tMatrix[1][1], tMatrix[2][1], tMatrix[1][3],
				tMatrix[0][2], tMatrix[1][2], tMatrix[2][2], tMatrix[2][3],
				0,             0,             0,             1);
	}

	@Override
	public Object clone() {
		return new Fixture(getName(), (Shape)getForm().clone(), (CoordinateSystem)localOrientation.clone());
	}
}