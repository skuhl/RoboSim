package expression;
/**
 * A operand that represents a floating-point contant value.
 */
public class ConstantOp implements Operand {
	private final float value;

	public ConstantOp() {
		value = 0f;
	}

	public ConstantOp(float val) {
		value = val;
	}

	public Object getValue() { return new Float(value); }

	public Operand clone() {
		return new ConstantOp(value);
	}

	public String toString() {
		return String.format("%4.3f", value);
	}
}