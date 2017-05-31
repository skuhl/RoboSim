package robot;

import processing.core.PVector;

/**
 * Defines a translation to apply to a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GTranslate implements DrawAction {
	
	public final PVector TRANSLATION;
	
	public GTranslate(float tx, float ty, float tz) {
		TRANSLATION = new PVector(tx, ty, tz);
	}
	
	public GTranslate(PVector t) {
		TRANSLATION = t;
	}
	
}
