package expression;

public class OperandFloat extends Operand<Float> implements FloatMath {	
	public OperandFloat(float v) {
		super(v, Operand.FLOAT);
	}

	@Override
	public Operand<Float> clone() {
		return new OperandFloat(value);
	}

	@Override
	public Float getArithValue() {
		return value;
	}
	
}
