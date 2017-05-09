package robot;

import java.util.ArrayList;

import geom.Box;
import geom.Cylinder;
import geom.DimType;
import geom.Fixture;
import geom.ModelShape;
import geom.RMath;
import geom.RQuaternion;
import geom.WorldObject;
import processing.core.PShape;
import processing.core.PVector;

public class RobotCamera {
	private float camAspectRatio; // Ratio of horizontal : vertical camera frustum size 
	private float camClipFar; // The distance from the camera to the far clipping plane
	private float camClipNear; // The distance from the camera to the near clipping plane
	
	private float camFOV; // Horizontal view angle of the camera, in degrees
	private RQuaternion camOrient;
	private PVector camPos;
	
	private float sensitivity;
	private Scenario scene;
	
	public RobotCamera(float posX, float posY, float posZ, RQuaternion q, 
			float fov, float ar, float near, float far, Scenario sc) {
		camPos = new PVector(posX, posY, posZ);
		camOrient = q;
		camFOV = fov;
		camAspectRatio = ar;
		camClipNear = near;
		camClipFar = far;
		sensitivity = 0.75f;
		scene = sc;
	}
	
	/**
	 * Examines a given WorldObject to determine whether it falls fully or
	 * partially in the camera view frustum.
	 * 
	 * @param o The WorldObject to be tested.
	 * @return  
	 */
	public float checkObjectInFrame(WorldObject o) {
		PVector objCenter = o.getLocalCenter();
		float l, w, h;
			
		if(o.getForm() instanceof Box) {
			//TODO should probably make sure that camera axes agree with camera look direction
			l = ((Box)o.getForm()).getDim(DimType.LENGTH);
			w = ((Box)o.getForm()).getDim(DimType.WIDTH);
			h = ((Box)o.getForm()).getDim(DimType.HEIGHT);
			
		}
		else if (o.getForm() instanceof Cylinder) {
			l = ((Cylinder)o.getForm()).getDim(DimType.RADIUS);
			w = ((Cylinder)o.getForm()).getDim(DimType.RADIUS);
			h = ((Cylinder)o.getForm()).getDim(DimType.HEIGHT);
		}
		else if (o.getForm() instanceof ModelShape){
			l = ((ModelShape)o.getForm()).getDim(DimType.LENGTH);
			w = ((ModelShape)o.getForm()).getDim(DimType.WIDTH);
			h = ((ModelShape)o.getForm()).getDim(DimType.HEIGHT);
		}
		else {
			return -1;
		}
		
		float s = (float)(1 / Math.tan((camFOV/2) * RobotRun.DEG_TO_RAD));
		float[][] pMat = new float[][] { 
			{s, 0, 0, 0},
			{0, s, 0, 0},
			{0, 0, -camClipFar/(camClipFar - camClipNear), -1},
			{0, 0, -camClipFar*camClipNear/(camClipFar - camClipNear), 0}
		};
				
		//Generate camera axes
		PVector lookVect = getVectLook();
		PVector upVect = getVectUp();
		PVector ltVect = lookVect.cross(upVect);
		
		//Create vector to object center point, find x, y, z offset components
		PVector toObj = new PVector(objCenter.x - camPos.x, objCenter.y - camPos.y, objCenter.z - camPos.z);
		float distZ = toObj.dot(lookVect);
		float distX = Math.abs(toObj.dot(ltVect));
		float distY = Math.abs(toObj.dot(upVect));
		
		PVector[] objVertices = new PVector[8];
		objVertices[0] = new PVector(objCenter.x + l/2, objCenter.y + h/2, objCenter.z + w/2);
		objVertices[1] = new PVector(objCenter.x + l/2, objCenter.y + h/2, objCenter.z - w/2);
		objVertices[2] = new PVector(objCenter.x + l/2, objCenter.y - h/2, objCenter.z + w/2);
		objVertices[3] = new PVector(objCenter.x + l/2, objCenter.y - h/2, objCenter.z - w/2);
		objVertices[4] = new PVector(objCenter.x - l/2, objCenter.y + h/2, objCenter.z + w/2);
		objVertices[5] = new PVector(objCenter.x - l/2, objCenter.y + h/2, objCenter.z - w/2);
		objVertices[6] = new PVector(objCenter.x - l/2, objCenter.y - h/2, objCenter.z + w/2);
		objVertices[7] = new PVector(objCenter.x - l/2, objCenter.y - h/2, objCenter.z - w/2);
		
		for(int i = 0; i < 8; i += 1) {
			objVertices[i] = objVertices[i].sub(camPos);
			System.out.println("vertex " + i + ": " + objVertices[i].toString());
			objVertices[i] = RMath.transformVector(objVertices[i], pMat);
			System.out.println("tvertex " + i + ": " + objVertices[i].toString());
		}
		
		//Calculate the width and height of our frustum view plane
		float pWidth = getPlaneWidth(distX);
		float pHeight = getPlaneHeight(distX);
		
		//Generate object axes and produce the diagonal vector of the object
		float[][] objCoord = o.getLocalOrientationAxes();
		PVector objAxisX = new PVector(objCoord[0][0], objCoord[0][1], objCoord[0][2]);
		PVector objAxisY = new PVector(objCoord[1][0], objCoord[1][1], objCoord[1][2]);
		PVector objAxisZ = new PVector(objCoord[2][0], objCoord[2][1], objCoord[2][2]);
		//PVector c = objAxisX.mult(l).add(objAxisY.mult(w)).add(objAxisZ.mult(h));
		
		//Projected "apparent" dimensions of box on to camera axes 
		//float dimZ = l*objAxisX.dot(lookVect) + w*objAxisY.dot(lookVect) + h*objAxisZ.dot(lookVect);
		float dimX = l*objAxisX.dot(ltVect) + w*objAxisY.dot(ltVect) + h*objAxisZ.dot(ltVect);
		float dimY = l*objAxisX.dot(upVect) + w*objAxisY.dot(upVect) + h*objAxisZ.dot(upVect);
		
		//Calculate signed distance from center of object to near edge of view plane
		float aZ = Math.min(distX - camClipNear, camClipFar - distX); //???
		float aX = (pWidth/2 - distX);
		float aY = (pHeight/2 - distY);
		
		//Find the portion of the object projected dimensions that are in view
		float visX = Math.min(RMath.clamp(dimX/2 + aX, 0, dimX), pWidth);
		float visY = Math.min(RMath.clamp(dimY/2 + aY, 0, dimY), pHeight);
		
		System.out.println(String.format("x: %4f / %4f, y: %4f / %4f", visX, dimX, visY, dimY));
		System.out.println((visX/dimX + visY/dimY)/2);
		
		return (visX/dimX + visY/dimY)/2;
	}
	
