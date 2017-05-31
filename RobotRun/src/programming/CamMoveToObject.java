package programming;

import geom.DimType;
import geom.Point;
import geom.RMatrix;
import geom.WorldObject;
import processing.core.PVector;

public class CamMoveToObject extends MotionInstruction {
	WorldObject tgtObj;	
	
	public CamMoveToObject(int type, int pos, boolean globl, float spd, int term) {
		super(type, pos, globl, spd, term);
		
	}
	
	@Override
	public Point getVector(Program parent) {
		PVector pos = tgtObj.getLocalCenter();
		RMatrix rot = tgtObj.getLocalOrientation();
		
		PVector zAxis = new PVector(rot.getEntryF(2, 0), rot.getEntryF(2, 1), rot.getEntryF(2, 2));
		float height = tgtObj.getForm().getDim(DimType.HEIGHT);
		pos.add(zAxis.mult(height));
		
		return new Point(pos, rot);
	}
	
}
