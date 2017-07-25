package geom;
import global.RMath;

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 */
public class Fixture extends WorldObject {

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Fixture(String n, int fill, int strokeVal, float edgeLen) {
		super(n, new RBox(fill, strokeVal, edgeLen));
	}

	/**
	 * Creates a cylinder object with the given colors and dimensions.
	 */
	public Fixture(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new RCylinder(fill, strokeVal, rad, hgt));
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Fixture(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new RBox(fill, strokeVal, len, hgt, wdh));
	}

	/**
	 * Creates a fixture with the given name and shape.
	 */
	public Fixture(String n, ComplexShape model) {
		super(n, model);
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 */
	public Fixture(String n, RShape s, CoordinateSystem cs) {
		super(n, s, cs);
	}

	@Override
	public Fixture clone() {
		return new Fixture(getName(), getForm().clone(),
				localOrientation.clone());
	}

	/**
	 * Applies the inverse of this Fixture's Coordinate System's transformation
	 * matrix to the matrix stack.
	 */
	public RMatrix getInvCoordinateSystem() {
		RMatrix tMatrix = RMath.formTMat(localOrientation.getOrigin(), localOrientation.getAxes());
		return RMath.invertHCMatrix(tMatrix);
	}
}