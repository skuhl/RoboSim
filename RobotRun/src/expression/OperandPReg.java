package expression;

import geom.Point;
import regs.PositionRegister;

public class OperandPReg extends OperandRegister<PositionRegister> implements PointMath {
	public OperandPReg() {
		super(new PositionRegister(), Operand.PREG);
	}
	
	public OperandPReg(PositionRegister v) {
		super(v, Operand.PREG);
	}
	
	@Override
	public OperandPReg clone() {
		return new OperandPReg(value);
	}
	
	@Override
	public Point getPointValue() {
		if (value != null && value.point != null) {
			return value.point.clone();
		}
		
		return null;
	}

	public Boolean isCart() {
		return value.isCartesian;
	}
}
