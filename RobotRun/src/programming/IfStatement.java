package programming;
import expression.BooleanBinaryExpression;
import expression.Expression;
import expression.Operand;
import expression.OperandBool;
import expression.Operator;

/**
 * An if statement consists of an expression and an instruction. If the expression evaluates
 * to true, the execution of this if statement will result in the execution of the associated
 * instruction.
 *
 * Legal operators for the if statement expression are "=, <>, >, <, >=, and <=," which 
 * correspond to the equal, not equal, greater-than, less-than, greater-than or equal to,
 * and less-than or equal to operations, respectively.
 *
 * @param o - the operator to use for this if statement's expression.
 * @param i - the instruction to be executed if the statement expression evaluates to true.
 */
public class IfStatement extends Instruction implements ExpressionEvaluation {
	Expression expr;
	Instruction instr;

	public IfStatement() {
		expr = new Expression();
		instr = null;
	}
	
	public IfStatement(Expression e) {
		expr = e;
		instr = null;
	}

	public IfStatement(Expression e, Instruction i) {
		expr = e;
		instr = i;
	}

	public IfStatement(Operator o, Instruction i) {
		expr = new BooleanBinaryExpression(o);
		instr = i;
	}
	
	@Override
	public Instruction clone() {
		Instruction copy;
		
		if(instr != null) copy = new IfStatement(expr.clone(), instr.clone());
		else 			  copy = new IfStatement(expr.clone());
		
		copy.setIsCommented( isCommented() );
		
		return copy;
	}
	
	/**
	 * Evaluates the expression associated with this if statement.
	 * 
	 * @return	0	the expression evaluates to true,
	 * 			1	the expression evaluates to false,
	 * 			2	an error occurred during expression evaluation
	 */
	public int evalExpression() {
		Operand<?> result = expr.evaluate();

		if (result instanceof OperandBool) {
			if (((OperandBool) result).getBoolValue()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 2;
		}
	}

	public Expression getExpr() {
		return expr;
	}

	public Instruction getInstr() {
		return instr;
	}

	public void setExpr(BooleanBinaryExpression expr) {
		this.expr = expr;
	}

	public void setInstr(Instruction instr) {
		this.instr = instr;
	}

	@Override
	public String toString() {
		return String.format("IF %s : %s\n", expr, instr);
	}

	@Override
	public String[] toStringArray() {
		String[] exprArray = expr.toStringArray();
		String[] ret;
		
		if(instr == null) {
			ret = new String[exprArray.length + 2];
			ret[ret.length - 1] = "...";
			
		} else {
			String[] instArray = instr.toStringArray();
			ret = new String[exprArray.length + instArray.length + 1];
			
			for (int idx = 0; idx < instArray.length; ++idx) {
				ret[idx + exprArray.length + 1] = instArray[idx];
			}
		}

		ret[0] = "IF";
		for(int i = 1; i < exprArray.length + 1; i += 1) {
			ret[i] = exprArray[i - 1];
		}
		ret[exprArray.length] += " :";

		return ret;
	}

	@Override
	public Operand<?> setOperand(int idx, Operand<?> o) {
		Operand<?> ret = null;
		
		if(expr instanceof BooleanBinaryExpression) {
			if(idx == 0) {
				ret = ((BooleanBinaryExpression)expr).setArg1(o);
			} else if(idx == 2) {
				ret = ((BooleanBinaryExpression)expr).setArg2(o);
			}
		} else {
			ret = ((Expression)expr).setOperand(idx, o);
		}
		
		return ret;
	}

	@Override
	public Operator setOperator(int idx, Operator o) {
		Operator ret;
		
		if(expr instanceof BooleanBinaryExpression) {
			ret = ((BooleanBinaryExpression)expr).setOperator(o);
		} else {
			ret = expr.setOperator(idx, o);
		}
		
		return ret;
	}

	@Override
	public Operand<?> getOperand(int idx) {
		Operand<?> ret = null;
		
		if(expr instanceof BooleanBinaryExpression) {
			if(idx == 0) {
				ret = ((BooleanBinaryExpression)expr).getArg1();
			} else if(idx == 2) {
				ret = ((BooleanBinaryExpression)expr).getArg2();
			}
		} else {
			ret = ((Expression)expr).getOperand(idx);
		}
		
		return ret;
	}

	@Override
	public Operator getOperator(int idx) {
		Operator ret;
		
		if(expr instanceof BooleanBinaryExpression) {
			ret = ((BooleanBinaryExpression)expr).getOperator();
		} else {
			ret = expr.getOperator(idx);
		}
		
		return ret;
	}
	
	@Override
	public int getHeaderLength() {
		//Number of elements before expression start
		return expr instanceof Expression ? 3 : 2;
	}
}