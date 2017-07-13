package expression;

public class ExpressionEvaluationException extends Exception {
	private static final long serialVersionUID = 8700378141664676258L;
	private final String errorMsg;
	
	public ExpressionEvaluationException(String msg) {
		errorMsg = msg;
	}
	
	public void printMessage() {
		System.err.println(errorMsg);
	}	
}
