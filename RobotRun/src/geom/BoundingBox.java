package geom;

import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import global.Fields;
import global.RMath;
import processing.core.PVector;
import robot.RobotRun;

/**
 * A box object with its own local Coordinate system.
 */
public class BoundingBox {
	private CoordinateSystem localOrientation;
	private Box boxFrame;

	/**
	 * Create a bounding box with a default dimension.
	 */
	public BoundingBox() {
		localOrientation = new CoordinateSystem();
		boxFrame = new Box(Fields.OBB_DEFAULT, 10f);
	}

	/**
	 * Create a bounding box with the given dimension.
	 * 
	 * @param	The edge length of the bounding box
	 */
	public BoundingBox(float edgeLen) {
		localOrientation = new CoordinateSystem();
		boxFrame = new Box(Fields.OBB_DEFAULT, edgeLen);
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
		boxFrame = new Box(Fields.OBB_DEFAULT, len, hgt, wdh);
	}
	
	/**
	 * Creates a bounding box with the given coordinate system and dimensions.
	 * 
	 * @param boxFrame			The frame of the bounding box
	 * @param localOrientation	The orientation of the bounding box
	 */
	public BoundingBox(Box boxFrame, CoordinateSystem localOrientation) {
		
		this.localOrientation = localOrientation;
		this.boxFrame = boxFrame;
	}
	
	public static void main(String[] args) {
		/**/
		RealMatrix m0 = new Array2DRowRealMatrix(
				new double[][] {
					{ -1,  0,  0 },
					{  0,  0, -1 },
					{  0,  1,  0 }
				}
		);
		
		RealMatrix m1 = new Array2DRowRealMatrix(
				new double[][] {
					{ -1,  0,  0 },
					{  0,  0,  1 },
					{  0, -1,  0 }
				}
		);
		
		RealMatrix m2 = new Array2DRowRealMatrix(
				new double[][] {
					{  0,  1,  0 },
					{  1,  0,  0 },
					{  0,  0, -1 }
				}
		);
		
		RealMatrix m3 = m2.multiply(m0);
		
		System.out.printf("M0:\n%s\nM1:\n%s\nM2:\n%s\nM3:\n%s\n",
				RMath.matrixToString(m0), RMath.matrixToString(m1),
				RMath.matrixToString(m2), RMath.matrixToString(m3));
		
		/**
		RMatrix rotMatrix = new RMatrix(new float[][] {
			{ 1, 2, 3 },
			{ 3, 4, 5 },
			{ 6, 7, 8 }
		});
		
		RMatrix tMatrix = RMath.transformationMatrix(new PVector(-15, 4, 35), rotMatrix);
		
		System.out.printf("%s\n%s\n", rotMatrix.toString(), tMatrix.toString());
		
		
		PVector v = new PVector(-13, 5, 11);
		PVector u = RMath.rotateVector(v, Fields.WORLD_AXES);
		
		PVector w = new PVector(10, -15, 20);
		PVector y = RMath.rotateVector(w, Fields.NATIVE_AXES);
		
		System.out.printf("v: %s\nu: %s\nw: %s\ny: %s\n", v, u, w, y);
		/**/
	}

	/**
	 * Apply the Coordinate System of the bounding-box onto the
	 * current transformation matrix.
	 */
	public void applyCoordinateSystem() {
		localOrientation.apply();
	}

	/**
	 * Return a replicate of this world object's Bounding Box
	 */
	@Override
	public BoundingBox clone() {
		return new BoundingBox( boxFrame.clone(),
				localOrientation.clone() );
	}
	
