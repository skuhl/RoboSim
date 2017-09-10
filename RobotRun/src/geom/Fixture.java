package geom;
import java.util.ArrayList;

import global.RMath;
import processing.core.PVector;

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 * 
 * @author Joshua Hooker
 */
public class Fixture extends WorldObject {
	
	/**
	 * A list of all parts that refer to fixture as its parent coordinate
	 * system.
	 */
	private ArrayList<Part> dependents;
	
	/**
	 * Initializes a fixture with the given name and shape.
	 * 
	 * @param name	The name of this fixture, which should be unique amongst
	 * 				all objects in its parent scenario
	 * @param form	This fixture's shape
	 */
	public Fixture(String name, RShape form) {
		super(name, form);
		dependents = new ArrayList<>();
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 * 
	 * @param n		The name of this fixture, which should be unique amongst
	 * 				all objects in its parent scenario
	 * @param s		This fixture's shape
	 * @param cs	The is fixture current center and orientation
	 */
	public Fixture(String n, RShape s, CoordinateSystem cs) {
		super(n, s, cs);
		dependents = new ArrayList<>();
	}
	
	/**
	 * Adds the given part as a dependent of this fixture's local coordinate
	 * system, if it is not already associated with this fixture.
	 * 
	 * @param p	The part to associate with this fixture
	 * @return	If the given part was successfully associated with this fixture
	 */
	public boolean addDependent(Part p) {
		if (p != null && !dependents.contains(p)) {
			dependents.add(p);
			p.setParent(this);
			return true;
		}
		
		return false;
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
	 * Disassociates all parts, which all dependent on this fixture.
	 */
	public void clearDependents() {
		for (int idx = dependents.size() - 1; idx >= 0; --idx) {
			Part p = dependents.remove(idx);
			p.setParent(null);
		}
	}
	
	/**
	 * Returns a transformation, which defines the inverse of the
	 * transformation defined by this fixture's local orientation.
	 * 
	 * @return	A transformation matrix, which represents the inverse of the
	 * 			transformation defined by this fixture's local orientation.
	 */
	public RMatrix getInvCoordinateSystem() {
		RMatrix tMatrix = RMath.formTMat(localOrientation.getOrigin(),
				localOrientation.getAxes());
		return RMath.invertHCMatrix(tMatrix);
	}
	
	/**
	 * Checks if the given part is dependent on this fixture.
	 * 
	 * @param p	The part in question
	 * @return	If the given part is dependent on this fixture
	 */
	public boolean isAChild(Part p) {
		return dependents.contains(p);
	}
	
	/**
	 * Disassociates the part, at the given index in this fixture's list of
	 * dependent parts, from this fixture.
	 * 
	 * @param idx	The index of the part to disassociate
	 * @return		The part, which was disassociated from this fixture
	 */
	public Part removeDependent(int idx) {
		if (idx >= 0 && idx < dependents.size()) {
			Part p = dependents.remove(idx);
			p.setParent(null);
			return p;
		}
		
		return null;
	}
	
	/**
	 * Disassociates the given part from this fixture, if it is associated with
	 * the fixture. 
	 * 
	 * @param p	The part to disassociate from this fixture
	 * @return	If the given part is successfully disassociated
	 */
	public boolean removeDependent(Part p) {
		boolean removed = dependents.remove(p);
		
		if (removed) {
			p.setParent(null);
		}
		
		return removed;
	}
	
	@Override
	public void setLocalCenter(PVector newCenter) {
		super.setLocalCenter(newCenter);
		updateDependents();
	}
	
	@Override
	public void setLocalCoordinates(PVector newCenter, RMatrix newAxes) {
		super.setLocalCoordinates(newCenter, newAxes);
		updateDependents();
	}
	
	@Override
	public void setLocalOrientation(RMatrix newAxes) {
		super.setLocalOrientation(newAxes);
		updateDependents();
	}
	
	@Override
	public void translate(float dx, float dy, float dz) {
		super.translate(dx, dy, dz);
		updateDependents();
	}
	
	@Override
	public void updateLocalCenter(Float x, Float y, Float z) {
		super.updateLocalCenter(x, y, z);
		updateDependents();
	}
	
	/**
	 * Updates all the dependent part's absolute orientation based on this
	 * fixture's current orientation.
	 */
	private void updateDependents() {
		for (Part p : dependents) {
			p.updateAbsoluteOrientation();
		}
	}
}