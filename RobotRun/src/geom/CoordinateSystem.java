package geom;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Defines the axes and origin vector associated with a Coordinate System.
 */
public class CoordinateSystem implements Cloneable {
	private PVector origin;
	/* A 3x3 rotation matrix */
	private RMatrix axesVectors;

	public CoordinateSystem() {
		/* Pull origin and axes from the current transformation matrix */
		origin = RobotRun.getInstance().getCoordFromMatrix(0f, 0f, 0f);
		axesVectors = RobotRun.getInstance().getRotationMatrix();
	}

	/**
	 * Create a coordinate syste with the given origin and 3x3 rotation matrix.
	 */
	public CoordinateSystem(PVector origin, RMatrix axes) {
		this.origin = origin.copy();
		axesVectors = axes.copy();
	}

	/**
	 * Apply the coordinate system's origin and axes to the current transformation matrix.
	 */
	public void apply() {
		RobotRun.getInstance().applyMatrix(origin, axesVectors);
	}

	@Override
	public CoordinateSystem clone() {
		return new CoordinateSystem(origin.copy(), axesVectors.copy());
	}

	/**
	 * Return this coordinate system's axes.
	 */
	public RMatrix getAxes() { return axesVectors; }
	public PVector getOrigin() { return origin; }

	public void setAxes(RMatrix newAxes) {
		axesVectors = newAxes;
	}

	public void setOrigin(PVector newCenter) {
		origin = newCenter;
	}
}