	public PVector collision(Ray ray) {
		Float rayWeight = null;
		PVector origin = localOrientation.getOrigin();
		float[][] axes = localOrientation.getAxes().getFloatData();
		// Transform ray into the coordinate frame of the bounding box
		PVector rayOrigin = RMath.rotateVector(PVector.sub(ray.getOrigin(), origin), axes);
		PVector rayDirect = RMath.rotateVector(ray.getDirection(), axes);
		
		float[] dims = boxFrame.getDimArray();
		
		dims[0] /= 2f;
		dims[1] /= 2f;
		dims[2] /= 2f;
		
		// Calculate the points on the planes of each of the bounding box's sides
		PVector[] P0 = new PVector[] {
				
				new PVector(dims[0], 0f, 0f),
				new PVector(0f, dims[1], 0f),
				new PVector(0f, 0f, dims[2]),
				new PVector(-dims[0], 0f, 0f),
				new PVector(0f, -dims[1], 0f),
				new PVector(0f, 0f, -dims[2]),
		};
		
		// Calculate the vectors normal to each of the bounding boxe's sides
		PVector[] N = new PVector[] {
				
				new PVector(1, 0, 0),
				new PVector(0, 1, 0),
				new PVector(0, 0, 1),
				new PVector(-1, 0, 0),
				new PVector(0, -1, 0),
				new PVector(0, 0, -1)
				
		};
		
		for (int c = 0; c < 6; ++c) {
			
			if (PVector.dot(N[c], rayOrigin) == 0) {
				/* The plane formed by P0[c] and N[c] runs parallel to the ray
				 * formed by the given ray */
				continue;
			}
			
			float E = N[c].x * (rayOrigin.x - P0[c].x) +
					  N[c].y * (rayOrigin.y - P0[c].y) +
					  N[c].z * (rayOrigin.z - P0[c].z);
			
			float G = N[c].x * rayDirect.x +
					  N[c].y * rayDirect.y +
					  N[c].z * rayDirect.z;
			
			if (G == 0f) {
				String msg = String.format("G = 0 for N=%s P0=%s R=%s",
						N[c], P0[c], ray);
				throw new ArithmeticException(msg);
			}
			
			float t = -E / G;
			
			if (t < 0) {
				// t < 0 -> the collision would occur before the origin of the ray
				System.out.printf("t = %f for N=%s P0=%s R=%s\n", t, N[c], P0[c], ray);
				
			} else {
				PVector ptOnRay = PVector.add(rayOrigin, PVector.mult(rayDirect, t));
				float[] ptOnRayArray = new float[] { ptOnRay.x, ptOnRay.y, ptOnRay.z };
				int dimToCheck0 = (c + 1) % 3;
				int dimToCheck1 = (dimToCheck0 + 1) % 3;
				
				if (ptOnRayArray[dimToCheck0] >= -dims[dimToCheck0] && ptOnRayArray[dimToCheck0] <= dims[dimToCheck0] &&
					ptOnRayArray[dimToCheck1] >= -dims[dimToCheck1] && ptOnRayArray[dimToCheck1] <= dims[dimToCheck1]) {
					
					if (rayWeight == null) {
						rayWeight = t;
						
					} else {
						PVector collision = PVector.add(rayOrigin, PVector.mult(rayDirect, rayWeight));
						// Find the closest collision
						if (PVector.dist(rayOrigin, ptOnRay) < PVector.dist(rayOrigin, collision)) {
							rayWeight = t;
						}
					}
					
				} else {
					// Point is outside the bounds of the bounding box
					System.out.printf("Out of bounds: origin=%s dim=%s pt=%s\n",
							origin, Arrays.toString(dims), ptOnRay);
				}
			}
			
		}
		
		if (rayWeight == null) {
			// No collision
			return null;
			
		} else {
			/**/
			return PVector.add(ray.getOrigin(),  PVector.mult(ray.getDirection(), rayWeight));
			
			/**
			PVector collision = PVector.add(ray.getOrigin(),  PVector.mult(ray.getDirection(), rayWeight));
			RMatrix invAxes = localOrientation.getAxes().getInverse();
			
			return RMath.rotateVector(collision, invAxes.getFloatData()).add(origin);
			/**/
		}
	}

	/**
	 * Draw both the object and its bounding box;
	 */
	public void draw() {
		RobotRun.getInstance().pushMatrix();
		// Draw shape in its own coordinate system
		localOrientation.apply();
		boxFrame.draw();
		RobotRun.getInstance().popMatrix();
	}

	/**
	 * Return a reference to this bounding-box's box.
	 */
	public Box getBox() { return boxFrame; }

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
	public void setCoordinateSystem() {
		localOrientation = new CoordinateSystem();
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
	public void setOrientationAxes(RMatrix newOrientation) {
		localOrientation.setAxes(newOrientation);
	}
}