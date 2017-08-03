package ui;

import global.DebugFloatFormat;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * A class designed which contains the camera transformation values
 * and the methods to manipulate apply the Camera's transformation.
 */
public class Camera {
	private final float DEF_ZOFFSET, MIN_ZOFFSET, MAX_ZOFFSET;
	
	private PVector position;
	// Rotations in X, Y, Z in radians
	private PVector rotation;
	private float zOffset;
	
	/**
	 * Creates a camera with the default position, orientation and scale.
	 */
	public Camera(float defZOffset, float minZOffset, float maxZOffset) {
		DEF_ZOFFSET = defZOffset;
		MIN_ZOFFSET = minZOffset;
		MAX_ZOFFSET = maxZOffset;
		
		position = new PVector(0f, 0f, 0f);
		rotation = new PVector(0f, 0f, 0f);
		zOffset = 2f * DEF_ZOFFSET;
	}

	/**
	 * Returns an independent replica of the Camera object.
	 */
	@Override
	public Camera clone() {
		Camera copy = new Camera(DEF_ZOFFSET, MIN_ZOFFSET, MAX_ZOFFSET);
		// Copy position, orientation, and scale
		copy.position = position.copy();
		copy.rotation = rotation.copy();
		copy.zOffset = zOffset;

		return copy;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public float getDefZOffset() {
		return DEF_ZOFFSET;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public PVector getOrientation() {
		return rotation.copy();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public PVector getBasePosition() {
		return position.copy();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public float getMinZOffset() {
		return MIN_ZOFFSET;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public float getMaxZOffset() {
		return MAX_ZOFFSET;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public float getScale() {
		return zOffset / DEF_ZOFFSET;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public float getZOffset() {
		return zOffset;
	}

	/**
	 * Return the camera perspective to the
	 * default position, orientation and scale.
	 */
	public void reset() {
		position.x = 0f;
		position.y = 0f;
		position.z = 0f;
		rotation.x = 0f;
		rotation.y = 0f;
		rotation.z = 0f;
		zOffset = 2f * DEF_ZOFFSET;
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
		
		// Only scale rotations down
		if (zOffset < DEF_ZOFFSET) {
			delta.mult(getScale());
		}
		
		// Apply rotation
		rotation.add( delta.mult(deltaScale) );
		
		// Apply camera rotation restrictions
		rotation.x = RMath.mod2PI(rotation.x);
		rotation.y = RMath.mod2PI(rotation.y);
		rotation.z = RMath.mod2PI(rotation.z);
	}
	
	/**
	 * Set the position of the camera.
	 * 
	 * @param x	The x-axis position
	 * @param y	The y-axis position
	 * @param z	The z-axis position
	 */
	public void setPosition(float x, float y, float z) {
		final float limit = 9999f;
		
		position.x = RMath.clamp(x, -limit, limit);
		position.y = RMath.clamp(y, -limit, limit);
		position.z = RMath.clamp(z, -limit, limit);
	}
	
	/**
	 * Set the rotation of the camera.
	 * 
	 * @param w	The x-axis rotation
	 * @param p	The y-axis rotation
	 * @param r	The z-axis rotation
	 */
	public void setRotation(float w, float p, float r) {		
		rotation.x = RMath.mod2PI(w);
		rotation.y = RMath.mod2PI(p);
		rotation.z = RMath.mod2PI(r);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newOffset
	 */
	public void setZOffset(float newOffset) {
		zOffset = RMath.clamp(newOffset, MIN_ZOFFSET, MAX_ZOFFSET);
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
		PVector worldPos = RMath.vToWorld(position);
		PVector inDegrees = PVector.mult(rotation, PConstants.RAD_TO_DEG);
		
		fields[0] = "X: " + DebugFloatFormat.format(worldPos.x);
		fields[1] = "Y: " + DebugFloatFormat.format(worldPos.y);
		fields[2] = "Z: " + DebugFloatFormat.format(worldPos.z);
		fields[3] = "W: " + DebugFloatFormat.format(inDegrees.x);
		fields[4] = "P: " + DebugFloatFormat.format(inDegrees.y);
		fields[5] = "R: " + DebugFloatFormat.format(inDegrees.z);
		fields[6] = "O: " + DebugFloatFormat.format(zOffset);
		
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
		PVector delta = new PVector(dx, dy, dz);
		float limit = 9999f;
		delta.mult(getScale());
		
		// Apply translation with position restrictions
		position.x = RMath.clamp(position.x + delta.x, -limit, limit);
		position.y = RMath.clamp(position.y + delta.y, -limit, limit);
		position.z = RMath.clamp(position.z + delta.z, -limit, limit);
	}
}