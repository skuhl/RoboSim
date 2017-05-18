package ui;

import global.MyFloatFormat;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * A class designed which contains the camera transformation values
 * and the methods to manipulate apply the Camera's transformation.
 */
public class Camera {
	private static final float MIN_SCALE = 0.25f, MAX_SCALE = 8f;
	
	private PVector position;
	// Rotations in X, Y, Z in radians
	private PVector orientation;
	private float scale;
	
	/**
	 * Creates a camera with the default position, orientation and scale.
	 */
	public Camera() {
		position = new PVector(0f, 0f, 0f);
		orientation = new PVector(0f, 0f, 0f);
		scale = 2f;
	}

	/**
	 * Returns an independent replica of the Camera object.
	 */
	@Override
	public Camera clone() {
		Camera copy = new Camera();
		// Copy position, orientation, and scale
		copy.position = position.copy();
		copy.orientation = orientation.copy();
		copy.scale = scale;

		return copy;
	}

	public PVector getOrientation() { return orientation.copy(); }
	public PVector getPosition() { return position.copy(); }
	public float getScale() { return scale; }

	/**
	 * Return the camera perspective to the
	 * default position, orientation and scale.
	 */
	public void reset() {
		position.x = 0f;
		position.y = 0f;
		position.z = 0f;
		orientation.x = 0f;
		orientation.y = 0f;
		orientation.z = 0f;
		scale = 2f;
	}

	/**
	 * Rotates the camera by the given xyz-rotation.
	 * 
	 * @param dw	x-rotation
	 * @param dp	y-rotation
	 * @param dr	z-rotation
	 */
	public void rotate(float dw, float dp, float dr) {
		PVector delta = new PVector(dw, dp, dr);
		float deltaScale = RMath.DEG_TO_RAD / 4f;
		
		if (scale < 1f) {
			// Only reduce rotation scaling
			deltaScale *= scale;
		}
		
		// Apply rotation
		orientation.add( delta.mult(deltaScale) );
		
		// Apply camera rotation restrictions
		orientation.x = RMath.mod2PI(orientation.x);
		orientation.y = RMath.mod2PI(orientation.y);
		orientation.z = RMath.mod2PI(orientation.z);
	}
	
	/**
	 * Scales the camera by the given multiplier
	 * 
	 * @param multiplier	The multiplier to apply to the camer's current
	 * 						scale
	 */
	public void scale(float multiplier) {
		scale = Math.max(MIN_SCALE, Math.min(scale * multiplier, MAX_SCALE));
	}
	
	/**
	 * Returns the Camera's position, orientation, and scale
	 * in the form of a formatted String array, where each
	 * entry is one of the following values:
	 * 
	 * 0	The camera's x-position value
	 * 1	The camera's y-position value
	 * 2	The camera's z-position value
	 * 3	The camera's x-rotation value
	 * 4	The camera's y-rotation value
	 * 5	The camera's z-rotation value
	 * 6	The camera's scale value
	 * 
	 * @returning  A 7-element String array
	 */
	public String[] toStringArray() {
		String[] fields = new String[7];
		// Display rotation in degrees
		PVector inDegrees = PVector.mult(orientation, PConstants.RAD_TO_DEG);
		
		fields[0] = "X: " + MyFloatFormat.format(position.x);
		fields[1] = "Y: " + MyFloatFormat.format(position.y);
		fields[2] = "Z: " + MyFloatFormat.format(position.z);
		fields[3] = "W: " + MyFloatFormat.format(inDegrees.x);
		fields[4] = "P: " + MyFloatFormat.format(inDegrees.y);
		fields[5] = "R: " + MyFloatFormat.format(inDegrees.z);
		fields[6] = "S: " + MyFloatFormat.format(scale);
		
		return fields;
	}
	
	/**
	 * Translates the camera by the given xyz-translation.
	 * 
	 * @param dx	Change in x position
	 * @param dy	Change in y position
	 * @param dz	Change in z position
	 */
	public void translate(float dx, float dy, float dz) {
		// Apply translation
		PVector delta = new PVector(dx, dy, dz);
		position.add( delta.mult(scale) );
		
		// Apply camera position restrictions
		float limit = scale * 9999f;
		position.x = Math.max(-limit, Math.min(position.x, limit));
		position.y = Math.max(-limit, Math.min(position.y, limit));
	}
}