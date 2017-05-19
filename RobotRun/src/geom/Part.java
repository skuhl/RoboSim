package geom;
import processing.core.PApplet;
import processing.core.PVector;
import robot.RobotRun;

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
		OBB_DIM_SCALE = 1.05f;
		OBB_RAD_SCALE = 2.07f;
	}
	
	/**
	 * This algorithm uses the Separating Axis Theorm to project radi of each Box on to several 
	 * axes to determine if a there is any overlap between the boxes. The method strongly resembles 
	 * the method outlined in Section 4.4 of "Real Time Collision Detection" by Christer Ericson
	 *
	 * @param A  The hit box associated with some object in space
	 * @param B  The hit box associated with another object in space
	 * @return   Whether the two hit boxes intersect
	 */
	public static boolean collision3D(BoundingBox A, BoundingBox B) {
		// Rows are x, y, z axis vectors for A and B: Ax, Ay, Az, Bx, By, and Bz
		float[][] axes_A = A.getOrientationAxes().getFloatData();
		float[][] axes_B = B.getOrientationAxes().getFloatData();

		// Rotation matrices to convert B into A's coordinate system
		float[][] rotMatrix = new float[3][3];
		float[][] absRotMatrix = new float[3][3];

		for(int v = 0; v < axes_A.length; v += 1) {
			for(int u = 0; u < axes_B.length; u += 1) {
				// PLEASE do not change to matrix multiplication
				rotMatrix[v][u] = axes_A[v][0] * axes_B[u][0] +  axes_A[v][1] * axes_B[u][1] +  axes_A[v][2] * axes_B[u][2];
				// Add offset for values close to zero (parallel axes)
				absRotMatrix[v][u] = Math.abs(rotMatrix[v][u]) + 0.00000000175f;
			}
		}

		// T = B's position - A's
		PVector posA = new PVector().set(A.getCenter());
		PVector posB = new PVector().set(B.getCenter());
		PVector limbo = posB.sub(posA);
		// Convert T into A's coordinate frame
		float[] T = new float[] { limbo.dot(new PVector().set(axes_A[0])), 
				limbo.dot(new PVector().set(axes_A[1])), 
				limbo.dot(new PVector().set(axes_A[2])) };

		float radiA, radiB;

		for(int idx = 0; idx < absRotMatrix.length; ++idx) {

			if (idx == 0) {
				radiA = A.getDim(DimType.LENGTH) / 2f;
			} else if (idx == 1) {
				radiA = A.getDim(DimType.HEIGHT) / 2f;
			} else {
				radiA = A.getDim(DimType.WIDTH) / 2f;
			}

			radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[idx][0] + 
					(B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[idx][1] + 
					(B.getDim(DimType.WIDTH) / 2) * absRotMatrix[idx][2];

			// Check Ax, Ay, and Az
			if( Math.abs(T[idx]) > (radiA + radiB)) { return false; }
		}

		for(int idx = 0; idx < absRotMatrix[0].length; ++idx) {
			radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][idx] + 
					(A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][idx] + 
					(A.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][idx];

			if (idx == 0) {
				radiB = B.getDim(DimType.LENGTH) / 2f;
			} else if (idx == 1) {
				radiB = B.getDim(DimType.HEIGHT) / 2f;
			} else {
				radiB = B.getDim(DimType.WIDTH) / 2f;
			}

			float check =  Math.abs(T[0]*rotMatrix[0][idx] + 
					T[1]*rotMatrix[1][idx] + 
					T[2]*rotMatrix[2][idx]);

			// Check Bx, By, and Bz
			if(check > (radiA + radiB)) { return false; }
		}

		radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
		// Check axes Ax x Bx
		if( Math.abs(T[2] * rotMatrix[1][0] - T[1] * rotMatrix[2][0]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][1] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
		// Check axes Ax x By
		if( Math.abs(T[2] * rotMatrix[1][1] - T[1] * rotMatrix[2][1]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
		// Check axes Ax x Bz
		if( Math.abs(T[2] * rotMatrix[1][2] - T[1] * rotMatrix[2][2]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][0] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
		// Check axes Ay x Bx
		if( Math.abs(T[0] * rotMatrix[2][0] - T[2] * rotMatrix[0][0]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
		// Check axes Ay x By
		if( Math.abs(T[0] * rotMatrix[2][1] - T[2] * rotMatrix[0][1]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][0];
		// Check axes Ay x Bz
		if( Math.abs(T[0] * rotMatrix[2][2] - T[2] * rotMatrix[0][2]) > (radiA + radiB)) { return false; }


		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][0] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][1];
		// Check axes Az x Bx
		if( Math.abs(T[1] * rotMatrix[0][0] - T[0] * rotMatrix[1][0]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][0];
		// Check axes Az x By
		if( Math.abs(T[1] * rotMatrix[0][1] - T[0] * rotMatrix[1][1]) > (radiA + radiB)) { return false; }

		radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0];
		// Check axes Az x Bz
		if( Math.abs(T[1] * rotMatrix[0][2] - T[0] * rotMatrix[1][2]) > (radiA + radiB)) { return false; }

		return true;
	}
	
	private BoundingBox absOBB;
	private CoordinateSystem defaultOrientation;
	private Fixture reference;

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Part(String n, int fill, int strokeVal, float edgeLen) {
		super(n, new Box(fill, strokeVal, edgeLen));
		absOBB = new BoundingBox(OBB_DIM_SCALE * edgeLen);
		defaultOrientation = (CoordinateSystem) localOrientation.clone();
	}

	/**
	 * Creates a cylinder objects with the given colors and dimensions.
	 */
	public Part(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new Cylinder(fill, strokeVal, rad, hgt));
		absOBB = new BoundingBox(OBB_RAD_SCALE * rad, OBB_RAD_SCALE * rad,
				OBB_DIM_SCALE * hgt);
		defaultOrientation = (CoordinateSystem) localOrientation.clone();
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Part(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new Box(fill, strokeVal, len, hgt, wdh));
		absOBB = new BoundingBox(OBB_DIM_SCALE * len, OBB_DIM_SCALE * hgt,
				OBB_DIM_SCALE * wdh);
		defaultOrientation = (CoordinateSystem) localOrientation.clone();
	}

	/**
	 * Define a complex object as a part.
	 */
	public Part(String n, ModelShape model) {
		super(n, model);

		absOBB = new BoundingBox(model.getDim(DimType.LENGTH),
								 model.getDim(DimType.HEIGHT),
								 model.getDim(DimType.WIDTH));
		defaultOrientation = (CoordinateSystem) localOrientation.clone();
	}

	/**
	 * Creates a Part with the given name, shape, bounding-box dimensions,
	 * default orientation and fixture reference.
	 */
	public Part(String n, Shape s, PVector OBBDims, CoordinateSystem local,
			CoordinateSystem def, Fixture fixRef) {
		
		super(n, s, local);
		absOBB = new BoundingBox(OBB_DIM_SCALE * OBBDims.x,
				OBB_DIM_SCALE * OBBDims.y, OBB_DIM_SCALE * OBBDims.z);
		defaultOrientation = def;
		setFixtureRef(fixRef);
	}

	@Override
	public void applyCoordinateSystem() {
		absOBB.applyCoordinateSystem();
	}

	public void applyLocalCoordinateSystem() {
		super.applyCoordinateSystem();
	}

	@Override
	public Part clone() {
		// The new object's reference still points to the same fixture!
		return new Part(getName(), (Shape)getForm().clone(), getOBBDims().copy(),
				(CoordinateSystem)localOrientation.clone(), (CoordinateSystem)defaultOrientation.clone(), reference);
	}

	/**
	 * Determies if the given bounding box is colliding
	 * with this Part's bounding box.
	 */
	public boolean collision(BoundingBox obb) {
		return collision3D(absOBB, obb);
	}

	/**
	 * Determine if the given world object is colliding
	 * with this world object.
	 */
	public boolean collision(Part obj) {
		return collision3D(absOBB, obj.absOBB);
	}

	/**
	 * Determine if the given point is within
	 * this object's bounding box.
	 */
	public boolean collision(PVector point) {
		return absOBB.collision(point);
	}

	/**
	 * Draw both the object and its bounding box in its local
	 * orientation, in the local orientation of the part's
	 * fixture reference.
	 */
	@Override
	public void draw() {
		RobotRun.getInstance().pushMatrix();
		applyCoordinateSystem();
		getForm().draw();
		
		if (RobotRun.getInstance().areOBBsDisplayed()) {
			absOBB.getBox().draw();
		}
		
		RobotRun.getInstance().popMatrix();
	}

	public Fixture getFixtureRef() { return reference; }

	/**
	 * Get the dimensions of the part's bounding-box
	 */
	public PVector getOBBDims() {
		return absOBB.getDims();
	}
	
	public PVector getDefaultCenter() {
		return defaultOrientation.getOrigin();
	}

	public RMatrix getDefaultOrientationAxes() {
		return defaultOrientation.getAxes();
	}

	/**
	 * Sets the stroke color of the world's bounding-box
	 * to the given value.
	 */
	public void setBBColor(int newColor) {
		absOBB.setColor(newColor);
	}

	@Override
	public void setCoordinateSystem() {
		absOBB.setCoordinateSystem();
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

	public void setLocalCoordinateSystem() {
		super.setCoordinateSystem();
		updateAbsoluteOrientation();
	}
	
	public void setDefaultCenter(PVector newCenter) {
		defaultOrientation.setOrigin(newCenter);
	}
	
	public void setDefaultOrientationAxes(RMatrix newAxes) {
		defaultOrientation.setAxes(newAxes);
	}

	@Override
	public void setLocalOrientationAxes(RMatrix newAxes) {
		super.setLocalOrientationAxes(newAxes);
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

	/**
	 * Update the Part's absolute (or world) orientation
	 * based om its local orientation and fixture
	 * reference's orientation.
	 */
	public void updateAbsoluteOrientation() {
		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();

		if (reference != null) {
			reference.applyCoordinateSystem();
		}

		super.applyCoordinateSystem();
		absOBB.setCoordinateSystem();
		RobotRun.getInstance().popMatrix();
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
		Shape s = getForm();
		float minAddition = Float.MAX_VALUE;

		if (s instanceof Box || s instanceof ModelShape) {
			// Update the OBB dimensions for a box or complex part
			minAddition = 0.1f * PApplet.min(s.getDim(DimType.LENGTH),
					PApplet.min(s.getDim(DimType.HEIGHT),
							s.getDim(DimType.WIDTH)));

			absOBB.setDim(s.getDim(DimType.LENGTH) + minAddition, DimType.LENGTH);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.WIDTH) + minAddition, DimType.WIDTH);

		} else if (s instanceof Cylinder) {
			// Update the OBB dimensions for a cylindrical part
			minAddition =  PApplet.min(0.12f * s.getDim(DimType.RADIUS),
					0.1f * s.getDim(DimType.HEIGHT));

			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.LENGTH);
			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.WIDTH); 
		}
	}
}