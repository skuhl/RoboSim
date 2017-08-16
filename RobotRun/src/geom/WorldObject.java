package geom;

import global.Fields;
import global.DebugFloatFormat;
import global.RMath;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Any object in the World other than the Robot.
 */
public abstract class WorldObject implements Cloneable {
	protected CoordinateSystem localOrientation;
	private RShape model;
	private String name;
	
	public WorldObject() {
		name = "Object";
		model = new RBox();
		localOrientation = new CoordinateSystem();
	}

	public WorldObject(String n, RShape mdl) {
		name = n;
		model = mdl;
		localOrientation = new CoordinateSystem();
	}

	public WorldObject(String n, RShape mdl, CoordinateSystem cs) {
		name = n;
		model = mdl;
		localOrientation = cs;
	}
	
	@Override
	public abstract WorldObject clone();
	
	/**
	 * Creates an independent replica of this object with the given name.
	 * 
	 * @param name	The name for the copied object
	 * @return		The copy of this object
	 */
	public abstract WorldObject clone(String name);
	
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
	public PVector collision(RRay ray) {
		PVector origin = localOrientation.getOrigin();
		RMatrix axes = localOrientation.getAxes();
		// Transform ray into the coordinate frame of the bounding box
		PVector rayOrigin = RMath.rotateVector(PVector.sub(ray.getOrigin(), origin), axes);
		PVector rayDirect = RMath.rotateVector(ray.getDirection(), axes);
		
		PVector dims = model.getDims();
		dims.x /= 2f;
		dims.y /= 2f;
		dims.z /= 2f;
		
		int[] planeAxes = new int[] {
			(rayOrigin.x < 0) ? -1 : 1,
			(rayOrigin.y < 0) ? -1 : 1,
			(rayOrigin.z < 0) ? -1 : 1
		};
		
		for (int planeAxis = 0; planeAxis < planeAxes.length; ++planeAxis) {
			
			float E, G;
			
			if (planeAxis == 0) {
				E = planeAxes[0] * (rayOrigin.x - (planeAxes[0] * dims.x));
				G = planeAxes[0] * rayDirect.x;
				
			} else if (planeAxis == 1) {
				E = planeAxes[1] * (rayOrigin.y - (planeAxes[1] * dims.y));
				G = planeAxes[1] * rayDirect.y;
				
			} else {
				E = planeAxes[2] * (rayOrigin.z - (planeAxes[2] * dims.z));
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
					float[] dimArray = dims.array();
					PVector ptOnRay = PVector.add(rayOrigin, PVector.mult(rayDirect, t));
					float[] ptOnRayArray = new float[] { ptOnRay.x, ptOnRay.y, ptOnRay.z };
					int dimToCheck0 = (planeAxis + 1) % 3;
					int dimToCheck1 = (dimToCheck0 + 1) % 3;
					
					if (ptOnRayArray[dimToCheck0] >= -dimArray[dimToCheck0] &&
						ptOnRayArray[dimToCheck0] <= dimArray[dimToCheck0] &&
						ptOnRayArray[dimToCheck1] >= -dimArray[dimToCheck1] &&
						ptOnRayArray[dimToCheck1] <= dimArray[dimToCheck1]) {
						
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
	 * Returns a list of values with short prefix labels, which describe
	 * the dimensions of the this world object's shape (except for Model
	 * shapes, because their dimensions are unknown).
	 * 
	 * @returning  A non-null, variable length string array
	 */
	public String[] dimFieldsToStringArray() {
		String[] fields;

		if (model instanceof RBox) {
			fields = new String[3];
			// Add the box's length, height, and width values
			fields[0] = "L: " + DebugFloatFormat.format(model.getDim(DimType.LENGTH));
			fields[1] = "H: " + DebugFloatFormat.format(model.getDim(DimType.HEIGHT));
			fields[2] = "W: " + DebugFloatFormat.format(model.getDim(DimType.WIDTH));

		} else if (model instanceof RCylinder) {
			fields = new String[2];
			// Add the cylinder's radius and height values
			fields[0] = "R: " + DebugFloatFormat.format(model.getDim(DimType.RADIUS));
			fields[1] = "H: " + DebugFloatFormat.format(model.getDim(DimType.HEIGHT));

		} else if (model instanceof ComplexShape) {

			if (this instanceof Part)  {
				// Use bounding-box dimensions instead
				fields = new String[4];
				PVector dims = model.getDims();

				fields[0] = "S: " + DebugFloatFormat.format(model.getDim(DimType.SCALE));
				fields[1] = "L: " + DebugFloatFormat.format(dims.x);
				fields[2] = "H: " + DebugFloatFormat.format(dims.y);
				fields[3] = "W: " + DebugFloatFormat.format(dims.z);

			} else if (this instanceof Fixture) {
				fields = new String[1];
				fields[0] = "S: " + DebugFloatFormat.format(model.getDim(DimType.SCALE));

			} else {
				// No dimensions to display
				fields = new String[0];
			}

		} else {
			// Invalid shape
			fields = new String[0];
		}

		return fields;
	}
	
	/**
	 * Draws the world object in its own coordinate frame.
	 * 
	 * @param g	The graphics used to render the object
	 */
	public void draw(PGraphics g) {
		g.pushMatrix();
		Fields.transform(g, this.localOrientation);
		
		model.draw(g);
		
		g.popMatrix();
	}

	// Getter and Setter methods for the World Object's local orientation, name, and form

	public PVector getLocalCenter() {
		return localOrientation.getOrigin();
	}
	
	public RMatrix getLocalOrientation() {
		return localOrientation.getAxes();
	}
	
	public RShape getModel() { return model; }
	
	public String getName() { return name; }
	
	/**
	 * Rotates the world object about the axis represented by the given unit
	 * vector by the given angle value.
	 * 
	 * @param axis	The unit vector representing the axis of rotation
	 * @param theta	the angle of rotation around the given axis
	 */
	public void rotateAroundAxis(PVector axis, float angle) {
		
		RMatrix rotation = RMath.formRMat(axis, angle);
		RMatrix orientation = localOrientation.getAxes();
		
		localOrientation.setAxes( rotation.multiply(orientation) );
	}

	public void setLocalCenter(PVector newCenter) {
		localOrientation.setOrigin(newCenter);
	}
	
	public void setLocalOrientation(RMatrix newAxes) {
		localOrientation.setAxes(newAxes);
	}

	public void setName(String newName) { name = newName; }
	
	/**
	 * Moves the world object's center position by the given x, y, z
	 * translations. Although, none of the object's positions can be less than
	 * -9999f or greater than 9999f.
	 * 
	 * @param dx	The change in x position
	 * @param dy	The change in y position
	 * @param dz	The change in z position
	 */
	public void translate(float dx, float dy, float dz) {
		PVector center = localOrientation.getOrigin();
		// Apply translation with the limits for an object's position
		center.x = RMath.clamp(center.x + dx, -9999f, 9999f);
		center.y = RMath.clamp(center.y + dy, -9999f, 9999f);
		center.z = RMath.clamp(center.z + dz, -9999f, 9999f);
	}
	
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