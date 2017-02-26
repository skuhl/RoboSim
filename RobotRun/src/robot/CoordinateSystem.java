package robot;
import processing.core.PVector;

/**
 * Defines the axes and origin vector associated with a Coordinate System.
 */
public class CoordinateSystem implements Cloneable {
	private PVector origin;
	/* A 3x3 rotation matrix */
	private float[][] axesVectors;

	public CoordinateSystem() {
		/* Pull origin and axes from the current transformation matrix */
		origin = RobotRun.getInstance().getCoordFromMatrix(0f, 0f, 0f);
		axesVectors = RobotRun.getInstance().getRotationMatrix();
	}

	/**
	 * Create a coordinate syste with the given origin and 3x3 rotation matrix.
	 */
	public CoordinateSystem(PVector origin, float[][] axes) {
		this.origin = origin.copy();
		axesVectors = new float[3][3];
		// Copy axes into axesVectors
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				axesVectors[row][col] = axes[row][col];
			}
		}
	}

	/**
	 * Apply the coordinate system's origin and axes to the current transformation matrix.
	 */
	public void apply() {
		RobotRun.getInstance().applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
										   axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
										   axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
										   				   0,				  0,                 0,        1);
	}

	@Override
	public Object clone() {
		float[][] axesCopy = new float[3][3];

		// Copy axes into axesVectors
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				axesCopy[row][col] = axesVectors[row][col];
			}
		}

		return new CoordinateSystem(origin.copy(), axesCopy);
	}

	/**
	 * Return this coordinate system's axes in row major order.
	 */
	public float[][] getAxes() {
		return axesVectors;
	}

	public PVector getOrigin() { return origin; }

	/**
	 * Reset the coordinate system's axes vectors and return the
	 * old axes; the given rotation matrix should be in row
	 * major order!
	 */
	public void setAxes(float[][] newAxes) {
		axesVectors = new float[3][3];

		// Copy axes into axesVectors
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				axesVectors[row][col] = newAxes[row][col];
			}
		}
	}

	public void setOrigin(PVector newCenter) {
		origin = newCenter;
	}
}