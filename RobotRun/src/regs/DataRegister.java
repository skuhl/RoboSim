package regs;
/**
 * An extension of the register class, which holds a floating-point value along
 * with an associated comment.
 * 
 * @author Vincent Druckte
 * @author Joshua Hooker
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
	public String regPrefix() {
		return "R";
	}
}