package expression;
import geom.Point;

public class AtomicExpression extends Operand<Object> {
	protected Operand<?> arg1;
	protected Operand<?> arg2;
	private Operator op;

	public AtomicExpression(){
		super(null, Operand.SUBEXP);
		op = Operator.UNINIT;
		arg1 = new OperandGeneric();
		arg2 = new OperandGeneric();
	}

	public AtomicExpression(Operand<?> a1, Operand<?> a2, Operator o) {
		super(null, Operand.SUBEXP);
		op = o;
		arg1 = a1;
		arg2 = a2;
	}

	public AtomicExpression(Operator o){
		super(null, Operand.SUBEXP);
		op = o;
		arg1 = new OperandGeneric();
		arg2 = new OperandGeneric();
	}

	private boolean checkTypeCompatable(Operator op, Operand<?> o1, Operand<?> o2) {
		switch(op.getType()) {
		case Operator.ARITH_OP:
		case Operator.BOOL_OP: 	return o1 instanceof FloatMath && o2 instanceof FloatMath;
		case Operator.LOGIC_OP:	return o1 instanceof BoolMath && o2 instanceof BoolMath;
		case Operator.POINT_OP: return o1 instanceof PointMath && o2 instanceof PointMath;
		default:				return false;
		}
	}
	
	@Override
	public AtomicExpression clone() {
		return new AtomicExpression(arg1.clone(), arg2.clone(), op);
	}
	
	public Operand<?> evaluate() {
		int opType = op.getType();
		
		//evaluate any sub-expressions
		if(arg1.type == Operand.SUBEXP) {
			arg1 = ((AtomicExpression)arg1).evaluate();
		}

		if(arg2.type == Operand.SUBEXP) {
			arg2 = ((AtomicExpression)arg2).evaluate();
		}

		//check for type compatability
		if(!checkTypeCompatable(op, arg1, arg2)) {
			return null;
		}
		else if(opType == Operator.ARITH_OP) {
			Float value = evaluateFloat((FloatMath)arg1, (FloatMath)arg2);
			return new OperandFloat(value);
		}
		else if(opType == Operator.LOGIC_OP) {
			Boolean value = evaluateBoolean((BoolMath)arg1, (BoolMath)arg2);
			return new OperandBool(value);
		}
		else if(opType == Operator.BOOL_OP) {
			Boolean value = evaluateArithBool((FloatMath)arg1, (FloatMath)arg2);
			return new OperandBool(value);
		}
		else if(opType == Operator.POINT_OP) {
			Point value = evaluatePosition((PointMath)arg1, (PointMath)arg2);
			return new OperandPoint(value);
		}
		
		return null;
	}
	
	private Boolean evaluateArithBool(FloatMath o1, FloatMath o2) {
		float v1 = o1.getArithValue();
		float v2 = o1.getArithValue();
		
		switch(op) {
		case GRTR:	return v1 > v2;
		case LESS:	return v1 < v2;
		case EQUAL:	return v1 == v2;
		case NEQUAL:return v1 != v2;
		case GREQ:	return v1 >= v2;
		case LSEQ:	return v1 <= v2;
		default:	return null;
		}
	}
	
	private Boolean evaluateBoolean(BoolMath o1, BoolMath o2) {
		boolean b1 = o1.getBoolValue();
		boolean b2 = o2.getBoolValue();
		
		switch(op) {
		case AND:	return b1 && b2;
		case OR:	return b1 || b2;
		case NOT:	return !b1;
		default:	return null;
		}
	}
	
	private Float evaluateFloat(FloatMath o1, FloatMath o2) {
		float v1 = o1.getArithValue();
		float v2 = o2.getArithValue();
		
		switch(op) {
		case ADD:	return v1 + v2;
		case SUB:	return v1 - v2;
		case MULT:	return v1 * v2;
		case DIV:	return v1 / v2;
		case MOD:	return v1 % v2;
		case IDIV:	return (float)((int)v1 / (int)v2);
		default:	return null;
		}
	}
	
	private Point evaluatePosition(PointMath o1, PointMath o2) {
		Point p1 = o1.getPointValue();
		Point p2 = o2.getPointValue();
		
		switch(op) {
		case PT_ADD:	return p1.add(p2);
		case PT_SUB:	return p1.sub(p2);
		default:		return null;
		}
	}

	public Operand<?> getArg1() { return arg1; }
	public Operand<?> getArg2() { return arg2; }

	@Override
	public int getLength() {
		if(getOp() == Operator.UNINIT) {
			return 1;    
		}

		int ret = 1;
		ret += arg1.getLength();
		ret += arg2.getLength();
		ret += (arg1.type == -1) ? 2 : 0;
		ret += (arg2.type == -1) ? 2 : 0;
		return ret;
	}

	public Operator getOp() {
		return op;
	}
	public Operator getOperator() { return getOp(); }

	public Operand<?> setArg1(Operand<?> a) { 
		arg1 = a;
		return arg1;
	}

	public Operand<?> setArg2(Operand<?> a) { 
		arg2 = a;
		return arg2;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	public void setOperator(Operator o) {
		setOp(o);
	}

	@Override
	public String toString(){
		String s = "";

		if(getOp() == Operator.UNINIT){
			return "...";
		}

		if(arg1 instanceof AtomicExpression)
			s += "(" + arg1.toString() + ")";
		else 
			s += arg1.toString();

		s += " " + getOp().getSymbol() + " ";

		if(arg2 instanceof AtomicExpression)
			s += "(" + arg2.toString() + ")";
		else 
			s += arg2.toString();

		return s;
	}

	@Override
	public String[] toStringArray() {
		String[] s1, s2, ret;
		String opString = "";

		if(getOp() == Operator.UNINIT) {
			return new String[]{"..."};
		}

		s1 = arg1.toStringArray();
		opString += " " + getOp().getSymbol() + " ";
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