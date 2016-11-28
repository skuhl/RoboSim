package programming;
import expression.AtomicExpression;
import expression.BooleanExpression;
import expression.ExprOperand;
import expression.Expression;
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
public class IfStatement extends Instruction {
	AtomicExpression expr;
	Instruction instr;

	public IfStatement() {
		expr = new Expression();
		instr = null;
	}

	public IfStatement(Operator o, Instruction i){
		expr = new BooleanExpression(o);
		instr = i;
	}

	public IfStatement(AtomicExpression e, Instruction i) {
		expr = e;
		instr = i;
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

	public int execute() {
		ExprOperand result = expr.evaluate();

		if(result == null || result.getBoolVal() == null) {
			return 1;
		} else if(expr.evaluate().getBoolVal()){
			instr.execute();
		}

		return 0;
	}

	public String toString() {
		return "IF " + expr.toString() + " : " + instr.toString();
	}

	public String[] toStringArray() {
		String[] exprArray = expr.toStringArray();
		String[] instArray, ret;
		if(instr == null) {
			ret = new String[exprArray.length + 2];
			ret[ret.length - 1] = "...";
		} else {
			ret = new String[exprArray.length + 3];
			instArray = instr.toStringArray();
			ret[ret.length - 2] = instArray[0];
			ret[ret.length - 1] = instArray[1];
		}

		ret[0] = "IF";
		for(int i = 1; i < exprArray.length + 1; i += 1) {
			ret[i] = exprArray[i - 1];
		}
		ret[exprArray.length] += " :";

		return ret;
	}

	public Instruction clone() {
		Instruction copy = new IfStatement(expr.clone(), instr.clone());
		copy.setIsCommented( isCommented() );
		// TODO actually copy the if statement
		return copy;
	}
}