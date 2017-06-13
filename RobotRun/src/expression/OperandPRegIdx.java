package expression;

import geom.Point;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;
import regs.PositionRegister;

public class OperandPRegIdx extends OperandRegister<PositionRegister> implements FloatMath {
	private int subIdx;
	
	public OperandPRegIdx() {		
		super(new PositionRegister(), Operand.PREG_IDX);
		subIdx = -1;
	}
	
	public OperandPRegIdx(PositionRegister v, int idx) {		
		super(v, Operand.PREG_IDX);
		subIdx = idx;
	}
	
	@Override
	public Operand<PositionRegister> clone() {
		return new OperandPRegIdx(value, subIdx);
	}
	
	@Override
	public Float getArithValue() {
		if(type == Operand.PREG_IDX) {
			
			if(value.isCartesian) {	
				Point p = value.point;
				/* Since the index is specified by the user, the value must be with
				 * respect to the world frame */
				if (subIdx >= 0 && subIdx < 3) {
					PVector pos = RMath.vToWorld(p.position);
					
					if (subIdx == 0) {
						return pos.x;
						
					} else if (subIdx == 1) {
						return pos.y;
						
					} else {
						return pos.z;
					}
					
				} else if (subIdx >= 3 && subIdx < 6) {
					PVector ori = RMath.nQuatToWEuler(p.orientation);
					
					if (subIdx == 3) {
						return ori.x;
						
					} else if (subIdx == 4) {
						return ori.y;
						
					} else {
						return ori.z;
					}
					
				}
				
			} else {
				/* In the same way, the user sees angles in degrees, however,
				 * they are stored in radians */
				return value.point.angles[subIdx] * PConstants.RAD_TO_DEG;	
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return value.toString(subIdx);
	}
	
	@Override
	public String[] toStringArray() {
		String idxStr = value.toString().substring(0, 4);
		String subStr = (subIdx >= 0 && subIdx < 6) ? "" + (subIdx + 1) : "..."; 
		return new String[] { idxStr + ", ", subStr + "]" };
	}
	
	public int getSubIdx() {
		return subIdx;
	}

	public OperandPRegIdx setSubIdx(int idx) {
		subIdx = idx;
		return this;
	}
}
