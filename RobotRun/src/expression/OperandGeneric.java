package expression;

public class OperandGeneric extends Operand<Object> {
	public OperandGeneric() {
		super(null, Operand.UNINIT);
	}

	@Override
	public OperandGeneric clone() {
		return new OperandGeneric();
	}
	
	@Override
	public String toString() {
		return "...";
	}
	
	@Override
	public String[] toStringArray() { 
		return new String[] { "..." };
	}
}
