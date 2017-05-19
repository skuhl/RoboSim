package geom;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

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
		boxFrame = new Box(RobotRun.getInstance().color(0, 255, 0), 10f);
	}

	/**
	 * Create a bounding box with the given dimension.
	 * 
	 * @param	The edge length of the bounding box
	 */
	public BoundingBox(float edgeLen) {
		localOrientation = new CoordinateSystem();
		boxFrame = new Box(RobotRun.getInstance().color(0, 255, 0), edgeLen);
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
		boxFrame = new Box(RobotRun.getInstance().color(0, 255, 0), len, hgt, wdh);
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

	/**
	 * Determine of a single position, in Native Coordinates, is with
	 * the bounding box of the this world object.
	 */
	public boolean collision(PVector point) {
		// Convert the point to the current reference frame
		RMatrix tMatrix = RMath.transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
		PVector relPosition = RMath.vectorMatrixMult(point, RMath.invertHCMatrix(tMatrix));

		PVector OBBDim = getDims();
		// Determine if the point is within the bounding-box of this object
		boolean is_inside = relPosition.x >= -(OBBDim.x / 2f) && relPosition.x <= (OBBDim.x / 2f)
				&& relPosition.y >= -(OBBDim.y / 2f) && relPosition.y <= (OBBDim.y / 2f)
				&& relPosition.z >= -(OBBDim.z / 2f) && relPosition.z <= (OBBDim.z / 2f);

				return is_inside;
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