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
		
		PVector objCenter = o.getLocalCenter();
		
		if(o.getForm() instanceof Box) {
			float len = o.getForm().getDim(DimType.LENGTH);
			float wid = o.getForm().getDim(DimType.WIDTH);
			float hgt = o.getForm().getDim(DimType.HEIGHT);
			
			
		}
		
		return 0;
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
	private PVector[] getPlane(float fov, float aspectRatio, float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(fov >= 90 || fov <= 0) { return null; }
		float diagonal = 2*(float)Math.tan(fov/2)*dist;
		float height = (float)Math.sqrt(diagonal*diagonal / (1 + fov*fov));
		float width = height * fov;
		
		// Produce a coordinate system based on camera orientation
		float[][] coord = RobotRun.quatToMatrix(camOrient);
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
