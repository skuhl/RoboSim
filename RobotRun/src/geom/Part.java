package geom;
import global.Fields;
import global.RMath;
import processing.core.PApplet;
import processing.core.PGraphics;
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
		OBB_DIM_SCALE = 0.05f;
		OBB_RAD_SCALE = 0.07f;
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
				rotMatrix[v][u] = axes_A[0][v] * axes_B[0][u] +  axes_A[1][v] * axes_B[1][u] +  axes_A[2][v] * axes_B[2][u];
				// Add offset for values close to zero (parallel axes)
				absRotMatrix[v][u] = Math.abs(rotMatrix[v][u]) + 0.00000000175f;
			}
		}

		// T = B's position - A's
		PVector posA = A.getCenter().copy();
		PVector posB = B.getCenter().copy();
		// Convert T into A's coordinate frame
		PVector limbo = RMath.rotateVector(posB.sub(posA), axes_A);
		float[] T = new float[] { limbo.x, limbo.y, limbo.z };

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
		absOBB = new BoundingBox(edgeLen);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Creates a cylinder objects with the given colors and dimensions.
	 */
	public Part(String n, int fill, int strokeVal, float rad, float hgt) {
		super(n, new Cylinder(fill, strokeVal, rad, hgt));
		absOBB = new BoundingBox(rad, rad, hgt);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Part(String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(n, new Box(fill, strokeVal, len, hgt, wdh));
		absOBB = new BoundingBox(len, hgt, wdh);
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Define a complex object as a part.
	 */
	public Part(String n, ModelShape model) {
		super(n, model);
		absOBB = new BoundingBox(model.getDim(DimType.LENGTH),
								 model.getDim(DimType.HEIGHT),
								 model.getDim(DimType.WIDTH));
		defaultOrientation = localOrientation.clone();
		updateOBBDims();
	}

	/**
	 * Creates a Part with the given name, shape, bounding-box dimensions,
	 * default orientation and fixture reference.
	 */
	public Part(String n, Shape s, PVector OBBDims, CoordinateSystem local,
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
		return new Part(getName(), getForm().clone(), getOBBDims().copy(),
				localOrientation.clone(), defaultOrientation.clone(),
				reference);
	}

	/**
	 * Determines if the given bounding box is colliding
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
	
	@Override
	public PVector collision(Ray ray) {
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
		
		getForm().draw(g);
		
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

	public Fixture getFixtureRef() { return reference; }
	
	/**
	 * @return	The bounding box of the part
	 */
	public Box getOBBFrame() {
		return absOBB.getFrame();
	}
	
	/**
	 * Get the dimensions of the part's bounding-box
	 */
	public PVector getOBBDims() {
		return absOBB.getDims();
	}
	
	/**
	 * @return	The absolute orientation of the part (without respect to its
	 * 			fixture reference)
	 */
	public RMatrix getOrientation() {
		return absOBB.getOrientationAxes();
	}
	
	public PVector getDefaultCenter() {
		return defaultOrientation.getOrigin();
	}

	public RMatrix getDefaultOrientation() {
		return defaultOrientation.getAxes();
	}
	
	@Override
	public void rotateAroundAxis(PVector axis, float angle) {
		
		if (reference != null) {
			// rotate with respect to the part's fixture reference
			RMatrix refRMat = reference.getLocalOrientation();
			axis = RMath.rotateVector(axis, refRMat.getFloatData());
		}
		
		RMatrix orientation = localOrientation.getAxes();
		RMatrix rotation = RMath.matFromAxisAndAngle(axis, angle);
		
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
	
	public void setDefaultCenter(PVector newCenter) {
		defaultOrientation.setOrigin(newCenter);
	}
	
	public void setDefaultOrientation(RMatrix newAxes) {
		defaultOrientation.setAxes(newAxes);
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
			delta = RMath.rotateVector(delta, refRMat.getFloatData());
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
			
			origin = RMath.rotateVector(origin, refRMat.getInverse().getFloatData());
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
		Shape s = getForm();
		float minAddition = Float.MAX_VALUE;

		if (s instanceof Box || s instanceof ModelShape) {
			// Update the OBB dimensions for a box or complex part
			minAddition = OBB_DIM_SCALE * PApplet.min(s.getDim(DimType.LENGTH),
					PApplet.min(s.getDim(DimType.HEIGHT),
							s.getDim(DimType.WIDTH)));

			absOBB.setDim(s.getDim(DimType.LENGTH) + minAddition, DimType.LENGTH);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.WIDTH) + minAddition, DimType.WIDTH);

		} else if (s instanceof Cylinder) {
			// Update the OBB dimensions for a cylindrical part
			minAddition =  PApplet.min(OBB_RAD_SCALE * s.getDim(DimType.RADIUS),
					OBB_DIM_SCALE * s.getDim(DimType.HEIGHT));

			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.LENGTH);
			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.WIDTH); 
		}
	}
}