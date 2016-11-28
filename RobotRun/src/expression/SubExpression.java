package expression;
import robot.RobotRun;

/**
 * Store a separate expression nested inside another expression as an operand
 */
public class SubExpression implements Operand {
	private final RobotRun robotRun;
	private final RegisterExpression expr;

	public SubExpression(RobotRun robotRun) {
		this.robotRun = robotRun;
		expr = new RegisterExpression(robotRun);
		expr.addParameter(new ConstantOp(1));
	}

	public SubExpression(RobotRun robotRun, Object... params) {
		this.robotRun = robotRun;
		expr = new RegisterExpression(robotRun, params);
	}

	public Object getValue() throws ExpressionEvaluationException { return expr.evaluate(); }

	public Operand clone() {
		// Copy the expression into a new Sub Expression
		return new SubExpression(robotRun, expr.clone());
	}

	public String toString() {
		return String.format("[ %s ]", expr.toString());
	}
}