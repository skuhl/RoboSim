package robot;

import java.util.ArrayList;
import geom.WorldObject;
import processing.core.PVector;

public class RobotCamera {
	private PVector camPos;
	private PVector directionVect;
	private float camRot;
	private float camFOV; // Angle between the diagonals of the camera view frustum, in degrees
	private float camAspectRatio; // Ratio of horizontal : vertical camera frustum size 
	
	private float camClipNear; // The distance from the camera to the near clipping plane
	private float camClipFar; // The distance from the camera to the far clipping plane
	private Scenario scene;
	
	public RobotCamera(float posX, float posY, float posZ, float rot, float dirX, float dirY, float dirZ, 
			float fov, float ar, float near, float far, Scenario s) {
		camPos = new PVector(posX, posY, posZ);
		directionVect = new PVector(dirX, dirY, dirZ).normalize();
		camRot = rot;
		camFOV = fov;
		camAspectRatio = ar;
		camClipNear = near;
		camClipFar = far;
		scene = s;
	}
	
	public RobotCamera(float posX, float posY, float posZ, RQuaternion q, 
			float fov, float ar, float near, float far, Scenario s) {
			
	}
	
	public WorldObject getNearestObjectInFrame() {
		for(WorldObject o : getObjectsInFrame()) {
			
		}
		
		return null;
	}
	
	/**
	 * Performs frustum culling on all objects in scene to obtain a list of the objects
	 * that fall inside the view frustum.
	 * 
	 * @return The list of WorldObjects that fall inside of the camera view frustum.
	 */
	public ArrayList<WorldObject> getObjectsInFrame() {
		for(WorldObject o : scene.getObjectList()) {
			
		}
		
		return null;
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
		PVector[] nearPlane = getPlane(camFOV, camAspectRatio, camClipNear);
		PVector[] farPlane = getPlane(camFOV, camAspectRatio, camClipFar);
		//TODO check for object/ frustum collision
		return 0;
	}
	
	private PVector[] getPlane(float fov, float aspectRatio, float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(fov >= 90 || fov <= 0) { return null; }
		float diagonal = 2*(float)Math.tan(fov/2)*dist;
		float height = (float)Math.sqrt(diagonal*diagonal / (1 + fov*fov));
		float width = height * fov;
		
		//TODO calculate up and left vectors for camera
		PVector upVect = new PVector();
		PVector ltVect = new PVector(); 
		
		PVector center = new PVector(camPos.x + directionVect.x * dist,
									 camPos.y + directionVect.y * dist,
									 camPos.z + directionVect.z * dist);
		PVector tl = new PVector(center.x + upVect.x * height / 2 + ltVect.x * width / 2,
								 center.y + upVect.y * height / 2 + ltVect.y * width / 2,
								 center.z + upVect.z * height / 2 + ltVect.z * width / 2);
		PVector tr = new PVector(center.x + upVect.x * height / 2 + ltVect.x * width / 2,
								 center.y + upVect.y * height / 2 + ltVect.y * width / 2,
								 center.z + upVect.z * height / 2 + ltVect.z * width / 2);
		PVector bl = new PVector(center.x + upVect.x * height / 2 + ltVect.x * width / 2,
				  				 center.y + upVect.y * height / 2 + ltVect.y * width / 2,
				  				 center.z + upVect.z * height / 2 + ltVect.z * width / 2);
		PVector br = new PVector(center.x + upVect.x * height / 2 + ltVect.x * width / 2,
								 center.y + upVect.y * height / 2 + ltVect.y * width / 2,
				  				 center.z + upVect.z * height / 2 + ltVect.z * width / 2);
		
		return new PVector[] {tl, tr, bl, br};
	}
}
