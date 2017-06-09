package expression;

import geom.Point;
import processing.core.PVector;
import regs.PositionRegister;

public class OperandPRegIdx extends OperandRegister<PositionRegister> implements FloatMath {
	private int subIdx;
	
	public OperandPRegIdx(PositionRegister v, int idx) {		
		super(v, Operand.PREG_IDX);
		regIdx = v.idx;
		subIdx = idx;
		value = v;
	}
	
	@Override
	public Operand<PositionRegister> clone() {
		return new OperandPRegIdx(value, subIdx);
	}
	
	@Override
	public Float getArithValue() {
		if(type == Operand.PREG_IDX) {
			if(value.isCartesian) {
				return value.point.angles[subIdx];			
			}
			else {
				Point p = value.point;
				PVector pos = p.position;
				PVector ori = p.orientation.toVector();
				
				switch(subIdx) {
				case 0: return pos.x;
				case 1: return pos.y;
				case 2: return pos.z;
				case 3: return ori.x;
				case 4: return ori.y;
				case 5: return ori.z;
				}
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
		String idxStr = value.toString();
		String subStr = (subIdx >= 0 && subIdx < 6) ? "" + subIdx : "..."; 
		return new String[] { idxStr.substring(0, idxStr.length()-2) + ", ", subStr + "]" };
	}
	
	public int getSubIdx() {
		return subIdx;
	}

	public void setSubIdx(int idx) {
		subIdx = idx;
	}
}
