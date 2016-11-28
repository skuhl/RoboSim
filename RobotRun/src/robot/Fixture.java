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
	 * 
	 */
	private final RobotRun robotRun;

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Fixture(RobotRun robotRun, String n, int fill, int strokeVal, float edgeLen) {
		super(robotRun, n, new Box(robotRun, fill, strokeVal, edgeLen));
		this.robotRun = robotRun;
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Fixture(RobotRun robotRun, String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(robotRun, n, new Box(robotRun, fill, strokeVal, len, hgt, wdh));
		this.robotRun = robotRun;
	}

	/**
	 * Creates a cylinder object with the given colors and dimensions.
	 */
	public Fixture(RobotRun robotRun, String n, int fill, int strokeVal, float rad, float hgt) {
		super(robotRun, n, new Cylinder(robotRun, fill, strokeVal, rad, hgt));
		this.robotRun = robotRun;
	}

	/**
	 * Creates a fixture with the given name and shape.
	 */
	public Fixture(RobotRun robotRun, String n, ModelShape model) {
		super(robotRun, n, model);
		this.robotRun = robotRun;
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 */
	public Fixture(RobotRun robotRun, String n, Shape s, CoordinateSystem cs) {
		super(robotRun, n, s, cs);
		this.robotRun = robotRun;
	}

	/**
	 * Applies the inverse of this Fixture's Coordinate System's transformation matrix to the matrix stack.
	 */
	public void removeCoordinateSystem() {
		float[][] tMatrix = this.robotRun.transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
		tMatrix = this.robotRun.invertHCMatrix(tMatrix);

		this.robotRun.applyMatrix(tMatrix[0][0], tMatrix[1][0], tMatrix[2][0], tMatrix[0][3],
				tMatrix[0][1], tMatrix[1][1], tMatrix[2][1], tMatrix[1][3],
				tMatrix[0][2], tMatrix[1][2], tMatrix[2][2], tMatrix[2][3],
				0,             0,             0,             1);
	}

	@Override
	public Object clone() {
		return new Fixture(this.robotRun, getName(), (Shape)getForm().clone(), (CoordinateSystem)localOrientation.clone());
	}
}