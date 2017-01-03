package expression;

public interface Operand {
	/* Return an independent replica of this object */
	public abstract Operand clone();
	/* Should return either a Float or RegStmtPoint Object */
	public abstract Object getValue();
}