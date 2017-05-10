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
	public float checkObjectInFrame(WorldObject o, boolean func) {
		PVector objCenter = o.getLocalCenter();
		float len, wid, hgt;
			
		if(o.getForm() instanceof Box) {
			//TODO should probably make sure that camera axes agree with camera look direction
			len = ((Box)o.getForm()).getDim(DimType.LENGTH);
			wid = ((Box)o.getForm()).getDim(DimType.WIDTH);
			hgt = ((Box)o.getForm()).getDim(DimType.HEIGHT);
			
		}
		else if (o.getForm() instanceof Cylinder) {
			len = ((Cylinder)o.getForm()).getDim(DimType.RADIUS);
			wid = ((Cylinder)o.getForm()).getDim(DimType.RADIUS);
			hgt = ((Cylinder)o.getForm()).getDim(DimType.HEIGHT);
		}
		else if (o.getForm() instanceof ModelShape){
			len = ((ModelShape)o.getForm()).getDim(DimType.LENGTH);
			wid = ((ModelShape)o.getForm()).getDim(DimType.WIDTH);
			hgt = ((ModelShape)o.getForm()).getDim(DimType.HEIGHT);
		}
		else {
			return -1;
		}
		
		//Generate camera axes
		PVector lookVect = getVectLook();
		PVector upVect = getVectUp();
		PVector ltVect = lookVect.cross(upVect);
		
		//Create vector to object center point, find x, y, z offset components
		PVector toObj = new PVector(objCenter.x - camPos.x, objCenter.y - camPos.y, objCenter.z - camPos.z);
		float distZ = toObj.dot(lookVect);
		System.out.println("z dist: " + distZ);
		
		if(distZ < camClipNear) return 0;
		
		float distX = Math.abs(toObj.dot(ltVect));
		float distY = Math.abs(toObj.dot(upVect));
		
		System.out.println("x dist: " + distX);
		System.out.println("y dist: " + distY);
		
		/*float r = getPlaneWidth(camClipNear)/2;
		float l = -r;
		float t = getPlaneHeight(camClipNear)/2;
		float b = -t;
		float n = camClipNear;
		float f = camClipFar;
		
		float[][] vMat = getViewMat();
		float[][] pMat1 = new float[][] {
			{1, 0, 0, 0},
			{0, 1, 0, 0},
			{0, 0, -f/(f-n), -1},
			{0, 0, -f*n/(f-n), 0}
		};
		
		float[][] pMat2 = new float[][] { 
			{2*n/(r-l), 0, 0, 0},
			{0, 2*n/(t-b), 0, 0},
			{(r+l)/(r-l), (t+b)/(t-b), -(f+n)/(f-n), -1},
			{0, 0, -(2*f*n)/(f-n), 0}
		};
		
		PVector[] objVertices = new PVector[8];
		objVertices[0] = new PVector(objCenter.x + len/2, objCenter.y + hgt/2, objCenter.z + wid/2);
		objVertices[1] = new PVector(objCenter.x + len/2, objCenter.y + hgt/2, objCenter.z - wid/2);
		objVertices[2] = new PVector(objCenter.x + len/2, objCenter.y - hgt/2, objCenter.z + wid/2);
		objVertices[3] = new PVector(objCenter.x + len/2, objCenter.y - hgt/2, objCenter.z - wid/2);
		objVertices[4] = new PVector(objCenter.x - len/2, objCenter.y + hgt/2, objCenter.z + wid/2);
		objVertices[5] = new PVector(objCenter.x - len/2, objCenter.y + hgt/2, objCenter.z - wid/2);
		objVertices[6] = new PVector(objCenter.x - len/2, objCenter.y - hgt/2, objCenter.z + wid/2);
		objVertices[7] = new PVector(objCenter.x - len/2, objCenter.y - hgt/2, objCenter.z - wid/2);
		
		for(int i = 0; i < 8; i += 1) {
			//System.out.println("v" + i + ": " + objVertices[i].toString());
			objVertices[i] = RMath.vectorMatrixMult(objVertices[i], vMat);
			objVertices[i] = RMath.vectorMatrixMult(objVertices[i], pMat);
			//System.out.println("v" + i + ": " + objVertices[i].toString());
		}*/
		
		//Generate object axes and produce the diagonal vector of the object
		float[][] objCoord = o.getLocalOrientationAxes();
		//System.out.println("objCoord");
		//RMath.printMat(objCoord);
		PVector objAxisX = new PVector(objCoord[0][0], objCoord[0][1], objCoord[0][2]);
		PVector objAxisY = new PVector(objCoord[1][0], objCoord[1][1], objCoord[1][2]);
		PVector objAxisZ = new PVector(objCoord[2][0], objCoord[2][1], objCoord[2][2]);
		
		//Projected "apparent" dimensions of box on to camera axes 
		float dimZ = Math.abs(len*objAxisX.dot(lookVect) + hgt*objAxisY.dot(lookVect) + wid*objAxisZ.dot(lookVect));
		float dimX = Math.abs(len*objAxisX.dot(ltVect) + hgt*objAxisY.dot(ltVect) + wid*objAxisZ.dot(ltVect));
		float dimY = Math.abs(len*objAxisX.dot(upVect) + hgt*objAxisY.dot(upVect) + wid*objAxisZ.dot(upVect));
		
		if(func) return distZ - dimZ/2;
		
		//Calculate the width and height of our frustum view plane
		float pWidth = getPlaneWidth(distZ - dimZ/2);
		float pHeight = getPlaneHeight(distZ - dimZ/2);
		
		System.out.println(String.format("plane width: %4f, plane height: %4f", pWidth, pHeight));
		
		//Calculate signed distance from center of object to near edge of view plane
		float aZ = Math.min(distX - camClipNear, camClipFar - distX); //???
		float aX = (pWidth/2 - distX);
		float aY = (pHeight/2 - distY);
		
		//Find the portion of the object projected dimensions that are in view
		float visX = Math.min(RMath.clamp(dimX/2 + aX, 0, dimX), pWidth);
		float visY = Math.min(RMath.clamp(dimY/2 + aY, 0, dimY), pHeight);
		
		System.out.println(String.format("x: %4f / %4f, y: %4f / %4f, ax: %4f, ay: %4f", visX, dimX, visY, dimY, aX, aY));
		System.out.println("area est: " + (visX/dimX) * (visY/dimY));
		System.out.println();
		
		return (visX/dimX) * (visY/dimY);
		//return objVertices;
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
			if(checkPointInFrame(o.getLocalCenter())){// && checkObjectInFrame(o) >= sensitivity) {
				objList.add(o);
			}
		}
		
		return objList;
	}
	
	public float[][] getOrientationMat() {
		return camOrient.toMatrix();
	}
	
	public float[][] getViewMat() {
		return RMath.transformationMatrix(camPos, getOrientationMat());
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
		//RMath.printMat(o.toMatrix());
		camOrient = o;
		return this;
	}
	
	public RobotCamera setPosition(PVector p) {
		camPos = p;
		return this;
	}
}
