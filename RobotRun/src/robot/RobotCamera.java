package robot;

import java.util.ArrayList;

import geom.Box;
import geom.DimType;
import geom.WorldObject;
import global.Fields;
import processing.core.PVector;

public class RobotCamera {
	private PVector camPos;
	private RQuaternion camOrient;
	private float camFOV; // Angle between the diagonals of the camera view frustum, in degrees
	private float camAspectRatio; // Ratio of horizontal : vertical camera frustum size 
	
	private float camClipNear; // The distance from the camera to the near clipping plane
	private float camClipFar; // The distance from the camera to the far clipping plane
	private Scenario scene;
	
	public RobotCamera(float posX, float posY, float posZ, RQuaternion q, 
			float fov, float ar, float near, float far, Scenario s) {
		camPos = new PVector(posX, posY, posZ);
		camOrient = q;
		camFOV = fov;
		camAspectRatio = ar;
		camClipNear = near;
		camClipFar = far;
		scene = s;
	}
	
	public static void main(String args[]) {
		RobotCamera c = new RobotCamera(200, 0, 200, new RQuaternion(), 90, 1, 10, 500, null);
		Box b = new Box(0, 5, 100);
		float[][] axes = new float[][] { {1, 0, 0},
									 	  {0, 1, 0},
									 	  {0, 0, 1} };
		CoordinateSystem coord = new CoordinateSystem(new PVector(250, 0, 200), axes);
		Fixture f = new Fixture("test", b, coord);
		
		System.out.println("is object in frame? " + c.checkObjectInFrame(f));
	}
	
	public RobotCamera setOrientation(RQuaternion o) {
		camOrient = o;
		return this;
	}
	
	public WorldObject getNearestObjectInFrame() {
		float minDist = Float.MAX_VALUE;
		WorldObject closeObj = null;
		for(WorldObject o : getObjectsInFrame()) {
			PVector objCenter = o.getLocalCenter();
			PVector toObj = new PVector(objCenter.x - camPos.x, objCenter.y - camPos.y, objCenter.z - camPos.z);
			
			float dist = toObj.mag();
			if(minDist > dist) {
				minDist = dist;
				closeObj = o;
			}
		}
		
		return closeObj;
	}
	
	public float[][] getOrientationMat() {
		return camOrient.toMatrix();
	}
	
	/**
	 * Performs frustum culling on all objects in scene to obtain a list of the objects
	 * that fall inside the view frustum.
	 * 
	 * @return The list of WorldObjects that fall inside of the camera view frustum.
	 */
	public ArrayList<WorldObject> getObjectsInFrame() {
		ArrayList<WorldObject> objList = new ArrayList<WorldObject>();
		
		for(WorldObject o : scene.getObjectList()) {
			if(checkObjectInFrame(o) >= 1) {
				objList.add(o);
			}
		}
		
		return objList;
	}
	
	/**
	 * Examines a given WorldObject to determine whether it falls fully or
	 * partially in the camera view frustum.
	 * 
	 * @param o The WorldObject to be tested.
	 * @return 0 if the object is not at all in frame, 1 if the object is partially in frame,
	 * 		   or 2 if the object is fully in frame.
	 */
	public int checkObjectInFrame(WorldObject o) {
		PVector objCenter = o.getLocalCenter();
		System.out.println("obj loc: " + objCenter.toString());
		if(checkPointInFrame(objCenter)) {
			return 1;
		}
		
		return 0;
	}
	
	public boolean checkPointInFrame(PVector p) {
		float coord[][] = camOrient.toMatrix();
		PVector lookVect = new PVector(coord[0][0], coord[0][1], coord[0][2]);
		PVector ltVect = new PVector(coord[1][0], coord[1][1], coord[1][2]);
		PVector upVect = new PVector(coord[2][0], coord[2][1], coord[2][2]);
		
		PVector toObj = new PVector(p.x - camPos.x, p.y - camPos.y, p.z - camPos.z);
		System.out.println("to obj: " + toObj.toString());
		System.out.println("look vec: " + lookVect.toString());
		float dist = toObj.dot(lookVect);
		System.out.println("dist: " + dist);
		if(dist > camClipFar || dist < camClipNear) { return false;	}
		
		float width = getPlaneWidth(camFOV, camAspectRatio, dist);
		float height = getPlaneHeight(camFOV, camAspectRatio, dist);
		
		float distW = toObj.dot(ltVect) + (width / 2);
		float distH = toObj.dot(upVect) + (height / 2);
		
		if(distW <= width && distW >= 0 && distH <= height && distH >= 0) {
			return true;
		}
		
		return false;
	}
	
	public float getPlaneHeight(float fov, float aspectRatio, float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(fov >= 180 || fov <= 0) { return -1; }
		float diagonal = 2*(float)Math.tan(fov/2)*dist;
		float height = (float)Math.sqrt(diagonal*diagonal / (1 + aspectRatio*aspectRatio));
		
		return height;
	}
	
	public float getPlaneWidth(float fov, float aspectRatio, float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(fov >= 180 || fov <= 0) { return -1; }
		float width = getPlaneHeight(fov, aspectRatio, dist) * aspectRatio;
		
		return width;
	}
	
	/**
	 * Returns a 4 element PVector array containing the locations of the 4 points that make up the
	 * plane specified by the camera field of view, camera aspect ratio, and the distance from the
	 * camera to the plane. Planes are always defined by their 4 corners and are always perpendicular
	 * to the camera.
	 * 
	 * @param fov Camera field of view
	 * @param aspectRatio Camera aspect ratio
	 * @param dist Distance from camera to center of plane
	 * @return A 4 element array containing the locations of the corners of the plane in the following
	 * 		   order: top left, top right bottom left, bottom right
	 */
	public PVector[] getPlane(float fov, float aspectRatio, float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(fov >= 180 || fov <= 0) { return null; }
		float height = getPlaneHeight(fov, aspectRatio, dist);
		float width = getPlaneWidth(fov, aspectRatio, dist);
		
		// Produce a coordinate system based on camera orientation
		float[][] coord = camOrient.toMatrix();
		PVector lookVect = new PVector(coord[0][0], coord[0][1], coord[0][2]);
		PVector ltVect = new PVector(coord[1][0], coord[1][1], coord[1][2]);
		PVector upVect = new PVector(coord[2][0], coord[2][1], coord[2][2]);
		
		PVector center = new PVector(camPos.x + lookVect.x * dist,
									 camPos.y + lookVect.y * dist,
									 camPos.z + lookVect.z * dist);
		
		PVector tl = new PVector(center.x + upVect.x * height / 2 + ltVect.x * width / 2,
								 center.y + upVect.y * height / 2 + ltVect.y * width / 2,
								 center.z + upVect.z * height / 2 + ltVect.z * width / 2);
		PVector tr = new PVector(center.x + upVect.x * height / 2 - ltVect.x * width / 2,
								 center.y + upVect.y * height / 2 - ltVect.y * width / 2,
								 center.z + upVect.z * height / 2 - ltVect.z * width / 2);
		PVector bl = new PVector(center.x - upVect.x * height / 2 + ltVect.x * width / 2,
				  				 center.y - upVect.y * height / 2 + ltVect.y * width / 2,
				  				 center.z - upVect.z * height / 2 + ltVect.z * width / 2);
		PVector br = new PVector(center.x - upVect.x * height / 2 - ltVect.x * width / 2,
								 center.y - upVect.y * height / 2 - ltVect.y * width / 2,
				  				 center.z - upVect.z * height / 2 - ltVect.z * width / 2);
		
		return new PVector[] {tl, tr, bl, br};
	}
}
