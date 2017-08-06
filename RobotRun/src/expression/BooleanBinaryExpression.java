package expression;

public class BooleanBinaryExpression extends Expression {
	protected Operand<?> arg1;
	protected Operand<?> arg2;
	private Operator operator;

	public BooleanBinaryExpression(){
		this(new OperandGeneric(), new OperandGeneric(), Operator.UNINIT);
	}

	public BooleanBinaryExpression(Operand<?> a1, Operand<?> a2, Operator o) {
		operator = o;
		arg1 = a1;
		arg2 = a2;
	}
	
	public BooleanBinaryExpression(Operator o){
		this(new OperandGeneric(), new OperandGeneric(), o);
	}
	
	@Override
	public BooleanBinaryExpression clone() {
		return new BooleanBinaryExpression(arg1.clone(), arg2.clone(), operator);
	}
	
	public Operand<?> evaluate() {
		this.clear();
		this.add(arg1);
		this.add(operator);
		this.add(arg2);
		return super.evaluate();
	}

	public Operand<?> getArg1() { return arg1; }
	public Operand<?> getArg2() { return arg2; }

	@Override
	public int getLength() {
		if(operator == Operator.UNINIT) {
			return 1;    
		}

		int ret = 1;
		ret += arg1.getLength();
		ret += arg2.getLength();
		return ret;
	}

	public Operator getOperator() { return operator; }

	public Operand<?> setArg1(Operand<?> a) { 
		arg1 = a;
		return arg1;
	}

	public Operand<?> setArg2(Operand<?> a) { 
		arg2 = a;
		return arg2;
	}

	public Operator setOperator(Operator op) {
		operator = op;
		return operator;
	}

	@Override
	public String toString(){
		String s = "";

		if(operator == Operator.UNINIT){
			return "...";
		}
		
		s += arg1.toString();
		s += " " + operator.getSymbol() + " "; 
		s += arg2.toString();

		return s;
	}

	@Override
	public String[] toStringArray() {
		String[] s1, s2, ret;
		String opString = "";

		if(operator == Operator.UNINIT) {
			return new String[]{"..."};
		}

		s1 = arg1.toStringArray();
		opString += " " + operator.getSymbol() + " ";
		s2 = arg2.toStringArray();

		int lm1 = (arg1 != null && arg1.type == Operand.SUBEXP) ? 2 : 0;
		int lm2 = (arg2 != null && arg2.type == Operand.SUBEXP) ? 2 : 0;
		ret = new String[s1.length + s2.length + 1 + lm1 + lm2];

		if(lm1 != 0) {
			ret[0] = "(";
			ret[s1.length + 1] = ")";
		}

		ret[s1.length + lm1] = opString;

		if(lm2 != 0) {
			ret[s1.length + lm1 + 1] = "(";
			ret[ret.length - 1] = ")";
		}

		for(int i = lm1/2; i < ret.length; i += 1) {
			if(ret[i] == null) {
				if(i < s1.length + lm1/2) {
					ret[i] = s1[i - lm1/2];
				}
				else {
					ret[i] = s2[i - s1.length - lm1 - 1 - lm2/2];
				}
			}
		}

		return ret;
	}
}