package programming;

import core.Scenario;
import geom.DimType;
import geom.Point;
import geom.RMatrix;
import geom.WorldObject;
import processing.core.PVector;

public class CamMoveToObject extends MotionInstruction {
	
	private Scenario scene;
	private WorldObject tgtObj;
	
	public CamMoveToObject(int type, int WOdx, float spd, int term,
			Scenario scene) {
		
		this(false, type, WOdx, spd, term, scene);
	}
	
	public CamMoveToObject(boolean isComm, int type, int WOdx, float spd,
			int term, Scenario scene) {
		
		super(isComm, type, spd, term);
		this.scene = scene;
		
		if (scene != null) {
			tgtObj = scene.getWorldObject(WOdx);
		}
	}
	
	public CamMoveToObject clone() {
		// TODO
		System.err.println("Not implemented in CamMoveToObject.clone()");
		return null;
	}
	
	public Scenario getScene() {
		return scene;
	}
	
	public WorldObject getTgtObject() {
		return tgtObj;
	}
	
	public Point getWOPosition() {
		WorldObject tgt = getTgtObject();
		RMatrix tgtOri = tgt.getLocalOrientation();
		PVector vecX = new PVector(tgtOri.getEntryF(0, 0), tgtOri.getEntryF(1, 0), tgtOri.getEntryF(2, 0));
		PVector offset = vecX.mult(tgt.getForm().getDimArray()[0] / 2f + 5);
					
		Point pt = new Point(PVector.add(tgt.getLocalCenter(), offset), tgt.getLocalOrientation());
		//float[] angles = RMath.inverseKinematics(this, getJointAngles(), pt.position, pt.orientation);
		//pt.angles = angles;
		
		return pt;
	}
	
	public void setWO(int WOdx) {
		if (scene != null) {
			tgtObj = scene.getWorldObject(WOdx);
		}
	}
	
	public String[] toStringArray() {
		// TODO
		return new String[] { "TODO" };
	}
}
