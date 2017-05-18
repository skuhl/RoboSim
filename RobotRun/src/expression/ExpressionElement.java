package expression;


public interface ExpressionElement {
	//operator types
	public static final int ARITH_OP = 0;
	public static final int BOOL_OP = 1;

	//operand types
	public static final int UNINIT = -2;
	public static final int SUBEXP = -1;
	public static final int FLOAT = 0;
	public static final int BOOL = 1;
	public static final int DREG = 2;
	public static final int IOREG = 3;
	public static final int PREG = 4;
	public static final int PREG_IDX = 5;
	public static final int POSTN = 6;

	public abstract int getLength();
	@Override
	public abstract String toString();
	public abstract String[] toStringArray();
}