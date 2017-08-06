package geom;

import global.Fields;
import global.RMath;
import processing.core.PVector;

/**
 * A box object with its own local Coordinate system.
 */
public class BoundingBox {
	private RBox boxFrame;
	private CoordinateSystem localOrientation;
	
	/**
	 * Create a bounding box with a default dimension.
	 */
	public BoundingBox() {
		localOrientation = new CoordinateSystem();
		boxFrame = new RBox(Fields.OBB_DEFAULT, 10f);
	}

	/**
	 * Create a bounding box with the given dimension.
	 * 
	 * @param	The edge length of the bounding box
	 */
	public BoundingBox(float edgeLen) {
		localOrientation = new CoordinateSystem();
		boxFrame = new RBox(Fields.OBB_DEFAULT, edgeLen);
	}

	/**
	 * Creates a bounding box with the given dimensions.
	 * 
	 * @param len	The length of the box
	 * @param hgt	The height of the box
	 * @param wdh	The width of the box
	 */
	public BoundingBox(float len, float hgt, float wdh) {
		localOrientation = new CoordinateSystem();
		boxFrame = new RBox(Fields.OBB_DEFAULT, len, hgt, wdh);
	}

	/**
	 * Creates a bounding box with the given coordinate system and dimensions.
	 * 
	 * @param boxFrame			The frame of the bounding box
	 * @param localOrientation	The orientation of the bounding box
	 */
	public BoundingBox(RBox boxFrame, CoordinateSystem localOrientation) {
		this.localOrientation = localOrientation;
		this.boxFrame = boxFrame;
	}
	
	/**
	 * Return a replicate of this world object's Bounding Box
	 */
	@Override
	public BoundingBox clone() {
		return new BoundingBox( boxFrame.clone(),
				localOrientation.clone() );
	}

	/**
	 * Calculates the point of collision between this bounding box and the
	 * given ray that is closest to the ray. If no collision exists, then null
	 * is returned.
	 * 
	 * Inspired by:
	 * https://stackoverflow.com/questions/5666222/3d-line-plane-intersection
	 * http://math.mit.edu/classes/18.02/notes/lecture5compl-09.pdf
	 * 
	 * @param ray	A ray with a defined origin and direction
	 * @return		The point of collision between this bounding box and the
	 * 				given ray, closest to the ray
	 */
	public PVector collision(RRay ray) {
		PVector origin = localOrientation.getOrigin();
		RMatrix axes = localOrientation.getAxes();
		// Transform ray into the coordinate frame of the bounding box
		PVector rayOrigin = RMath.rotateVector(PVector.sub(ray.getOrigin(), origin), axes);
		PVector rayDirect = RMath.rotateVector(ray.getDirection(), axes);
		
		float[] dims = boxFrame.getDimArray();
		dims[0] /= 2f;
		dims[1] /= 2f;
		dims[2] /= 2f;
		
		int[] planeAxes = new int[] {
			(rayOrigin.x < 0) ? -1 : 1,
			(rayOrigin.y < 0) ? -1 : 1,
			(rayOrigin.z < 0) ? -1 : 1
		};
		
		for (int planeAxis = 0; planeAxis < planeAxes.length; ++planeAxis) {
			float E, G;
			
			if (planeAxis == 0) {
				E = planeAxes[0] * (rayOrigin.x - (planeAxes[0] * dims[0]));
				G = planeAxes[0] * rayDirect.x;
				
			} else if (planeAxis == 1) {
				E = planeAxes[1] * (rayOrigin.y - (planeAxes[1] * dims[1]));
				G = planeAxes[1] * rayDirect.y;
				
			} else {
				E = planeAxes[2] * (rayOrigin.z - (planeAxes[2] * dims[2]));
				G = planeAxes[2] * rayDirect.z;
				
			}
			
			if (G == 0f) {
				/**
				Fields.debug("G = 0 for A=%f R=%s\n",
							planeAxes[planeAxis] * dims[planeAxis], ray);
				/**/
				
			} else {
				float t = -E / G;
				
				if (t >= 0) {
					PVector ptOnRay = PVector.add(rayOrigin, PVector.mult(rayDirect, t));
					float[] ptOnRayArray = new float[] { ptOnRay.x, ptOnRay.y, ptOnRay.z };
					int dimToCheck0 = (planeAxis + 1) % 3;
					int dimToCheck1 = (dimToCheck0 + 1) % 3;
					
					if (ptOnRayArray[dimToCheck0] >= -dims[dimToCheck0] &&
						ptOnRayArray[dimToCheck0] <= dims[dimToCheck0] &&
						ptOnRayArray[dimToCheck1] >= -dims[dimToCheck1] &&
						ptOnRayArray[dimToCheck1] <= dims[dimToCheck1]) {
						
						// Collision exists
						return PVector.add(ray.getOrigin(),  PVector.mult(ray.getDirection(), t));
						
					}
				}
			}
		}
		
		// No collision
		return null;
	}
	
