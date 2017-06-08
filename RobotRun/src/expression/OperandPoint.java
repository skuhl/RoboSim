package expression;

import geom.Point;

public class OperandPoint extends Operand<Point> implements PointMath {

	public OperandPoint(Point v) {
		super(v, Operand.POSTN);
	}
	
	@Override
	public Operand<Point> clone() {
		return new OperandPoint(value);
	}
	
	@Override
	public Point getPointValue() {
		return value;
	}
}
