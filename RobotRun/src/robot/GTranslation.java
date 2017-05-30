package robot;

/**
 * Defines a translation to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GTranslation implements DrawAction {
	
	public final float transX, transY, transZ;
	
	public GTranslation(float tx, float ty, float tz) {
		transX = tx;
		transY = ty;
		transZ = tz;
	}
	
}
