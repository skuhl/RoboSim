package expression;


/**
 * This class defines an error that occurs during the evaluation process of an ExpressionSet Object.
 */
public class ExpressionEvaluationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * TODO constructor comment
	 */
	public ExpressionEvaluationException(int flag) {
		// TODO develop message for expression parsing error
		super( String.format("Error: %d", flag) );
	}

	public ExpressionEvaluationException(int flag, Class<?> exception) {
		super( String.format("Error: %d (%s)", flag, exception.toString()));
	}
}