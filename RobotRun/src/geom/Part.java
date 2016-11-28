package geom;
import processing.core.PVector;
import robot.BoundingBox;
import robot.CoordinateSystem;
import robot.Fixture;
import robot.RobotRun;

/**
 * Defines a world object, which has a shape, a bounding box and a reference to a fixture.
 * The bounding box holds the local coordinate system of the object.
 */
public class Part extends WorldObject {
	
	private final RobotRun robotRun;
	private BoundingBox absOBB;
	private Fixture reference;

	/**
	 * Create a cube object with the given colors and dimension
	 */
	public Part(RobotRun robotRun, String n, int fill, int strokeVal, float edgeLen) {
		super(robotRun, n, new Box(robotRun, fill, strokeVal, edgeLen));
		this.robotRun = robotRun;
		absOBB = new BoundingBox(this.robotRun, 1.1f * edgeLen);
	}

	/**
	 * Create a box object with the given colors and dimensions
	 */
	public Part(RobotRun robotRun, String n, int fill, int strokeVal, float len, float hgt, float wdh) {
		super(robotRun, n, new Box(robotRun, fill, strokeVal, len, hgt, wdh));
		this.robotRun = robotRun;
		absOBB = new BoundingBox(this.robotRun, 1.1f * len, 1.1f * hgt, 1.1f * wdh);
	}

	/**
	 * Creates a cylinder objects with the given colors and dimensions.
	 */
	public Part(RobotRun robotRun, String n, int fill, int strokeVal, float rad, float hgt) {
		super(robotRun, n, new Cylinder(robotRun, fill, strokeVal, rad, hgt));
		this.robotRun = robotRun;
		absOBB = new BoundingBox(this.robotRun, 2.12f * rad, 2.12f * rad, 1.1f * hgt);
	}

	/**
	 * Define a complex object as a partx.
	 */
	public Part(RobotRun robotRun, String n, ModelShape model) {
		super(robotRun, n, model);
		this.robotRun = robotRun;

		absOBB = new BoundingBox(this.robotRun, 1.1f * model.getDim(DimType.LENGTH),
				1.1f * model.getDim(DimType.HEIGHT),
				1.1f * model.getDim(DimType.WIDTH));
	}

	/**
	 * Creates a Part with the given name, shape, bounding-box dimensions, and fixture reference.
	 */
	public Part(RobotRun robotRun, String n, Shape s, PVector OBBDims, CoordinateSystem local, Fixture fixRef) {
		super(robotRun, n, s, local);
		this.robotRun = robotRun;
		absOBB = new BoundingBox(this.robotRun, OBBDims.x, OBBDims.y, OBBDims.z);
		setFixtureRef(fixRef);
	}

	@Override
	public void applyCoordinateSystem() {
		absOBB.applyCoordinateSystem();
	}

	@Override
	public void setCoordinateSystem() {
		absOBB.setCoordinateSystem();
	}

	/**
	 * Update the part's bounding box dimensions of the part based on the dimensions of its form.
	 */
	public void updateOBBDims() {
		Shape s = getForm();
		float minAddition = Float.MAX_VALUE;

		if (s instanceof Box || s instanceof ModelShape) {
			// Update the OBB dimensions for a box or complex part
			minAddition = 0.1f * RobotRun.min(s.getDim(DimType.LENGTH),
					RobotRun.min(s.getDim(DimType.HEIGHT),
							s.getDim(DimType.WIDTH)));

			absOBB.setDim(s.getDim(DimType.LENGTH) + minAddition, DimType.LENGTH);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.WIDTH) + minAddition, DimType.WIDTH);

		} else if (s instanceof Cylinder) {
			// Update the OBB dimensions for a cylindrical part
			minAddition =  RobotRun.min(0.12f * s.getDim(DimType.RADIUS),
					0.1f * s.getDim(DimType.HEIGHT));

			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.LENGTH);
			absOBB.setDim(2f * s.getDim(DimType.RADIUS) + minAddition, DimType.HEIGHT);
			absOBB.setDim(s.getDim(DimType.HEIGHT) + minAddition, DimType.WIDTH); 
		}
	}

	public void applyLocalCoordinateSystem() {
		super.applyCoordinateSystem();
	}

	public void setLocalCoordinateSystem() {
		super.setCoordinateSystem();
	}

	/**
	 * Draw both the object and its bounding box in its local
	 * orientaiton, in the local orientation of the part's
	 * fixture reference.
	 */
	public void draw() {
		this.robotRun.pushMatrix();
		applyCoordinateSystem();
		getForm().draw();
		if (this.robotRun.showOOBs) { absOBB.getBox().draw(); }
		this.robotRun.popMatrix();
	}

	/**
	 * Set the fixture reference of this part and
	 * update its absolute orientation.
	 */
	public void setFixtureRef(Fixture refFixture) {
		reference = refFixture;
		updateAbsoluteOrientation();
	}

	public Fixture getFixtureRef() { return reference; }

	/**
	 * Update the Part's absolute (or world) orientation
	 * based om its local orientation and fixture
	 * reference's orientation.
	 */
	public void updateAbsoluteOrientation() {
		this.robotRun.pushMatrix();
		this.robotRun.resetMatrix();

		if (reference != null) {
			reference.applyCoordinateSystem();
		}

		super.applyCoordinateSystem();
		absOBB.setCoordinateSystem();
		this.robotRun.popMatrix();
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
	 * Get the dimensions of the part's bounding-box
	 */
	public PVector getOBBDims() {
		return absOBB.getDims();
	}

	/**
	 * Return a reference to this object's bounding-box.
	 */ 
	private BoundingBox getOBB() { return absOBB; }

	/**
	 * Sets the stroke color of the world's bounding-box
	 * to the given value.
	 */
	public void setBBColor(int newColor) {
		absOBB.setColor(newColor);
	}

	/**
	 * Determine if the given world object is colliding
	 * with this world object.
	 */
	public boolean collision(Part obj) {
		return RobotRun.collision3D(absOBB, obj.getOBB());
	}

	/**
	 * Determies if the given bounding box is colliding
	 * with this Part's bounding box.
	 */
	public boolean collision(BoundingBox obb) {
		return RobotRun.collision3D(absOBB, obb);
	}

	/**
	 * Determine if the given point is within
	 * this object's bounding box.
	 */
	public boolean collision(PVector point) {
		return absOBB.collision(point);
	}

	@Override
	public void setLocalCenter(PVector newCenter) {
		super.setLocalCenter(newCenter);
		updateAbsoluteOrientation();
	}

	@Override
	public void updateLocalCenter(Float x, Float y, Float z) {
		super.updateLocalCenter(x, y, z);
		updateAbsoluteOrientation();
	}

	@Override
	public void setLocalOrientationAxes(float[][] newAxes) {
		super.setLocalOrientationAxes(newAxes);
		updateAbsoluteOrientation();
	}

	@Override
	public Object clone() {
		// The new object's reference still points to the same fixture!
		return new Part(this.robotRun, getName(), (Shape)getForm().clone(), getOBBDims().copy(), (CoordinateSystem)localOrientation.clone(), reference);
	}
}