package expression;

import geom.Point;
import regs.PositionRegister;

public class OperandPReg extends OperandRegister<PositionRegister> implements PointMath {
	
	public OperandPReg(PositionRegister v) {
		super(v, Operand.PREG);
	}
	
	@Override
	public OperandPReg clone() {
		return new OperandPReg(value);
	}
	
	@Override
	public Point getPointValue() {
		return value.point;
	}

	public Boolean isCart() {
		return value.isCartesian;
	}
}
