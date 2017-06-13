package expression;

import geom.Point;
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
