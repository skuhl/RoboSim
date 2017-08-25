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
	 * TODO comment this
	 * 
	 * @param name
	 * @param form
	 */
	public Fixture(String name, RShape form) {
		super(name, form);
		dependents = new ArrayList<>();
	}

	/**
	 * Creates a fixture with the given name and shape, and coordinate system.
	 * 
	 * @param n
	 * @param s
	 * @param cs
	 */
	public Fixture(String n, RShape s, CoordinateSystem cs) {
		super(n, s, cs);
		dependents = new ArrayList<>();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param p
	 * @return
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
	 * TODO comment this
	 */
	public void clearDependents() {
		for (int idx = dependents.size() - 1; idx >= 0; --idx) {
			Part p = dependents.remove(idx);
			p.setParent(null);
		}
	}
	
	/**
	 * Applies the inverse of this Fixture's Coordinate System's transformation
	 * matrix to the matrix stack.
	 * 
	 * @return
	 */
	public RMatrix getInvCoordinateSystem() {
		RMatrix tMatrix = RMath.formTMat(localOrientation.getOrigin(),
				localOrientation.getAxes());
		return RMath.invertHCMatrix(tMatrix);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param p
	 * @return
	 */
	public boolean isAChild(Part p) {
		return dependents.contains(p);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
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
	 * TODO comment this
	 * 
	 * @param p
	 * @return
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
	 * TODO comment this
	 */
	private void updateDependents() {
		for (Part p : dependents) {
			p.updateAbsoluteOrientation();
		}
	}
}