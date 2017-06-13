package programming;
import expression.AtomicExpression;
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
	AtomicExpression expr;
	Instruction instr;

	public IfStatement() {
		expr = new Expression();
		instr = null;
	}
	
	public IfStatement(AtomicExpression e) {
		expr = e;
		instr = null;
	}

	public IfStatement(AtomicExpression e, Instruction i) {
		expr = e;
		instr = i;
	}

	public IfStatement(Operator o, Instruction i) {
		expr = new AtomicExpression(o);
		instr = i;
	}
	
	@Override
	public Instruction clone() {
		Instruction copy;
		
		if(expr instanceof Expression) {
			if(instr != null) copy = new IfStatement(((Expression)expr).clone(), instr.clone());
			else 			  copy = new IfStatement(((Expression)expr).clone());
		} else {
			if(instr != null) copy = new IfStatement(expr.clone(), instr.clone());
			else 			  copy = new IfStatement(expr.clone());
		}
		
		copy.setIsCommented( isCommented() );
		
		return copy;
	}

	@Override
	public int execute() {
		Operand<?> result = expr.evaluate();

		if (result instanceof OperandBool) {
			
			if (((OperandBool) result).getBoolValue()) {
				return instr.execute();
			}
			
			return -2;	
		}

		return -1;
	}

	public AtomicExpression getExpr() {
		return expr;
	}

	public Instruction getInstr() {
		return instr;
	}

	public void setExpr(AtomicExpression expr) {
		this.expr = expr;
	}

	public void setInstr(Instruction instr) {
		this.instr = instr;
	}

	@Override
	public String toString() {
		return "IF " + expr.toString() + " : " + instr.toString();
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
		Operand<?> ret;
		
		if(expr instanceof Expression) {
			ret = ((Expression)expr).setOperand(idx, o);
		} else if(idx == 0) {
			ret = expr.setArg1(o);
		} else if(idx == 2) {
			ret = expr.setArg2(o);
		} else {
			ret = null;
		}
		
		return ret;
	}

	@Override
	public Operator setOperator(int idx, Operator o) {
		Operator ret;
		
		if(expr instanceof Expression) {
			ret = ((Expression)expr).setOperator(idx, o);
		} else {
			expr.setOp(o);
			ret = expr.getOp();
		}
		
		return ret;
	}

	@Override
	public Operand<?> getOperand(int idx) {
		if(expr instanceof Expression) {
			return ((Expression)expr).getOperand(idx);
		} else if(idx == 0) {
			return expr.getArg1();
		} else if(idx == 2) {
			return expr.getArg2();
		} else {
			return null;
		}
	}

	@Override
	public Operator getOperator(int idx) {
		if(expr instanceof Expression) {
			return ((Expression)expr).getOperator(idx);
		} else {
			return expr.getOp();
		}
	}
}