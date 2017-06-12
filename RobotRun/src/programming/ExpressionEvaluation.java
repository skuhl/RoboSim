package programming;

import expression.Operand;
import expression.Operator;

public interface ExpressionEvaluation {
	public Operand<?> setOperand(int idx, Operand<?> o);
	public Operator setOperator(int idx, Operator o);
	
	public Operand<?> getOperand(int idx);
	public Operator getOperator(int idx);
}
