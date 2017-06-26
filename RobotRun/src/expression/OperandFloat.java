package expression;

public class OperandFloat extends Operand<Float> implements FloatMath {
	public OperandFloat() {
		super(null, Operand.FLOAT);
	}
		
	public OperandFloat(Float v) {
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
