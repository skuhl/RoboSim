package programming;

import geom.Point;
import geom.RMatrix;
import geom.Scenario;
import geom.WorldObject;
import global.Fields;
import processing.core.PVector;
import robot.RoboticArm;


/**
 * A motion instruction designed to move the robot to a specific position with
 * respect to the world object associated with this motion instruction.
 * 
 * @author Vincent Druckte
 */
public class CamMoveToObject extends MotionInstruction {
	
	/**
	 * The name saved in the program save file, which is used to initialize a
	 * camera motion instruction loaded from a save file.
	 */
	private final String loadedSceneName;
	private Scenario scene;
	
	private WorldObject tgtObj;
	
	public CamMoveToObject(boolean isComm, int type, int WOdx, float spd,
			int term, Scenario scene) {
		
		super(isComm, type, Fields.PTYPE_WO, WOdx, spd, term);
		this.scene = scene;
		
		if (scene != null) {
			this.tgtObj = scene.getWorldObject(WOdx);
			
		} else {
			this.tgtObj = null;
		}
		
		if (scene != null) {
			loadedSceneName = scene.getName();
			
		} else {
			loadedSceneName = null;
		}
	}
	
	/**
	 * Used for initializing a loaded camera motion instruction
	 * 
	 * @param isComm
	 * @param mType
	 * @param pType
	 * @param WOdx
	 * @param spd
	 * @param term
	 * @param sceneName
	 */
	public CamMoveToObject(boolean isComm, int mType, int pType, int WOdx,
			float spd, int term, String sceneName) {
		
		super(isComm, mType, Fields.PTYPE_WO, WOdx, spd, term);
		
		tgtObj = null;
		scene = null;
		loadedSceneName = sceneName;
	}
	
	public CamMoveToObject(int type, int WOdx, float spd, int term,
			Scenario scene) {
		
		this(false, type, WOdx, spd, term, scene);
	}
	
	/**
	 * Used for cloning a camera motion instruction
	 * 
	 * @param isComm
	 * @param mType
	 * @param pType
	 * @param pdx
	 * @param spd
	 * @param term
	 * @param tgtWO
	 * @param scene
	 */
	private CamMoveToObject(boolean isComm, int mType, int pType, int pdx,
			float spd, int term, WorldObject tgtWO, Scenario scene) {
		
		super(isComm, mType, pType, pdx, spd, term);
		this.scene = scene;
		this.tgtObj = tgtWO;
		
		if (scene != null) {
			loadedSceneName = scene.getName();
			
		} else {
			loadedSceneName = null;
		}
	}
	
	public CamMoveToObject clone() {
		return new CamMoveToObject(isCommented(), motionType, posType, posIdx,
				spdModifier, termination, tgtObj, scene);
	}
	
	public String getLoadedSceneName() {
		return loadedSceneName;
	}
	
	public Scenario getScene() {
		return scene;
	}
	
	public WorldObject getTgtObject() {
		return tgtObj;
	}
	
	public Point getWOPosition() {
		if (tgtObj != null) {
			WorldObject tgt = getTgtObject();
			RMatrix tgtOri = tgt.getLocalOrientation();
			PVector vecX = new PVector(tgtOri.getEntryF(0, 0), tgtOri.getEntryF(1, 0), tgtOri.getEntryF(2, 0));
			PVector offset = vecX.mult(tgt.getModel().getDims().x / 2f + 5);
						
			Point pt = new Point(PVector.add(tgt.getLocalCenter(), offset), tgt.getLocalOrientation());
			//float[] angles = RMath.inverseKinematics(this, getJointAngles(), pt.position, pt.orientation);
			//pt.angles = angles;
			
			return pt;
		}
		
		return null;
	}
	
	public void setPosIdx(int WOdx) {
		posIdx = WOdx;
		
		if (scene != null) {
			tgtObj = scene.getWorldObject(WOdx);
		}
	}
	
	/**
	 * Updates this motion instructions scene reference as well as its object
	 * reference.
	 * 
	 * @param s	The new scene reference for this instruction
	 */
	public void setScene(Scenario s) {
		scene = s;
		
		if (s != null) {
			// Update target object reference
			tgtObj = s.getWorldObject(posIdx);
		}
	}
	
	@Override
	public String[] toStringArray() {
		int fieldLen = 5;
		
		String[] fields = new String[fieldLen];
		
		// Motion type symbol
		int idx = 0;
		fields[idx] = "\0";
		
		// Position type
		++idx;
		if (posType == Fields.PTYPE_WO) {
			fields[idx] = "WO[";
			
		} else {
			fields[idx] = "  [";
		}
		
		// Position index
		++idx;
		if (tgtObj == null) {
			fields[idx] = "...]";
			
		} else {
			fields[idx] = String.format("%s]", tgtObj.getName());
		}
		
		// Motion speed modifier
		++idx;
		fields[idx] = String.format("%dmm/s",
				Math.round(RoboticArm.motorSpeed * spdModifier));
		
		// Termination percent
		++idx;
		if (termination == 0) {
			fields[idx] = "FINE";
			
		} else {
			fields[idx] = String.format("CONT%d", termination);
		}
		
		return fields;
	}
}
