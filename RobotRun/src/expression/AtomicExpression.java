package expression;
import geom.Point;

public class AtomicExpression extends Operand {
	protected Operand arg1;
	protected Operand arg2;
	private Operator op;

	public AtomicExpression(){
		type = ExpressionElement.SUBEXP;
		setOp(Operator.UNINIT);
		arg1 = new Operand();
		arg2 = new Operand();
	}

	public AtomicExpression(Operand a1, Operand a2, Operator o) {
		type = ExpressionElement.SUBEXP;
		setOp(o);
		arg1 = a1;
		arg2 = a2;
	}

	public AtomicExpression(Operator o){
		type = ExpressionElement.SUBEXP;
		setOp(o);
		arg1 = new Operand();
		arg2 = new Operand();
	}

	@Override
	public AtomicExpression clone() {
		return new AtomicExpression(arg1.clone(), arg2.clone(), getOp());
	}
	
	public Operand evaluate() {
		Operand result;
		int t1 = arg1.type;
		int t2 = arg2.type;
		//operation return type:
		// -1 = uninit, 0 = float,
		// 1 = boolean, 2 = point
		int opType = -1;
		Float o1 = null, o2 = null; //floating point operand values
		Boolean b1 = null, b2 = null; //boolean operand values
		Boolean ptCart = null;
		Point p1 = null, p2 = null; //point operand values

		//evaluate any sub-expressions
		if(t1 == -1) {
			arg1 = ((AtomicExpression)arg1).evaluate();
			t1 = arg1.type;
		}

		if(t2 == -1) {
			arg2 = ((AtomicExpression)arg2).evaluate();
			t2 = arg2.type;
		}

		//check for type compatability
		if(t1 == ExpressionElement.UNINIT || t2 == ExpressionElement.UNINIT) {
			return null;
		} 
		else if(t1 == ExpressionElement.FLOAT || t1 == ExpressionElement.DREG || t1 == ExpressionElement.PREG_IDX) {
			opType = 0;
			o1 = arg1.getDataVal();

			switch(t2) {
			case ExpressionElement.FLOAT:
			case ExpressionElement.DREG:
			case ExpressionElement.PREG_IDX:
				o2 = arg2.getDataVal();
				break;
			default:
				return null;
			}
		}
		else if(t1 == ExpressionElement.BOOL || t1 == ExpressionElement.IOREG) {
			opType = 1;
			b1 = arg1.getBoolVal();

			switch(t2) {
			case ExpressionElement.BOOL:
			case ExpressionElement.IOREG:
				b2 = arg2.getBoolVal();
				break;
			default:
				return null;
			}
		}
		else if(t1 == ExpressionElement.PREG || t1 == ExpressionElement.POSTN) {
			opType = 2;
			Boolean p1Cart = arg1.isCart();
			p1 = arg1.getPointVal();

			switch(t2) {
			case ExpressionElement.PREG:
			case ExpressionElement.POSTN:
				Boolean p2Cart = arg2.isCart();	
				
				if (p1Cart && p2Cart || (!p1Cart & !p2Cart)) {
					// Must be the same type of register
					ptCart = p1Cart;
				}
				
				p2 = arg2.getPointVal();
				break;
			default:
				return null;
			}
		}

		if(opType == 0) {
			if(o1 == null || o2 == null) return null;

			//integer operands for integer operations
			int intop1 = Math.round(o1);
			int intop2 = Math.round(o2);

			switch(getOp()) {
			case ADDTN:
				result = new Operand(o1 + o2);
				break;
			case SUBTR:
				result = new Operand(o1 - o2);
				break;
			case MULT:
				result = new Operand(o1 * o2);
				break;
			case DIV:
				result = new Operand(o1 / o2);
				break;
			case MOD:
				result = new Operand(o1 % o2);
				break;
			case INTDIV:
				result = new Operand(intop1 / intop2);
				break;
			case EQUAL:
				// Do not use == or != with ANY Objects!
				result = new Operand(o1.floatValue() == o2.floatValue());
				break;
			case NEQUAL:
				result = new Operand(o1.floatValue() != o2.floatValue());
				break;
			case GRTR:
				result = new Operand(o1 > o2);
				break;
			case LESS:
				result = new Operand(o1 < o2);
				break;
			case GREQ:
				result = new Operand(o1 >= o2);
				break;
			case LSEQ:
				result = new Operand(o1 <= o2);
				break;
			default:
				result = null;
				break;
			}
		}
		else if(opType == 1) {
			if(b1 == null || b2 == null) return null;

			switch(getOp()) {
			case EQUAL:
				result = new Operand(b1 == b2);
				break;
			case NEQUAL:
				result = new Operand(b1 != b2);
				break;
			case AND:
				result = new Operand(b1 && b2);
			case OR:
				result = new Operand(b1 || b2);
			default:
				result = null;
				break;
			}
		}
		else if(opType == 2) {
			if(p1 == null || p2 == null || ptCart == null) {
				return null;
			}

			switch(getOp()) {
			case ADDTN:
				if (ptCart) {
					// Add Cartesian values
					result = new Operand( p1.add(p2.position, p2.orientation) );
					
				} else {
					// Add joint values
					result = new Operand( p1.add(p2.angles) );
				}
				
				break;
			case SUBTR:
				if (ptCart) {
					// Add Cartesian values
					Point nP2 = p2.negateCartesian();
					result = new Operand( p1.add(nP2.position, nP2.orientation) );
					
				} else {
					// Add joint values
					Point nP2 = p2.negateJoint();
					result = new Operand( p1.add(nP2.angles) );
				}
				break;
			default:
				result = null;
				break;
			}
		}
		else {
			result = null;
		}

		return result;
	}

	public Operand getArg1() { return arg1; }
	public Operand getArg2() { return arg2; }

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

	public Operand setArg(Operand a, int argNo) {
		if(argNo == 1) {
			return setArg1(a);
		} else {
			return setArg2(a);
		}
	}

	public Operand setArg1(Operand a) { 
		arg1 = a;
		return arg1;
	}

	public Operand setArg2(Operand a) { 
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

		s += " " + getOp().symbol + " ";

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
		opString += " " + getOp().symbol + " ";
		s2 = arg2.toStringArray();

		int lm1 = (arg1 != null && arg1.type == -1) ? 2 : 0;
		int lm2 = (arg2 != null && arg2.type == -1) ? 2 : 0;
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