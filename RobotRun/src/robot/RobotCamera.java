package robot;

import java.util.ArrayList;

import geom.ComplexShape;
import geom.RMatrix;
import geom.RQuaternion;
import geom.WorldObject;
import global.RMath;
import processing.core.PVector;

public class RobotCamera {
	private final int RES = 5;
	private float camAspectRatio; // Ratio of horizontal : vertical camera frustum size 
	private float camClipFar; // The distance from the camera to the far clipping plane
	private float camClipNear; // The distance from the camera to the near clipping plane
	
	private float camFOV; // Horizontal view angle of the camera, in degrees
	private RQuaternion camOrient;
	private PVector camPos;
	
	private float sensitivity;
	private float brightness;
	private float exposure;
	private ArrayList<WorldObject> taughtObjects;
	
	public RobotCamera(float posX, float posY, float posZ, RQuaternion q, 
			float fov, float ar, float near, float far) {
		camPos = new PVector(posX, posY, posZ);
		camOrient = q;
		camFOV = fov;
		camAspectRatio = ar;
		camClipNear = near;
		camClipFar = far;
		sensitivity = 0.75f;
		brightness = 10.0f;
		exposure = 0.1f;
		taughtObjects = new ArrayList<WorldObject>();
	}
	
	public RobotCamera() {
		camPos = new PVector(-500, 0, 500);
		camOrient = new RQuaternion();
		camFOV = 75;
		camAspectRatio = 1.5f;
		camClipNear = 0.5f;
		camClipFar = 1000;
		sensitivity = 0.75f;
		brightness = 10.0f;
		exposure = 0.1f;
		taughtObjects = new ArrayList<WorldObject>();
		
	}
	
	public RobotCamera update(PVector pos, PVector rot,	float fov, float ar, 
			float near, float far, float br, float exp) {
		camPos = RMath.vFromWorld(pos);
		camOrient = RMath.wEulerToNQuat(rot);
		camFOV = fov;
		camAspectRatio = ar;
		camClipNear = near;
		camClipFar = far;
		brightness = br;
		exposure = exp;
		return this;
	}
	
	public void camLookAt(PVector p, PVector up) {
		PVector toObj = camPos.sub(p).normalize();
		PVector lt = up.cross(toObj);
		PVector orthoUp = toObj.cross(lt);
		
		RMatrix coord = new RMatrix(new float[][] {
			{lt.x, orthoUp.x, toObj.x},
			{lt.y, orthoUp.y, toObj.y},
			{lt.z, orthoUp.z, toObj.z}
		});
		
		camOrient = RMath.matrixToQuat(coord);
	}
	
	/**
	 * Examines a given WorldObject to determine whether it falls fully or
	 * partially in the camera view frustum.
	 * 
	 * @param o The WorldObject to be tested
	 * @return A float between 0 and 1 representing how far in frame the object is
	 */
	public float checkObjectInFrame(WorldObject o) {
		PVector objCenter = o.getLocalCenter();
		float[] dims = o.getForm().getDimArray();
		float len = dims[0];
		float hgt = dims[1];
		float wid = dims[2];
		
		PVector s = new PVector(objCenter.x - len/2, objCenter.y - hgt/2, objCenter.z - wid/2);
		int inView = 0;
		for(int i = 0; i < RES; i += 1) {
			for(int j = 0; j < RES; j += 1) {
				for(int k = 0; k < RES; k += 1) {
					PVector test = new PVector(s.x + i*(len/(RES-1)), s.y + j*(hgt/(RES-1)), s.z + k*(wid/(RES-1)));
					if(checkPointInFrame(test)) {
						inView += 1;
					}
				}
			}
		}
		
		return (inView / (float)(RES*RES*RES)) * brightness * exposure;
	}
	
