package expression;
/* These are used to store the operators used in register statement expressions in the ExpressionSet Object */
public enum Operator implements ExpressionElement {
	ADD("+", ARITH_OP, 2), 
	SUB("-", ARITH_OP, 2), 
	MULT("*", ARITH_OP, 2), 
	DIV("/", ARITH_OP, 2), 
	MOD("%", ARITH_OP, 2), 
	IDIV("|", ARITH_OP, 2),
	
	PT_ADD("+", POINT_OP, 2),
	PT_SUB("-", POINT_OP, 2),
		
	AND("&&", LOGIC_OP, 2),
	OR("||", LOGIC_OP, 2),
	NOT("!", LOGIC_OP, 1),
	
	GRTR(">", BOOL_OP, 2),
	LESS("<", BOOL_OP, 2),
	GREQ(">=", BOOL_OP, 2),
	LSEQ("<=", BOOL_OP, 2),
	EQUAL("=", BOOL_OP, 2),
	NEQUAL("<>", BOOL_OP, 2),
	
	PAR_OPEN("(", NO_OP, Integer.MAX_VALUE),
	PAR_CLOSE(")", NO_OP, Integer.MAX_VALUE),
	UNINIT("_", NO_OP, Integer.MAX_VALUE);

	/**
	 * Returns a specific operator based on the id value given. Integers 0 through
	 * 16 correspond to 17 of the operator types; any other integer value
	 * corresponds to the uninitialized operator.
	 */
	public static Operator getOpFromID(int id) {
		switch(id) {
		case 0:   return Operator.ADD;
		case 1:   return Operator.SUB;
		case 2:   return Operator.MULT;
		case 3:   return Operator.DIV;
		case 4:   return Operator.MOD;
		case 5:   return Operator.IDIV;
		case 6:   return Operator.PAR_OPEN;
		case 7:   return Operator.PAR_CLOSE;
		case 8:   return Operator.EQUAL;
		case 9:   return Operator.NEQUAL;
		case 10:  return Operator.GRTR;
		case 11:  return Operator.LESS;
		case 12:  return Operator.GREQ;
		case 13:  return Operator.LSEQ;
		case 14:  return Operator.AND;
		case 15:  return Operator.OR;
		case 16:  return Operator.NOT;
		default:  return Operator.UNINIT;
		}
	}
	
	private String symbol;
	private int type;
	private int argNo;

	private Operator(String s, int t, int n) {
		symbol = s;
		type = t;
		argNo = n;
	}

	@Override
	public int getLength() {
		return 1;
	}

	/**
	 * Returns the id value associated with this enumeration instance based off of
	 * the getOpFromID method.
	 */
	public int getOpID() {
		switch(this) {
		case ADD:		return 0;
		case SUB:		return 1;
		case MULT:      return 2;
		case DIV:       return 3;
		case MOD:       return 4;
		case IDIV:		return 5;
		case PAR_OPEN:  return 6;
		case PAR_CLOSE: return 7;
		case EQUAL:		return 8;
		case NEQUAL:	return 9;
		case GRTR:      return 10;
		case LESS:      return 11;
		case GREQ:      return 12;
		case LSEQ:      return 13;
		case AND:       return 14;
		case OR:        return 15;
		case NOT:       return 16;
		default:        return 17;
		}
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public int getType() {
		return type;
	}
	
	public boolean matchTypeToArg(Operand<?> arg) {
		switch(type) {
		case ARITH_OP: return arg instanceof FloatMath;
		case BOOL_OP: return arg instanceof FloatMath;
		case LOGIC_OP: return arg instanceof BoolMath;
		case POINT_OP: return arg instanceof PointMath;
		default: return false;
		}
	}
	
	public int getArgNo() {
		return argNo;
	}
	
	@Override
	public String toString() {
		return symbol;
	}

	@Override
	public String[] toStringArray() {
		return new String[] { symbol };
	}
}