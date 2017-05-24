package geom;
import global.Fields;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Defines a coordinate system comprised of a position origin and rotation
 * matrix orientation.
 * 
 * @author Joshu Hooker
 */
public class CoordinateSystem implements Cloneable {
	
	/**
	 * The position of the coordinate system, with respect to its part
	 * coordinate system.
	 */
	private PVector origin;
	
	/**
	 * A 3x3 rotation matrix representing the coordinate system's orientation.
	 */
	private RMatrix axesVectors;
	
	/**
	public CoordinateSystem() {
		/* Pull origin and axes from the current transformation matrix *
		origin = RobotRun.getInstance().getCoordFromMatrix(0f, 0f, 0f);
		axesVectors = RobotRun.getInstance().getRotationMatrix();
	}
	/**/

	/**
	 * Creates a coordinate system with the given origin and orientation.
	 * 
	 * @param origin	The origin of the coordinate system
	 * @param axes		The orientation of the coordinate system
	 */
	public CoordinateSystem(PVector origin, RMatrix axes) {
		this.origin = origin;
		axesVectors = axes;
	}
	
	/**
	 * @return	An instance of the default coordinate system
	 */
	public static CoordinateSystem getDefault() {
		return new CoordinateSystem(new PVector(0f, 0f, 0f),
				Fields.IDENTITY_MAT.copy());
	}

	/**
	 * Apply the coordinate system's origin and axes to the current transformation matrix.
	 */
	public void apply() {
		RobotRun.getInstance().applyCoord(origin, axesVectors);
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
		
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				axesVectors.setEntry(row, col, newAxes.getEntry(row, col));
			}
		}
		
	}

	public void setOrigin(PVector newOrigin) {
		origin.x = newOrigin.x;
		origin.y = newOrigin.y;
		origin.z = newOrigin.z;
	}
}