	/**
	 * This algorithm uses the Separating Axis Theorem to project radi of each
	 * bounding box on to several axes to determine if a there is any overlap
	 * between them. The method strongly resembles the method outlined in
	 * Section 4.4 of "Real Time Collision Detection" by Christer Ericson
	 *
	 * @param B  Some other bounding box associated with an object in space
	 * @return   Whether this and the given bounding box intersect
	 */
	public boolean collision3D(BoundingBox B) {
		// Rows are x, y, z axis vectors for this and B: Ax, Ay, Az, Bx, By, and Bz
		float[][] axes_A = getOrientationAxes().getDataF();
		float[][] axes_B = B.getOrientationAxes().getDataF();

		// Rotation matrices to convert B into this coordinate system
		float[][] rotMatrix = new float[3][3];
		float[][] absRotMatrix = new float[3][3];

		for(int v = 0; v < axes_A.length; v += 1) {
			for(int u = 0; u < axes_B.length; u += 1) {
				// PLEASE do not change to matrix multiplication
				rotMatrix[v][u] = axes_A[0][v] * axes_B[0][u] +  axes_A[1][v] *
						axes_B[1][u] +  axes_A[2][v] * axes_B[2][u];
				// Add offset for values close to zero (parallel axes)
				absRotMatrix[v][u] = Math.abs(rotMatrix[v][u]) + 0.00000000175f;
			}
		}

		// T = B's position - position of this
		PVector posA = getCenter().copy();
		PVector posB = B.getCenter().copy();
		// Convert T into coordinate frame of this
		PVector limbo = RMath.rotateVector(posB.sub(posA), axes_A);
		float[] T = new float[] { limbo.x, limbo.y, limbo.z };

		float radiA, radiB;

		for(int idx = 0; idx < absRotMatrix.length; ++idx) {

			if (idx == 0) {
				radiA = getDim(DimType.LENGTH) / 2f;
			} else if (idx == 1) {
				radiA = getDim(DimType.HEIGHT) / 2f;
			} else {
				radiA = getDim(DimType.WIDTH) / 2f;
			}

			radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[idx][0] + 
					(B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[idx][1] + 
					(B.getDim(DimType.WIDTH) / 2) * absRotMatrix[idx][2];

			// Check Ax, Ay, and Az
			if( Math.abs(T[idx]) > (radiA + radiB)) { return false; }
		}

		for(int idx = 0; idx < absRotMatrix[0].length; ++idx) {
			radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[0][idx] + 
					(getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][idx] + 
					(getDim(DimType.WIDTH) / 2) * absRotMatrix[2][idx];

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

		radiA = (getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
		// Check axes Ax x Bx
		if( Math.abs(T[2] * rotMatrix[1][0] - T[1] * rotMatrix[2][0]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][1] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
		// Check axes Ax x By
		if( Math.abs(T[2] * rotMatrix[1][1] - T[1] * rotMatrix[2][1]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[1][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
		// Check axes Ax x Bz
		if( Math.abs(T[2] * rotMatrix[1][2] - T[1] * rotMatrix[2][2]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[2][0] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
		// Check axes Ay x Bx
		if( Math.abs(T[0] * rotMatrix[2][0] - T[2] * rotMatrix[0][0]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
		// Check axes Ay x By
		if( Math.abs(T[0] * rotMatrix[2][1] - T[2] * rotMatrix[0][1]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (getDim(DimType.WIDTH) / 2) * absRotMatrix[0][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][0];
		// Check axes Ay x Bz
		if( Math.abs(T[0] * rotMatrix[2][2] - T[2] * rotMatrix[0][2]) > (radiA + radiB)) { return false; }


		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[1][0] + (getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
		radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][1];
		// Check axes Az x Bx
		if( Math.abs(T[1] * rotMatrix[0][0] - T[0] * rotMatrix[1][0]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][1];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][0];
		// Check axes Az x By
		if( Math.abs(T[1] * rotMatrix[0][1] - T[0] * rotMatrix[1][1]) > (radiA + radiB)) { return false; }

		radiA = (getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2];
		radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0];
		// Check axes Az x Bz
		if( Math.abs(T[1] * rotMatrix[0][2] - T[0] * rotMatrix[1][2]) > (radiA + radiB)) { return false; }

		return true;
	}

	public PVector getCenter() { return localOrientation.getOrigin(); }

	/**
	 * See Box.getDim()
	 */
	public float getDim(DimType dim) {
		return boxFrame.getDim(dim);
	}

	/**
	 * Returns the bounding-box's dimension in the
	 * form of a PVector: (length, height, width).
	 */
	public PVector getDims() {
		PVector dims = new PVector();
		dims.x = boxFrame.getDim(DimType.LENGTH);
		dims.y = boxFrame.getDim(DimType.HEIGHT);
		dims.z = boxFrame.getDim(DimType.WIDTH);
		return dims;
	}

	/**
	 * Return a reference to this bounding-box's box.
	 */
	public RBox getFrame() { return boxFrame; }

	public RMatrix getOrientationAxes() {
		return localOrientation.getAxes();
	}

	/**
	 * Reset the object's center point
	 */
	public void setCenter(PVector newCenter) {
		localOrientation.setOrigin(newCenter);
	}

	/**
	 * Sets the stroke color of this ounding-box
	 * to the given value.
	 */
	public void setColor(int newColor) {
		boxFrame.setStrokeValue(newColor);
	}
	
	/**
	 * Reset the bounding-box's coordinate system to the current
	 * transformation matrix.
	 */
	public void setCoordinateSystem(CoordinateSystem newCS) {
		localOrientation.setOrigin(newCS.getOrigin());
		localOrientation.setAxes(newCS.getAxes());
	}

	/**
	 * Sets the coordinate system of the bounding box based off the given
	 * transformation matrix.
	 * 
	 * @param tMat	A transformation matrix containing only rotations and
	 * 				translations
	 */
	public void setCoordinateSystem(RMatrix tMat) {
		PVector origin = localOrientation.getOrigin();
		origin.x = tMat.getEntryF(0, 3);
		origin.y = tMat.getEntryF(1, 3);
		origin.z = tMat.getEntryF(2, 3);
		
		localOrientation.setAxes( RMath.formRMat(tMat) );
	}

	/**
	 * See Box.setDim()
	 */
	public void setDim(Float newVal, DimType dim) {
		boxFrame.setDim(newVal, dim);
	}

	/**
	 * Sets all the dimension values of the
	 * bounding-box, where:
	 * X -> length
	 * Y -> height
	 * Z -> width
	 */
	public void setDims(PVector newDims) {
		boxFrame.setDim(newDims.x, DimType.LENGTH);
		boxFrame.setDim(newDims.y, DimType.HEIGHT);
		boxFrame.setDim(newDims.z, DimType.WIDTH);
	}

	/**
	 * Reset the object's orientation axes; the given rotation
	 * matrix should be in row major order!
	 */
	public void setOrientation(RMatrix newOrientation) {
		localOrientation.setAxes(newOrientation);
	}
}