	public boolean checkPointInFrame(PVector p) {
		PVector lookVect = getVectLook();
		PVector upVect = getVectUp();
		PVector ltVect = lookVect.cross(upVect);
		
		PVector toObj = new PVector(p.x - camPos.x, p.y - camPos.y, p.z - camPos.z);
		System.out.println("to obj: " + toObj.toString());
		System.out.println("look vec: " + lookVect.toString());
		float dist = toObj.dot(lookVect);
		System.out.println("dist: " + dist);
		if(dist > camClipFar || dist < camClipNear) { return false;	}
		
		float pWidth = getPlaneWidth(dist);
		float pHeight = getPlaneHeight(dist);
		
		float distW = toObj.dot(ltVect) + (pWidth / 2);
		float distH = toObj.dot(upVect) + (pHeight / 2);
		
		if(distW <= pWidth && distW >= 0 && distH <= pHeight && distH >= 0) {
			return true;
		}
		
		return false;
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
	
	
	/**
	 * Performs frustum culling on all objects in scene to obtain a list of the objects
	 * that fall inside the view frustum.
	 * 
	 * @return The list of WorldObjects that fall inside of the camera view frustum.
	 */
	public ArrayList<WorldObject> getObjectsInFrame() {
		ArrayList<WorldObject> objList = new ArrayList<>();
		
		for(WorldObject o : scene.getObjectList()) {
			if(checkPointInFrame(o.getLocalCenter()) && checkObjectInFrame(o) >= sensitivity) {
				objList.add(o);
			}
		}
		
		return objList;
	}
	
	public float[][] getOrientationMat() {
		return camOrient.toMatrix();
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
	public PVector[] getPlane(float dist) {
		// Field of view must be in the range of (0, 90) degrees
		float height = getPlaneHeight(dist);
		float width = getPlaneWidth(dist);
		
		// Produce a coordinate system based on camera orientation
		PVector lookVect = getVectLook();
		PVector upVect = getVectUp();
		PVector ltVect = lookVect.cross(upVect);
		
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
	
	public PVector[] getPlaneNear() {
		return getPlane(camClipNear);
	}
	
	public PVector[] getPlaneFar() {
		return getPlane(camClipFar);
	}
	
	public float getPlaneHeight(float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(camFOV >= 180 || camFOV <= 0) { return -1; }
		float height = getPlaneWidth(dist) / camAspectRatio;
		
		return height;
	}
	
	public float getPlaneWidth(float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(camFOV >= 180 || camFOV <= 0) { return -1; }
		float width = 2*(float)Math.tan((camFOV/2)*RobotRun.DEG_TO_RAD)*dist;
		
		return width;
	}
	
	public PVector getVectLook() {
		//Look down -z
		double x = -2*camOrient.x()*camOrient.z() + 2*camOrient.y()*camOrient.w();
		double y = -2*camOrient.y()*camOrient.z() - 2*camOrient.x()*camOrient.w();
		double z = -1 + 2*Math.pow(camOrient.x(), 2) + 2*Math.pow(camOrient.y(), 2);
		
		return new PVector((float)x, (float)y, (float)z);
	}
	
	public PVector getVectUp() {
		//Up vector is -y for left handed coordinate system
		double x = -2*camOrient.x()*camOrient.y() - 2*camOrient.z()*camOrient.w();
		double y = -1 + 2*Math.pow(camOrient.x(), 2) + 2*Math.pow(camOrient.z(), 2);
		double z = -2*camOrient.y()*camOrient.z() + 2*camOrient.x()*camOrient.w();
		
		return new PVector((float)x, (float)y, (float)z);
	}
	
	public RobotCamera setOrientation(RQuaternion o) {
		camOrient = o;
		return this;
	}
	
	public RobotCamera setPosition(PVector p) {
		camPos = p;
		return this;
	}
}