	public boolean checkPointInFrame(PVector p) {
		RMatrix vMat = getViewMat();
		RMatrix pMat = getPerspProjMat();
		
		PVector tp = pMat.multiply(vMat.multiply(p));
		
		if(Math.abs(tp.x) < 1 && Math.abs(tp.y) < 1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Deprecated
	public float[] getColinearDimensions(WorldObject o) {
		float[] dims = o.getForm().getDimArray();
		float len = dims[0];
		float hgt = dims[1];
		float wid = dims[2];
		
		//Generate camera axes
		PVector lookVect = getVectLook();
		PVector upVect = getVectUp();
		PVector ltVect = lookVect.cross(upVect);
		
		//Generate object axes and produce the diagonal vector of the object
		float[][] objCoord = o.getLocalOrientation().getDataF();
		PVector objAxisX = new PVector(objCoord[0][0], objCoord[1][0], objCoord[2][0]);
		PVector objAxisY = new PVector(objCoord[0][1], objCoord[1][1], objCoord[2][1]);
		PVector objAxisZ = new PVector(objCoord[0][2], objCoord[1][2], objCoord[2][2]);
		
		//Projected "apparent" dimensions of box on to camera axes 
		float dimX = Math.abs(len*objAxisX.dot(ltVect)) + Math.abs(hgt*objAxisY.dot(ltVect)) + Math.abs(wid*objAxisZ.dot(ltVect));
		float dimY = Math.abs(len*objAxisX.dot(upVect)) + Math.abs(hgt*objAxisY.dot(upVect)) + Math.abs(wid*objAxisZ.dot(upVect));
		float dimZ = Math.abs(len*objAxisX.dot(lookVect)) + Math.abs(hgt*objAxisY.dot(lookVect)) + Math.abs(wid*objAxisZ.dot(lookVect));
		
		//Create vector to object center point, find x, y, z offset components
		PVector objCenter = o.getLocalCenter();
		PVector toObj = new PVector(objCenter.x - camPos.x, objCenter.y - camPos.y, objCenter.z - camPos.z);
		float distX = Math.abs(toObj.dot(ltVect));
		float distY = Math.abs(toObj.dot(upVect));
		float distZ = toObj.dot(lookVect);
		
		//Calculate the width and height of our frustum view plane
		float pWidth = getPlaneWidth(distZ);
		float pHeight = getPlaneHeight(distZ);
		
		System.out.println(String.format("plane width: %4f, plane height: %4f", pWidth, pHeight));
		
		//Calculate signed distance from center of object to near edge of view plane
		float aX = (pWidth/2 - distX);
		float aY = (pHeight/2 - distY);
		float aZ = Math.min(distZ - camClipNear, camClipFar - distZ);
		
		//Find the portion of the object projected dimensions that are in view
		float visX = Math.min(RMath.clamp(dimX/2 + aX, 0, dimX), pWidth);
		float visY = Math.min(RMath.clamp(dimY/2 + aY, 0, dimY), pHeight);
		float visZ = Math.min(RMath.clamp(dimZ/2 + aZ, 0, dimZ), camClipFar - camClipNear);
		
		return new float[] {visX, visY, visZ};
	}
	
	public WorldObject getNearestObjectInFrame(Scenario scene) {
		float minDist = Float.MAX_VALUE;
		WorldObject closeObj = null;
		for(WorldObject o : getObjectsInFrame(scene)) {
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
	public ArrayList<WorldObject> getObjectsInFrame(Scenario scene) {
		ArrayList<WorldObject> objList = new ArrayList<>();
		if(scene == null) return objList;
		
		for(WorldObject o : scene.getObjectList()) {
			if(checkPointInFrame(o.getLocalCenter()) && checkObjectInFrame(o) >= sensitivity) {
				objList.add(o);
			}
		}
		
		return objList;
	}
	
	public RQuaternion getOrientation() { 
		return camOrient; 
	}
		
	public RMatrix getOrientationMat() {
		return new RMatrix(camOrient.toMatrix());
	}
	
	public RMatrix getPerspProjMat() {
		float r = getPlaneWidth(camClipNear)/2;
		float l = -r;
		float t = getPlaneHeight(camClipNear)/2;
		float b = -t;
		float n = camClipNear;
		float f = camClipFar;
		
		RMatrix pMat = new RMatrix(new float[][] {
			{2*n/(r-l), 0, (r+l)/(r-l), 0},
			{0, 2*n/(t-b), (t+b)/(t-b), 0},
			{0, 0, -(f+n)/(f-n), -(2*f*n)/(f-n)},
			{0, 0, -1, 0}
		});
		
		return pMat;
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
	
	public PVector[] getPlaneFar() {
		return getPlane(camClipFar);
	}
	
	public float getPlaneHeight(float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(camFOV >= 180 || camFOV <= 0) { return -1; }
		float height = getPlaneWidth(dist) / camAspectRatio;
		
		return height;
	}
	
	public PVector[] getPlaneNear() {
		return getPlane(camClipNear);
	}
	
	public float getPlaneWidth(float dist) {
		// Field of view must be in the range of (0, 90) degrees
		if(camFOV >= 180 || camFOV <= 0) { return -1; }
		float width = 2*(float)Math.tan((camFOV/2)*RobotRun.DEG_TO_RAD)*dist;
		
		return width;
	}
	
	public PVector getPosition() { 
		return camPos; 
	}
	
	public PVector getVectLook() {
		//Look down -z
		double x = -2*camOrient.x()*camOrient.z() + 2*camOrient.y()*camOrient.w();
		double y = -2*camOrient.y()*camOrient.z() - 2*camOrient.x()*camOrient.w();
		double z = -1 + 2*Math.pow(camOrient.x(), 2) + 2*Math.pow(camOrient.y(), 2);
		
		return new PVector((float)x, (float)y, (float)z);
	}
	
	public PVector getVectUp() {
		//Up vector is aligned to local y axis
		double x = 2*camOrient.x()*camOrient.y() + 2*camOrient.z()*camOrient.w();
		double y = 1 - 2*Math.pow(camOrient.x(), 2) - 2*Math.pow(camOrient.z(), 2);
		double z = 2*camOrient.y()*camOrient.z() - 2*camOrient.x()*camOrient.w();
		
		return new PVector((float)x, (float)y, (float)z);
	}
	
	public RMatrix getViewMat() {
		float[][] rot = getOrientationMat().getDataF();
		
		float tPosX = -camPos.x*rot[0][0] - camPos.y*rot[1][0] - camPos.z*rot[2][0];
		float tPosY = -camPos.x*rot[0][1] - camPos.y*rot[1][1] - camPos.z*rot[2][1];
		float tPosZ =  camPos.x*rot[0][2] + camPos.y*rot[1][2] + camPos.z*rot[2][2];
		
		float[][] vMat = new float[][] {
			{ rot[0][0],  rot[1][0],  rot[2][0], tPosX},
			{ rot[0][1],  rot[1][1],  rot[2][1], tPosY},
			{-rot[0][2], -rot[1][2], -rot[2][2], tPosZ},
			{0, 0, 0, 1}
		};
				
		return new RMatrix(vMat);
	}
	
	public ArrayList<WorldObject> matchTaughtObject(int idx, Scenario scene) {
		WorldObject objProto;
		RMatrix objProtoOrient;
		
		if(idx < taughtObjects.size()) {
			objProto = taughtObjects.get(idx);
			objProtoOrient = objProto.getLocalOrientation();
		} else {
			return null;
		}
		
		ArrayList<WorldObject> inFrame = getObjectsInFrame(scene);
		ArrayList<WorldObject> objMatches = new ArrayList<WorldObject>();
		
		for(WorldObject o: inFrame) {
			if(o.getModelFamilyID() == objProto.getModelFamilyID()) {
				RMatrix objOrient = o.getLocalOrientation();
				RMatrix viewOrient = objOrient.transpose().multiply(camOrient.toMatrix());
				RMatrix oDiff = objProtoOrient.transpose().multiply(viewOrient);
				float[][] axes = oDiff.getDataF();
				PVector zDiff = new PVector(axes[0][2], axes[1][2], axes[2][2]);
				
				if(Math.pow(zDiff.dot(new PVector(0, 0, 1)), 2) > 0.9) {
					if(o.getModelFamilyID() == -1) {
						objMatches.add(o);
					}
					else {
						ComplexShape protoMdl = (ComplexShape)objProto.getForm();
						boolean objMatch = true;
						
						//TODO handle mismatches due to emphasized/ unselected defects (reflections, etc.)
						for(int i = 0; i < protoMdl.getNumSelectAreas(); i += 1) {
							CamSelectArea protoArea = protoMdl.getCamSelectArea(i);
							if(protoArea.getView(objProtoOrient) != null && !protoArea.isIgnored()) {
								if(protoArea.isEmphasized()) {
									if(o.getModelID() != objProto.getModelID() || protoArea.isDefect) {
										objMatch = false;
										break;
									}
								}
								else if(Math.random() < 0.5) {
									objMatch = false;
									break;
								}
							}
						}
						
						if(objMatch) {
							objMatches.add(o);
						}
					}
				}
			}
		}
		
		return objMatches;
	}
	
	public RobotCamera setOrientation(PVector o) {
		camOrient = RMath.eulerToQuat(o);
		return this;
	}
	
	public RobotCamera setOrientation(RQuaternion o) {
		camOrient = o;
		return this;
	}
	
	public RobotCamera setPosition(PVector p) {
		camPos = p;
		return this;
	}
	
	public ArrayList<WorldObject> teachObjectToCamera(Scenario scene) {
		ArrayList<WorldObject> objs = getObjectsInFrame(scene);
		if(objs.size() > 1 || objs.size() <= 0) {
			return null;
		}
		else {
			WorldObject teachObj = objs.get(0).clone();
			RMatrix objOrient = teachObj.getLocalOrientation();
			RMatrix viewOrient = objOrient.transpose().multiply(camOrient.toMatrix());
			teachObj.setLocalOrientation(viewOrient);
			
			for(int i = 0; i < taughtObjects.size(); i += 1) {
				WorldObject o = taughtObjects.get(i);
				if(o.getName().compareTo(teachObj.getName()) == 0) {
					taughtObjects.set(i, teachObj);
					return taughtObjects;
				}
			}
			
			taughtObjects.add(teachObj);
			return taughtObjects;
		}
	}

	public float getBrightness() {
		return brightness;
	}

	public float getExposure() {
		return exposure;
	}

	public float getNearClipDist() {
		return camClipNear;
	}
	
	public float getFarClipDist() {
		return camClipFar;
	}

	public float getFOV() {
		return camFOV;
	}
	
	public float getAspectRatio() {
		return camAspectRatio;
	}

	public ArrayList<WorldObject> getTaughtObjects() {
		return taughtObjects;
	}
}
