package expression;

public class OperandGeneric extends Operand<Object> {
	public OperandGeneric() {
		super(null, Operand.UNINIT);
	}

	@Override
	public OperandGeneric clone() {
		return new OperandGeneric();
	}
}
