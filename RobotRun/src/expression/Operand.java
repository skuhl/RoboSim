package expression;

public interface Operand {
	/* Should return either a Float or RegStmtPoint Object */
	public abstract Object getValue();
	/* Return an independent replica of this object */
	public abstract Operand clone();
}