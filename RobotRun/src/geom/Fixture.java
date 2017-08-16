package geom;
import global.RMath;

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 */
public class Fixture extends WorldObject {
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @param form
	 */
	public Fixture(String name, RShape form) {
		super(name, form);
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 */
	public Fixture(String n, RShape s, CoordinateSystem cs) {
		super(n, s, cs);
	}

	@Override
	public Fixture clone() {
		return new Fixture(getName(), getModel().clone(),
				localOrientation.clone());
	}
	
	@Override
	public Fixture clone(String name) {
		return new Fixture(name, getModel().clone(),
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