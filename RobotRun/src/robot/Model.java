package robot;

import processing.core.PShape;
import processing.core.PVector;

public class Model {
	public PShape mesh;
	public String name;
	public boolean[] rotations = new boolean[3]; // is rotating on this joint valid?
	// Joint ranges follow a clockwise format running from the PVector.x to PVector.y, where PVector.x and PVector.y range from [0, TWO_PI]
	public PVector[] jointRanges = new PVector[3];
	public float[] currentRotations = new float[3]; // current rotation value
	public float[] targetRotations = new float[3]; // we want to be rotated to this value
	public int[] rotationDirections = new int[3]; // use shortest direction rotation
	public float rotationSpeed;
	public float[] jointsMoving = new float[3]; // for live control using the joint buttons

	public Model(String n, PShape mShape) {
		mesh = mShape;
		name = n;
		rotationSpeed = 0.01f;
		
		for(int i = 0; i < 3; i++) {
			rotations[i] = false;
			currentRotations[i] = 0;
			jointRanges[i] = null;
		}
	}

	public boolean anglePermitted(int idx, float angle) {
		return RobotRun.angleWithinBounds(angle, jointRanges[idx].x, jointRanges[idx].y);
	}

	public void draw() {
		RobotRun.getInstance().shape(mesh);
	}

} // end Model class