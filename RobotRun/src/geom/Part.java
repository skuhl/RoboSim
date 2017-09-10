package geom;

import enums.DimType;
import global.Fields;
import global.RMath;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Defines a world object, which has a shape, a bounding box and a reference to a fixture.
 * The bounding box holds the local coordinate system of the object.
 * 
 * @author Joshua Hooker
 */
public class Part extends WorldObject {
	
	/**
	 * A scale factor for calculating the dimensions of a part's bounding box.
	 */
	private static final float OBB_DIM_SCALE, OBB_RAD_SCALE;
	
	static {
		OBB_DIM_SCALE = 0.05f;
		OBB_RAD_SCALE = 0.07f;
	}
	
	protected CoordinateSystem defaultOrientation;
	private BoundingBox absOBB;
	private Fixture parent;
	
	/**
	 * Initializes a part with the given name and shape.
	 * 
	 * @param name	The name of the part, which should be unique amongst all
	 * 				objects in the parent scenario
	 * @param form	The shape of this part
	 */
	public Part(String name, RShape form) {
		super(name, form);
		
		if (form instanceof RCylinder) {
			absOBB = new BoundingBox(form.getDim(DimType.RADIUS),
									form.getDim(DimType.RADIUS),
									form.getDim(DimType.HEIGHT));
			
		} else {
			absOBB = new BoundingBox(form.getDim(DimType.LENGTH),
					form.getDim(DimType.HEIGHT),
					form.getDim(DimType.WIDTH));

		}
		
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Creates a Part with the given name, shape, bounding-box dimensions,
	 * default orientation and fixture reference.
	 * 
	 * @param n			The name of this part, which should be unique amongst
	 * 					all objects in the part scenario
	 * @param s			The shape of this part
	 * @param OBBDims	The dimensions of this part's bounding box
	 * @param local		This part's local coordinate system
	 * @param def		This part's default coordinate system
	 */
	public Part(String n, RShape s, PVector OBBDims, CoordinateSystem local,
			CoordinateSystem def) {
		
		super(n, s, local);
		absOBB = new BoundingBox(OBBDims.x, OBBDims.y, OBBDims.z);
		defaultOrientation = def;
		updateOBBDims();
		updateAbsoluteOrientation();
	}

	@Override
	public Part clone() {
		return new Part(getName(), getModel().clone(), getOBBDims().copy(),
				localOrientation.clone(), defaultOrientation.clone());
	}
	
	@Override
	public WorldObject clone(String name) {
		return new Part(name, getModel().clone(), getOBBDims().copy(),
				localOrientation.clone(), defaultOrientation.clone());
	}

	/**
	 * Determines if the given bounding box is colliding with this part's
	 * bounding box.
	 * 
	 * @param obb	The bounding box, which is compared to this part's
	 * 				bounding box
	 * @return		If the given bounding box is colliding with this part's
	 * 				bounding box
	 */
	public boolean collision(BoundingBox obb) {
		return absOBB.collision3D(obb);
	}

	/**
	 * Determines if the given world object is colliding with this world
	 * object.
	 * 
	 * @param obj	The part to compare to this part
	 * @return		If the given part's bounding box is colliding with this
	 * 				part's bounding box
	 */
	public boolean collision(Part obj) {
		return  absOBB.collision3D(obj.absOBB);
	}
	
	@Override
	public PVector collision(RRay ray) {
		return absOBB.collision(ray);
	}
	
	@Override
	public void draw(PGraphics g) {
		draw(g, true);
	}
	
	/**
	 * Parts the part and its bounding box (depending on the value of drawOBBs)
	 * in the absolute coordinate frame of part.
	 * 
	 * @param g			The graphics used to draw the part
	 * @param drawOBBs	Whether to render the bounding boxes
	 */
	public void draw(PGraphics g, boolean drawOBBs) {
		g.pushMatrix();
		Fields.transform(g, absOBB.getCenter(), absOBB.getOrientationAxes());
		
		getModel().draw(g);
		
		if (drawOBBs) {
			absOBB.getFrame().draw(g);
		}
		
		g.popMatrix();
	}
	
	/**
	 * Returns a reference to this part's position with respective to
	 * Processing's native coordinate system.
	 * 
	 * @return	This part's absolute center position
	 */
	public PVector getCenter() {
		return absOBB.getCenter();
	}

	/**
	 * Return's a reference to this part's default origin position.
	 * 
	 * @return	a reference to the default position of this part
	 */
	public PVector getDefaultCenter() {
		return defaultOrientation.getOrigin();
	}
	
	/**
	 * Return's a reference to this part's default orientation.
	 * 
	 * @return	a reference to the default orientation of this part
	 */
	public RMatrix getDefaultOrientation() {
		return defaultOrientation.getAxes();
	}
	
	/**
	 * Returns a reference to this part's parent fixture, which can be null.
	 * 
	 * @return	a reference to this part's parent fixture
	 */
	public Fixture getParent() {
		return parent;
	}
	
	/**
	 * @return	The dimensions of this part's bounding box
	 * @see BoundingBox#getDims()
	 */
	public PVector getOBBDims() {
		return absOBB.getDims();
	}
	
	/**
	 * @return	The shape of this part's bounding box
	 * @see BoundingBox#getFrame()
	 */
	public RBox getOBBFrame() {
		return absOBB.getFrame();
	}

	/**
	 * This part's orientation with respect to Procesing's native coordinate
	 * system.
	 * 
	 * @return	The absolute orientation of the part
	 */
	public RMatrix getOrientation() {
		return absOBB.getOrientationAxes();
	}
	
	/**
	 * Disassociates this part from its parent fixture.
	 */
	public void removeParent() {
		if (parent != null) {
			Fixture parentRef = parent;
			parentRef.removeDependent(this);
		}
	}
	
	@Override
	public void rotateAroundAxis(PVector axis, float angle) {
		
		if (parent != null) {
			// rotate with respect to the part's fixture reference
			RMatrix refRMat = parent.getLocalOrientation();
			axis = RMath.rotateVector(axis, refRMat);
		}
		
		RMatrix orientation = localOrientation.getAxes();
		RMatrix rotation = RMath.formRMat(axis, angle);
		
		localOrientation.setAxes( rotation.multiply(orientation) );
		updateAbsoluteOrientation();
	}

	/**
	 * Sets the color of this part's bounding box.
	 * 
	 * @param newColor	The new stroke of this bounding box
	 */
	public void setBBColor(int newColor) {
		absOBB.setColor(newColor);
	}

	/**
	 * Sets this part's default center position.
	 * 
	 * @param newCenter	This part's new default center position 
	 */
	public void setDefaultCenter(PVector newCenter) {
		defaultOrientation.setOrigin(newCenter);
	}

	/**
	 * Sets this part's default orientation.
	 * 
	 * @param newAxes	This part's new default orientation
	 */
	public void setDefaultOrientation(RMatrix newAxes) {
		defaultOrientation.setAxes(newAxes);
	}
	
	/**
	 * Set the fixture reference of this part and
	 * update its absolute orientation.
	 * 
	 * @param newParent	The new parent of this part
	 */
	protected void setParent(Fixture newParent) {
		parent = newParent;
		updateAbsoluteOrientation();
	}
	
	@Override
	public void setLocalCenter(PVector newCenter) {
		super.setLocalCenter(newCenter);
		updateAbsoluteOrientation();
	}
	
	@Override
	public void setLocalCoordinates(PVector newCenter, RMatrix newAxes) {
		super.setLocalCoordinates(newCenter, newAxes);
		this.updateAbsoluteOrientation();
	}

	@Override
	public void setLocalOrientation(RMatrix newAxes) {
		super.setLocalOrientation(newAxes);
		updateAbsoluteOrientation();
	}

	/**
	 * @param newVal	The new value for the specified dimension
	 * @param dim		The type of the dimension to set
	 * @see BoundingBox#setDim(Float, DimType)
	 */
	public void setOBBDim(Float newVal, DimType dim) {
		absOBB.setDim(newVal, dim);
	}

	/**
	 * @param newDims	The new set of dimensions for this part's bounding box
	 * @see BoundingBox#setDims(PVector)
	 */
	public void setOBBDimenions(PVector newDims) {
		absOBB.setDims(newDims);
	}
	
	@Override
	public void translate(float dx, float dy, float dz) {
		PVector delta = new PVector(dx, dy, dz);
		
		if (parent != null) {
			// translate with respect to the part's fixture reference
			RMatrix refRMat = parent.getLocalOrientation();
			delta = RMath.rotateVector(delta, refRMat);
		}
		
		super.translate(delta.x, delta.y, delta.z);
		updateAbsoluteOrientation();
	}

	/**
	 * Update the Part's absolute (or world) orientation
	 * based on its local orientation and fixture
	 * reference's orientation.
	 */
	public void updateAbsoluteOrientation() {
		PVector origin = localOrientation.getOrigin().copy();
		RMatrix rMat = localOrientation.getAxes().copy();
		
		if (parent != null) {
			PVector RefOrigin = parent.getLocalCenter();
			RMatrix refRMat = parent.getLocalOrientation();
			
			origin = RMath.rotateVector(origin, refRMat.getInverse());
			origin.add(RefOrigin);
			
			rMat = refRMat.multiply(rMat);
		}
		
		absOBB.setCenter(origin);
		absOBB.setOrientation(rMat);
	}

	@Override
	public void updateLocalCenter(Float x, Float y, Float z) {
		super.updateLocalCenter(x, y, z);
		updateAbsoluteOrientation();
	}
	
	/**
	 * Update the part's bounding box dimensions of the part based on the
	 * dimensions of its form.
	 */
	public void updateOBBDims() {
		RShape s = getModel();
		float minAddition = Float.MAX_VALUE;

		if (s instanceof RBox || s instanceof ComplexShape) {
			// Update the OBB dimensions for a box or complex part
			minAddition = OBB_DIM_SCALE * RMath.min(s.getDim(DimType.LENGTH),
					s.getDim(DimType.HEIGHT), s.getDim(DimType.WIDTH));

			absOBB.setDim(s.getDim(DimType.LENGTH) + minAddition, DimType.LENGTH);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.WIDTH) + minAddition, DimType.WIDTH);

		} else if (s instanceof RCylinder) {
			// Update the OBB dimensions for a cylindrical part
			minAddition =  RMath.min(OBB_RAD_SCALE * s.getDim(DimType.RADIUS),
					OBB_DIM_SCALE * s.getDim(DimType.HEIGHT));

			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.LENGTH);
			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.WIDTH); 
		}
	}
}