package regs;
/**
 * An extension of the register class, which holds a floating-point value along
 * with an associated comment.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public class DataRegister extends Register {
	public Float value;

	public DataRegister() {
		super();
		value = null;
	}

	public DataRegister(int i) {
		super(i, null);
		value = null;
	}

	public DataRegister(int i, String c, Float v) {
		super(i, c);
		value = v;
	}
	
	@Override
	protected String regPrefix() {
		return "R";
	}
}