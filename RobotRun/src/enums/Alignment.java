package enums;

/**
 * Defines the relative positions of a controller in the UI used for position
 * UI elements on the screen with respect to other UI elements.
 * 
 * @author Joshua Hooker
 */
public enum Alignment {
	BOTTOM_CENTER(0.5f, 1f), BOTTOM_LEFT(0f, 1f), BOTTOM_RIGHT(1f, 1f),
	CENTER(0.5f, 0.5f), CENTER_LEFT(0f, 0.5f), CENTER_RIGHT(1f, 0.5f),
	TOP_CENTER(0.5f, 0), TOP_LEFT(0f, 0f), TOP_RIGHT(1f, 0f);
	
	public final float factorX, factorY;
	
	private Alignment(float fx, float fy) {
		factorX = fx;
		factorY = fy;
	}
}

