package expression;


public interface ExpressionElement {
	public static final int ARITH_OP = 0;
	public static final int BOOL_OP = 2;
	public static final int LOGIC_OP = 1;
	//operator types
	public static final int NO_OP = -1;
	public static final int POINT_OP = 3;
	
	public abstract int getLength();
	public abstract int getType();
	public abstract String toString();
	public abstract String[] toStringArray();
}