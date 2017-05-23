package geom;
import java.util.Arrays;

import global.MyFloatFormat;
import global.RMath;
import processing.core.PVector;
import robot.RobotRun;

/**
 * Any object in the World other than the Robot.
 */
public abstract class WorldObject implements Cloneable {
	protected CoordinateSystem localOrientation;
	private String name;
	private Shape form;
	
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
	
	/**
	 * Calculates the point of collision between this world object and the
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
		
		float[] dims = form.getDimArray();
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
				System.err.printf("G = 0 for A=%f R=%s\n", planeAxes[planeAxis] *
						dims[planeAxis], ray);
				
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
	
	@Override
	public abstract WorldObject clone();

	/**
	 * Returns a list of values with short prefix labels, which describe
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
			fields[0] = "L: " + MyFloatFormat.format(form.getDim(DimType.LENGTH));
			fields[1] = "H: " + MyFloatFormat.format(form.getDim(DimType.HEIGHT));
			fields[2] = "W: " + MyFloatFormat.format(form.getDim(DimType.WIDTH));

		} else if (form instanceof Cylinder) {
			fields = new String[2];
			// Add the cylinder's radius and height values
			fields[0] = "R: " + MyFloatFormat.format(form.getDim(DimType.RADIUS));
			fields[1] = "H: " + MyFloatFormat.format(form.getDim(DimType.HEIGHT));

		} else if (form instanceof ModelShape) {

			if (this instanceof Part)  {
				// Use bounding-box dimensions instead
				fields = new String[4];
				PVector dims = ((Part)this).getOBBDims();

				fields[0] = "S: " + MyFloatFormat.format(form.getDim(DimType.SCALE));
				fields[1] = "L: " + MyFloatFormat.format(dims.x);
				fields[2] = "H: " + MyFloatFormat.format(dims.y);
				fields[3] = "W: " + MyFloatFormat.format(dims.z);

			} else if (this instanceof Fixture) {
				fields = new String[1];
				fields[0] = "S: " + MyFloatFormat.format(form.getDim(DimType.SCALE));

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
	
	public int getObjectID() { return form.getID(); }

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