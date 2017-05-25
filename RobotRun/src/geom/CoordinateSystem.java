package geom;

import global.Fields;
import processing.core.PVector;

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
	 * The standard coordinate system with origin (0, 0, 0) and the identity
	 * matrix as the orientation.
	 */
	public CoordinateSystem() {
		origin = new PVector(0f, 0f, 0f);
		axesVectors = Fields.IDENTITY_MAT.copy();
	}

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