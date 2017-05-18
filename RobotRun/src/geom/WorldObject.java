package geom;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Any object in the World other than the Robot.
 */
public abstract class WorldObject implements Cloneable {
	private String name;
	private Shape form;
	protected CoordinateSystem localOrientation;

	public WorldObject() {
		name = "Object";
		form = new Box();
		localOrientation = new CoordinateSystem();
	}

	public WorldObject(String n, Shape f) {
		name = n;
		form = f;
		localOrientation = new CoordinateSystem();
	}

	public WorldObject(String n, Shape f, CoordinateSystem cs) {
		name = n;
		form = f;
		localOrientation = cs;
	}

	/**
	 * Apply the local Coordinate System of the World Object.
	 */
	public void applyCoordinateSystem() {
		localOrientation.apply();
	}
	
	@Override
	public abstract Object clone();

	/**
	 * Returns a list of values with short prefix labels, which descibe
	 * the dimensions of the this world object's shape (except for Model
	 * shapes, because their dimensions are unknown).
	 * 
	 * @returning  A non-null, variable length string array
	 */
	public String[] dimFieldsToStringArray() {
		String[] fields;

		if (form instanceof Box) {
			fields = new String[3];
			// Add the box's length, height, and width values
			fields[0] = String.format("L: %4.3f", form.getDim(DimType.LENGTH));
			fields[1] = String.format("H: %4.3f", form.getDim(DimType.HEIGHT));
			fields[2] = String.format("W: %4.3f", form.getDim(DimType.WIDTH));

		} else if (form instanceof Cylinder) {
			fields = new String[2];
			// Add the cylinder's radius and height values
			fields[0] = String.format("R: %4.3f", form.getDim(DimType.RADIUS));
			fields[1] = String.format("H: %4.3f", form.getDim(DimType.HEIGHT));

		} else if (form instanceof ModelShape) {

			if (this instanceof Part)  {
				// Use bounding-box dimensions instead
				fields = new String[4];
				PVector dims = ((Part)this).getOBBDims();

				fields[0] = String.format("S: %4.3f", form.getDim(DimType.SCALE));
				fields[1] = String.format("L: %4.3f", dims.x);
				fields[2] = String.format("H: %4.3f", dims.y);
				fields[3] = String.format("W: %4.3f", dims.z);

			} else if (this instanceof Fixture) {
				fields = new String[1];
				fields[0] = String.format("S: %4.3f", form.getDim(DimType.SCALE));

			} else {
				// No dimensios to display
				fields = new String[0];
			}

		} else {
			// Invalid shape
			fields = new String[0];
		}

		return fields;
	}

	/**
	 * Draw the world object in its local orientation.
	 */
	public void draw() {
		RobotRun.getInstance().pushMatrix();
		// Draw shape in its own coordinate system
		applyCoordinateSystem();
		form.draw();
		RobotRun.getInstance().popMatrix();
	}

	// Getter and Setter methods for the World Object's local orientation, name, and form

	public Shape getForm() { return form; }

	public PVector getLocalCenter() {
		return localOrientation.getOrigin();
		}

	public RMatrix getLocalOrientationAxes() {
		return localOrientation.getAxes();
	}

	public String getName() { return name; }

	/**
	 * Transform the World Object's local Coordinate System to
	 * the current transformation matrix.
	 */
	public void setCoordinateSystem() {
		localOrientation = new CoordinateSystem();
	}

	public void setLocalCenter(PVector newCenter) {
		localOrientation.setOrigin(newCenter);
	}
	
	public void setLocalOrientationAxes(RMatrix newAxes) {
		localOrientation.setAxes(newAxes);
	}

	public void setName(String newName) { name = newName; }

	@Override
	public String toString() { return name; }
	/**
	 * Updates all non-null values of the object's center position.
	 * If a given value is null, then the origin value remains unchanged.
	 * 
	 * @param x  The new x value*
	 * @param y  The new y value*
	 * @param z  The new z value*
	 *           *null indicates that the origin value will remain unchanged
	 */
	public void updateLocalCenter(Float x, Float y, Float z) {
		PVector center = localOrientation.getOrigin();

		if (x != null) {
			// Update x value
			center.x = x;
		}
		if (y != null) {
			// Update y value
			center.y = y;
		}
		if (z != null) {
			// update z value
			center.z = z;
		}
	}
}