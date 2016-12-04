package ui;
import processing.core.PVector;
import robot.RobotRun;

/**
 * A class designed which contains the camera transformation values
 * and the methods to manipulate apply the Camera's transformation.
 */
public class Camera {
	private PVector position,
	// Rotations in X, Y, Z in radians
	orientation;
	private static final float MAX_SCALE = 8f;
	private float scale;

	/**
	 * Creates a camera with the default position, orientation and scale.
	 */
	public Camera() {
		position = new PVector(0f, 0f, -500f);
		orientation = new PVector(0f, 0f, 0f);
		scale = 2f;
	}

	/**
	 * Apply the camer's scale, position, and orientation to the current matrix.
	 */
	public void apply() {
		RobotRun.getInstance().beginCamera();
		// Apply camera translations
		RobotRun.getInstance().translate(position.x + RobotRun.getInstance().width / 2f, position.y + RobotRun.getInstance().height / 2f, position.z);

		// Apply camera rotations
		RobotRun.getInstance().rotateX(orientation.x);
		RobotRun.getInstance().rotateY(orientation.y);

		// Apply camera scaling
		float horizontalMargin = scale * RobotRun.getInstance().width / 2f,
				verticalMargin = scale * RobotRun.getInstance().height / 2f,
				near = MAX_SCALE / scale,
				far = scale * 5000f;
		RobotRun.getInstance().ortho(-horizontalMargin, horizontalMargin, -verticalMargin, verticalMargin, near, far);

		RobotRun.getInstance().endCamera();
	}

	/**
	 * Return the camera perspective to the
	 * default position, orientation and scale.
	 */
	public void reset() {
		position.x = 0;
		position.y = 0;
		position.z = -500f;
		orientation.x = 0f;
		orientation.y = 0f;
		orientation.z = 0f;
		scale = 2f;
	}

	/**
	 * Change the camera's position by the given values.
	 */
	public void move(float x, float y, float z) {
		float horzontialLimit = MAX_SCALE * RobotRun.getInstance().width / 3f,
				verticalLimit = MAX_SCALE * RobotRun.getInstance().height / 3f;

		position.add( new PVector(x, y, z) );
		// Apply camera position restrictions
		position.x = RobotRun.max(-horzontialLimit, RobotRun.min(position.x, horzontialLimit));
		position.y = RobotRun.max(-verticalLimit, RobotRun.min(position.y, verticalLimit));
		position.z = RobotRun.max(-1000f, RobotRun.min(position.z, 1000f));
	}

	/**
	 * Change the camera's rotation by the given values.
	 */
	public void rotate(float w, float p, float r) {
		PVector rotation = new PVector(w, p, r);

		orientation.add( rotation );
		// Apply caerma rotation restrictions
		orientation.x = RobotRun.mod2PI(orientation.x);
		orientation.y = RobotRun.mod2PI(orientation.y);
		orientation.z = 0f;//mod2PI(orientation.z);
	}

	/**
	 * Change the scaling of the camera.
	 */
	public void changeScale(float multiplier) {
		scale = RobotRun.max(0.25f, RobotRun.min(scale * multiplier, MAX_SCALE));
	}

	/**
	 * Returns the Camera's position, orientation, and scale
	 * in the form of a formatted String array, where each
	 * entry is one of the following values:
	 * 
	 * Title String
	 * X - The camera's x -position value
	 * Y - The camera's y-position value
	 * Z - The camera's z-position value
	 * W - The camera's x-rotation value
	 * P - The camera's y-rotation value
	 * R - The camera's z-rotation value
	 * S - The camera's scale value
	 * 
	 * @returning  A 6-element String array
	 */
	public String[] toStringArray() {
		String[] fields = new String[8];
		// Display rotation in degrees
		PVector inDegrees = PVector.mult(orientation, RobotRun.RAD_TO_DEG);

		fields[0] = "Camera Fields";
		fields[1] = String.format("X: %6.9f", position.x);
		fields[2] = String.format("Y: %6.9f", position.y);
		fields[3] = String.format("Z: %6.9f", position.z);
		fields[4] = String.format("W: %6.9f", inDegrees.x);
		fields[5] = String.format("P: %6.9f", inDegrees.y);
		fields[6] = String.format("R: %6.9f", inDegrees.z);
		fields[7] = String.format("S: %3.9f", scale);

		return fields;
	}

	/**
	 * Returns an independent replica of the Camera object.
	 */
	public Camera clone() {
		Camera copy = new Camera();
		// Copy position, orientation, and scale
		copy.position = position.copy();
		copy.orientation = orientation.copy();
		copy.scale = scale;

		return copy;
	}

	// Getters for the Camera's position, orientation, and scale
	public PVector getPosition() { return position; }
	public PVector getOrientation() { return orientation; }
	public float getScale() { return scale; }
}