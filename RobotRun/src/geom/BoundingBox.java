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
	public PVector collision(Ray ray) {
		PVector origin = localOrientation.getOrigin();
		float[][] axes = localOrientation.getAxes().getFloatData();
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
				String msg = String.format("G = 0 for A=%f R=%s",
						planeAxes[planeAxis] * dims[planeAxis], ray);
				throw new ArithmeticException(msg);
				
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