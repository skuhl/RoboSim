package robot;
import geom.Box;
import geom.DimType;
import processing.core.PVector;

/**
 * A box object with its own local Coordinate system.
 */
public class BoundingBox {
	/**
	 * 
	 */
	private RobotRun robotRun;
	private CoordinateSystem localOrientation;
	/* The origin of the bounding box's local Coordinate System */
	private Box boundingBox;

	/**
	 * Create a cube object with the given colors and dimension
	 * @param robotRun TODO
	 */
	public BoundingBox(RobotRun robotRun) {
		this.robotRun = robotRun;
		localOrientation = new CoordinateSystem(this.robotRun);
		boundingBox = new Box(this.robotRun, this.robotRun.color(0, 255, 0), 10f);
	}

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public BoundingBox(RobotRun robotRun, float edgeLen) {
		this.robotRun = robotRun;
		localOrientation = new CoordinateSystem(this.robotRun);
		boundingBox = new Box(this.robotRun, this.robotRun.color(0, 255, 0), edgeLen);
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public BoundingBox(RobotRun robotRun, float len, float hgt, float wdh) {
		this.robotRun = robotRun;
		localOrientation = new CoordinateSystem(this.robotRun);
		boundingBox = new Box(this.robotRun, this.robotRun.color(0, 255, 0), len, hgt, wdh);
	}

	/**
	 * Apply the Coordinate System of the bounding-box onto the
	 * current transformation matrix.
	 */
	public void applyCoordinateSystem() {
		localOrientation.apply();
	}

	/**
	 * Reset the bounding-box's coordinate system to the current
	 * transformation matrix.
	 */
	public void setCoordinateSystem() {
		localOrientation = new CoordinateSystem(this.robotRun);
	}

	/**
	 * Draw both the object and its bounding box;
	 */
	public void draw() {
		this.robotRun.pushMatrix();
		// Draw shape in its own coordinate system
		localOrientation.apply();
		boundingBox.draw();
		this.robotRun.popMatrix();
	}

	/**
	 * Reset the object's center point
	 */
	public void setCenter(PVector newCenter) {
		localOrientation.setOrigin(newCenter);
	}

	public PVector getCenter() { return localOrientation.getOrigin(); }

	/**
	 * Reset the object's orientation axes; the given rotation
	 * matrix should be in row major order!
	 */
	public void setOrientationAxes(float[][] newOrientation) {
		localOrientation.setAxes(newOrientation);
	}

	public float[][] getOrientationAxes() {
		return localOrientation.getAxes();
	}

	/**
	 * Sets the stroke color of this ounding-box
	 * to the given value.
	 */
	public void setColor(int newColor) {
		boundingBox.setStrokeValue(newColor);
	}

	/**
	 * See Box.setDim()
	 */
	public void setDim(Float newVal, DimType dim) {
		boundingBox.setDim(newVal, dim);
	}

	/**
	 * See Box.getDim()
	 */
	public float getDim(DimType dim) {
		return boundingBox.getDim(dim);
	}

	/**
	 * Sets all the dimension values of the
	 * bounding-box, where:
	 * X -> length
	 * Y -> height
	 * Z -> width
	 */
	public void setDims(PVector newDims) {
		boundingBox.setDim(newDims.x, DimType.LENGTH);
		boundingBox.setDim(newDims.y, DimType.HEIGHT);
		boundingBox.setDim(newDims.z, DimType.WIDTH);
	}

	/**
	 * Returns the bounding-box's dimension in the
	 * form of a PVector: (length, height, width).
	 */
	public PVector getDims() {
		PVector dims = new PVector();
		dims.x = boundingBox.getDim(DimType.LENGTH);
		dims.y = boundingBox.getDim(DimType.HEIGHT);
		dims.z = boundingBox.getDim(DimType.WIDTH);
		return dims;
	}

	/**
	 * Return a reference to this bounding-box's box.
	 */
	public Box getBox() { return boundingBox; }

	/**
	 * Determine of a single position, in Native Coordinates, is with
	 * the bounding box of the this world object.
	 */
	public boolean collision(PVector point) {
		// Convert the point to the current reference frame
		float[][] tMatrix = this.robotRun.transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
		PVector relPosition = this.robotRun.transformVector(point, this.robotRun.invertHCMatrix(tMatrix));

		PVector OBBDim = getDims();
		// Determine if the point iw within the bounding-box of this object
		boolean is_inside = relPosition.x >= -(OBBDim.x / 2f) && relPosition.x <= (OBBDim.x / 2f)
				&& relPosition.y >= -(OBBDim.y / 2f) && relPosition.y <= (OBBDim.y / 2f)
				&& relPosition.z >= -(OBBDim.z / 2f) && relPosition.z <= (OBBDim.z / 2f);

				return is_inside;
	}

	/**
	 * Return a replicate of this world object's Bounding Box
	 */
	public BoundingBox clone() {
		this.robotRun.pushMatrix();
		localOrientation.apply();
		PVector dims = getDims();
		BoundingBox copy = new BoundingBox(this.robotRun, dims.x, dims.y, dims.z);
		copy.setColor( boundingBox.getStrokeValue() );
		this.robotRun.popMatrix();

		return copy;
	}
}