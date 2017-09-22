package expression;

public class ExpressionEvaluationException extends Exception {
	public static final String ERR_EMPTY = "Empty expression error";
	public static final String ERR_FLOAT_NAN = "Floating point operand value not a number";
	public static final String ERR_FORMAT = "Expression formatting error";
	public static final String ERR_INVALID_OP = "Invalid operator type";
	public static final String ERR_TYPE_MISMATCH = "Operator/ operand type mismatch";
	
	private static final long serialVersionUID = 8700378141664676258L;
	private final String errorMsg;
	
	public ExpressionEvaluationException(String msg) {
		errorMsg = msg;
	}
	
	public String getMessage() {
		return errorMsg;
	}	
	
	public void printMessage() {
		System.err.println(errorMsg);
	}
}
