package geom;

import global.Fields;
import global.RMath;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Defines a world object, which has a shape, a bounding box and a reference to a fixture.
 * The bounding box holds the local coordinate system of the object.
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
	private Fixture reference;

	/**
	 * Define a complex object as a part.
	 */
	public Part(String n, ComplexShape model) {
		super(n, model);
		absOBB = new BoundingBox(model.getDim(DimType.LENGTH),
								 model.getDim(DimType.HEIGHT),
								 model.getDim(DimType.WIDTH));
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Part(String n, int fill, int strokeVal, float edgeLen) {
		super(n, new RBox(fill, strokeVal, edgeLen));
		absOBB = new BoundingBox(edgeLen);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Creates a cylinder objects with the given colors and dimensions.
	 */
	public Part(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new RCylinder(fill, strokeVal, rad, hgt));
		absOBB = new BoundingBox(rad, rad, hgt);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Part(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new RBox(fill, strokeVal, len, hgt, wdh));
		absOBB = new BoundingBox(len, hgt, wdh);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Creates a Part with the given name, shape, bounding-box dimensions,
	 * default orientation and fixture reference.
	 */
	public Part(String n, RShape s, PVector OBBDims, CoordinateSystem local,
			CoordinateSystem def, Fixture fixRef) {
		
		super(n, s, local);
		absOBB = new BoundingBox(OBBDims.x, OBBDims.y, OBBDims.z);
		defaultOrientation = def;
		setFixtureRef(fixRef);
		updateOBBDims();
	}

	@Override
	public Part clone() {
		// The new object's reference still points to the same fixture!
		return new Part(getName(), getModel().clone(), getOBBDims().copy(),
				localOrientation.clone(), defaultOrientation.clone(),
				reference);
	}

	/**
	 * Determines if the given bounding box is colliding
	 * with this Part's bounding box.
	 */
	public boolean collision(BoundingBox obb) {
		return absOBB.collision3D(obb);
	}

	/**
	 * Determine if the given world object is colliding
	 * with this world object.
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
	 * @return	The absolute center of the part (without respect to its fixture
	 * 			reference)
	 */
	public PVector getCenter() {
		return absOBB.getCenter();
	}

	public PVector getDefaultCenter() {
		return defaultOrientation.getOrigin();
	}
	
	public RMatrix getDefaultOrientation() {
		return defaultOrientation.getAxes();
	}
	
	public Fixture getFixtureRef() { return reference; }
	
	/**
	 * Get the dimensions of the part's bounding-box
	 */
	public PVector getOBBDims() {
		return absOBB.getDims();
	}
	
	/**
	 * @return	The bounding box of the part
	 */
	public RBox getOBBFrame() {
		return absOBB.getFrame();
	}

	/**
	 * @return	The absolute orientation of the part (without respect to its
	 * 			fixture reference)
	 */
	public RMatrix getOrientation() {
		return absOBB.getOrientationAxes();
	}
	
	@Override
	public void rotateAroundAxis(PVector axis, float angle) {
		
		if (reference != null) {
			// rotate with respect to the part's fixture reference
			RMatrix refRMat = reference.getLocalOrientation();
			axis = RMath.rotateVector(axis, refRMat);
		}
		
		RMatrix orientation = localOrientation.getAxes();
		RMatrix rotation = RMath.formRMat(axis, angle);
		
		localOrientation.setAxes( rotation.multiply(orientation) );
		updateAbsoluteOrientation();
	}

	/**
	 * Sets the stroke color of the world's bounding-box
	 * to the given value.
	 */
	public void setBBColor(int newColor) {
		absOBB.setColor(newColor);
	}

	public void setDefaultCenter(PVector newCenter) {
		defaultOrientation.setOrigin(newCenter);
	}

	public void setDefaultOrientation(RMatrix newAxes) {
		defaultOrientation.setAxes(newAxes);
	}
	
	/**
	 * Set the fixture reference of this part and
	 * update its absolute orientation.
	 */
	public void setFixtureRef(Fixture refFixture) {
		reference = refFixture;
		updateAbsoluteOrientation();
	}
	
	@Override
	public void setLocalCenter(PVector newCenter) {
		super.setLocalCenter(newCenter);
		updateAbsoluteOrientation();
	}

	@Override
	public void setLocalOrientation(RMatrix newAxes) {
		super.setLocalOrientation(newAxes);
		updateAbsoluteOrientation();
	}

	/**
	 * See BoundingBox.setDim()
	 */
	public void setOBBDim(Float newVal, DimType dim) {
		absOBB.setDim(newVal, dim);
	}

	/**
	 * Set the dimensions of this part's bounding box.
	 */
	public void setOBBDimenions(PVector newDims) {
		absOBB.setDims(newDims);
	}
	
	@Override
	public void translate(float dx, float dy, float dz) {
		PVector delta = new PVector(dx, dy, dz);
		
		if (reference != null) {
			// translate with respect to the part's fixture reference
			RMatrix refRMat = reference.getLocalOrientation();
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
		
		if (reference != null) {
			PVector RefOrigin = reference.getLocalCenter();
			RMatrix refRMat = reference.getLocalOrientation();
			
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