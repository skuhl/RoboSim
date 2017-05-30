package robot;

/**
 * Defines a rotation around the z-axis to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GRotateZ implements DrawAction {
	
	public final float rotation;
	
	public GRotateZ(float rotZ) {
		rotation = rotZ;
	}
}
