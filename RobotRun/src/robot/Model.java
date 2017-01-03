package robot;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import geom.Triangle;
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

	/**
	 * Use default scaling
	 */
	public Model(String filename, int col) {
		for(int n = 0; n < 3; n++) {
			rotations[n] = false;
			currentRotations[n] = 0;
			jointRanges[n] = null;
		}
		rotationSpeed = 0.01f;
		name = filename;
		loadSTLModel(filename, col, 1.0f);
	}

	/**
	 * Define the scaling of the Model.
	 */
	public Model(String filename, int col, float scaleVal) {
		for(int n = 0; n < 3; n++) {
			rotations[n] = false;
			currentRotations[n] = 0;
			jointRanges[n] = null;
		}
		rotationSpeed = 0.01f;
		name = filename;
		loadSTLModel(filename, col, scaleVal);
	}

	public boolean anglePermitted(int idx, float angle) {

		if(jointRanges[idx].x < jointRanges[idx].y) {
			// Joint range does not overlap TWO_PI
			return angle >= jointRanges[idx].x && angle < jointRanges[idx].y;
		} else {
			// Joint range overlaps TWO_PI
			return !(angle >= jointRanges[idx].y && angle < jointRanges[idx].x);
		}
	}

	public void draw() {
		RobotRun.getInstance().shape(mesh);
	}

	public void loadSTLModel(String filename, int col, float scaleVal) {
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		byte[] data = RobotRun.getInstance().loadBytes(filename);
		int n = 84; // skip header and number of triangles

		while(n < data.length) {
			Triangle t = new Triangle();
			for(int m = 0; m < 4; m++) {
				byte[] bytesX = new byte[4];
				bytesX[0] = data[n+3]; bytesX[1] = data[n+2];
				bytesX[2] = data[n+1]; bytesX[3] = data[n];
				n += 4;
				byte[] bytesY = new byte[4];
				bytesY[0] = data[n+3]; bytesY[1] = data[n+2];
				bytesY[2] = data[n+1]; bytesY[3] = data[n];
				n += 4;
				byte[] bytesZ = new byte[4];
				bytesZ[0] = data[n+3]; bytesZ[1] = data[n+2];
				bytesZ[2] = data[n+1]; bytesZ[3] = data[n];
				n += 4;
				t.components[m] = new PVector(
						ByteBuffer.wrap(bytesX).getFloat(),
						ByteBuffer.wrap(bytesY).getFloat(),
						ByteBuffer.wrap(bytesZ).getFloat()
						);
			}
			triangles.add(t);
			n += 2; // skip meaningless "attribute byte count"
		}
		mesh = RobotRun.getInstance().createShape();
		mesh.beginShape(RobotRun.TRIANGLES);
		mesh.noStroke();
		mesh.scale(scaleVal);
		mesh.fill(col);
		for(Triangle t : triangles) {
			mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
			mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
			mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
			mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
		}
		mesh.endShape();
	} // end loadSTLModel

} // end Model class