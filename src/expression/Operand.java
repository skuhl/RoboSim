package expression;

public abstract class Operand<T> implements ExpressionElement {
	public static final int BOOL = 2;
	public static final int CAM_MATCH = 8;
	public static final int DREG = 3;
	public static final int FLOAT = 1;
	public static final int IOREG = 4;
	public static final int POSTN = 7;
	public static final int PREG = 5;
	public static final int PREG_IDX = 6;
	public static final int ROBOT = 9;
	public static final int SUBEXP = 0;
	//operand types
	public static final int UNINIT = -1;
	
	protected final int type;
	protected T value;

	//default constructor
	public Operand(T v, int t) {
		value = v;
		type = t;
	}
	
	@Override
	public abstract Operand<T> clone();
	
	@Override
	public int getLength() {
		return (type == PREG_IDX) ? 2 : 1;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	public T getValue() {
		return value;
	}
	
	public Operand<T> setValue(T v) {
		value = v;
		return this;
	}

	@Override
	public String toString() {
		if(value != null)
			return value.toString().toUpperCase();
		else
			return null;
	}
	
	@Override
	public String[] toStringArray() {
		if(value != null)
			return new String[] { value.toString() };
		else
			return new String[] {"..."};
	}
	
}