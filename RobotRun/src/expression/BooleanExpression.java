package expression;


public class BooleanExpression extends AtomicExpression {

	public BooleanExpression() {
		super();
	}

	public BooleanExpression(Operator o) {
		if(o.type == BOOL) {
			type = -1;
			setOp(o);
			arg1 = new ExprOperand();
			arg2 = new ExprOperand();
		}
		else {
			type = -1;
			setOp(Operator.UNINIT);
		}
	}

	public void setOperator(Operator o) {
		if(o.type != BOOL) return;
		setOp(o);
	}
}