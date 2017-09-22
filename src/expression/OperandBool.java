package expression;

public class OperandBool extends Operand<Boolean> implements BoolMath {
	public OperandBool() {
		super(false, Operand.BOOL);
	}
	
	public OperandBool(boolean b) {
		super(b, Operand.BOOL);
	}

	@Override
	public Operand<Boolean> clone() {
		return new OperandBool(value);
	}

	@Override
	public Boolean getBoolValue() {
		return value;
	}
}
