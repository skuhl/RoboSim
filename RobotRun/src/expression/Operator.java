package expression;
/* These are used to store the operators used in register statement expressions in the ExpressionSet Object */
public enum Operator implements ExpressionElement {
	ADDTN("+", ARITH_OP), 
	SUBTR("-", ARITH_OP), 
	MULT("*", ARITH_OP), 
	DIV("/", ARITH_OP), 
	MOD("%", ARITH_OP), 
	INTDIV("|", ARITH_OP),
	PAR_OPEN("(", -1),
	PAR_CLOSE(")", -1),
	EQUAL("=", BOOL_OP),
	NEQUAL("<>", BOOL_OP),
	GRTR(">", BOOL_OP),
	LESS("<", BOOL_OP),
	GREQ(">=", BOOL_OP),
	LSEQ("<=", BOOL_OP),
	AND("&&", BOOL_OP),
	OR("||", BOOL_OP),
	NOT("!", BOOL_OP),
	UNINIT("_", -1);

	/**
	 * Returns a specific operator based on the id value given. Integers 0 through
	 * 16 correspond to 17 of the operator types; any other integer value
	 * corresponds to the uninitialized operator.
	 */
	public static Operator getOpFromID(int id) {
		switch(id) {
		case 0:   return Operator.ADDTN;
		case 1:   return Operator.SUBTR;
		case 2:   return Operator.MULT;
		case 3:   return Operator.DIV;
		case 4:   return Operator.MOD;
		case 5:   return Operator.INTDIV;
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
	public final String symbol;

	public final int type;

	private Operator(String s, int t) {
		symbol = s;
		type = t;
	}

	public int getLength() {
		return 1;
	}

	/**
	 * Returns the id value associated with this enumeration instance based off of
	 * the getOpFromID method.
	 */
	public int getOpID() {
		switch(this) {
		case ADDTN:     return 0;
		case SUBTR:     return 1;
		case MULT:      return 2;
		case DIV:       return 3;
		case MOD:       return 4;
		case INTDIV:    return 5;
		case PAR_OPEN:  return 6;
		case PAR_CLOSE: return 7;
		case EQUAL:     return 8;
		case NEQUAL:    return 9;
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

	public String toString() {
		return symbol;
	}

	public String[] toStringArray() {
		return new String[] { toString() };
	}
}