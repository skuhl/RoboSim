package expression;

import java.util.ArrayList;

import geom.Point;
import global.Fields;

public class Expression extends Operand<Object> {
	private ArrayList<ExpressionElement> elementList;

	public Expression() {
		super(null, Operand.SUBEXP);
		elementList = new ArrayList<>();
		elementList.add(new OperandGeneric());
	}

	public Expression(ArrayList<ExpressionElement> e) {
		super(null, Operand.SUBEXP);
		elementList = e;
	}

	public void add(ExpressionElement e) {
		elementList.add(e);
	}
	
	@Override
	public Expression clone() {
		ArrayList<ExpressionElement> newList = new ArrayList<>();
		for(ExpressionElement e : elementList) {
			if(e instanceof Operand<?>){
				newList.add(((Operand<?>)e).clone());
			} else {
				newList.add(e);
			}
		}

		return new Expression(newList);
	}
	
	public Operand<?> evaluate() {
		if (elementList.isEmpty()) {
			Fields.setMessage(ExpressionEvaluationException.ERR_EMPTY);
			return null;
		}
		
		Operand<?> result = null;
		Operator curOp = null;
		boolean notOp = false;
		
		for(int i = 0; i < elementList.size(); i += 1) {
			ExpressionElement cur = elementList.get(i);
			
			try {
				if(cur instanceof Operand<?>) {
					if(cur instanceof Expression) {
						cur = ((Expression)cur).evaluate();
					}
					
					if(notOp) {
						cur = evaluateNot((Operand<?>)cur);
						notOp = false;
					}
					
					if(result == null) {
						result = (Operand<?>)cur;
					} else {
						System.out.println("current result: " + result.getValue());
						result = evaluate(curOp, result, (Operand<?>)cur);
						curOp = null;
					}
				} else if(cur instanceof Operator && ((Operator)cur).getType() != Operator.NO_OP) {
					if((Operator)cur == Operator.NOT) {
						notOp = true;
					} else {
						curOp = (Operator)cur;
					}
				}
			} catch (ExpressionEvaluationException e) {
				Fields.setMessage(e.getMessage());
				e.printMessage();
				return null;
			}
		}
		
		// Map register operands to their respective values
		if (result instanceof OperandIOReg) {
			result = new OperandBool(((OperandIOReg) result).getBoolValue());
		} else if (result instanceof OperandDReg) {
			result = new OperandFloat(((OperandDReg) result).getArithValue());
		} else if (result instanceof OperandPReg) {
			result = new OperandPoint(((OperandPReg) result).getPointValue());
		} else if (result instanceof OperandPRegIdx) {
			result = new OperandFloat(((OperandPRegIdx) result).getArithValue());	
		}
		
		if (result == null || result.getValue() == null) {
			// Return a null operator, not an uninitialized operator
			Fields.setMessage(ExpressionEvaluationException.ERR_FORMAT);
			return null;
		}
		
		System.out.println("final result: " + result.getValue());
		return result;
	}
	
	public ExpressionElement get(int idx) {
		return elementList.get(idx);
	}
	
	@Override
	public int getLength() {
		int len = 2;
		for(ExpressionElement e: elementList) {
			len += e.getLength();
		}

		return len;
	}
	
	public Operand<?> getOperand(int idx) {
		if(elementList.get(idx) instanceof Operand<?>)
			return (Operand<?>)elementList.get(idx);
		else
			return null;
	}
	
	public Operator getOperator(int idx) {
		if(elementList.get(idx) instanceof Operator)
			return (Operator)elementList.get(idx);
		else
			return null;
	}

	public int getStartingIdx(int element) {
		int[] elements = mapToEdit();
		int idx = 0;

		while(elements[idx] != element) {
			idx += 1;
		}

		return idx;
	}

	public void insertElement(int edit_idx) {
		//limit number of elements allowed in this expression
		if(getLength() >= 21) return;
		//ensure index is within the bounds of our list of elements
		else if(edit_idx < -1) return;
		else if(edit_idx >= getLength() - 2) return;

		if(edit_idx == -1) {
			if(elementList.get(0) instanceof Operand<?>) {
				elementList.add(0, Operator.UNINIT);
			} else {
				elementList.add(0, new OperandGeneric());
			}
		}
		else {
			int[] elements = mapToEdit();
			int start_idx = getStartingIdx(elements[edit_idx]);
			ExpressionElement e = elementList.get(elements[edit_idx]);

			if(e instanceof Expression && (edit_idx != start_idx + e.getLength() - 1)) {
				edit_idx -= (start_idx + 1);
				((Expression)e).insertElement(edit_idx);
			} 
			else {
				if(e instanceof Operand<?>) {
					elementList.add(elements[edit_idx] + 1, Operator.UNINIT);
				} else {
					elementList.add(elements[edit_idx] + 1, new OperandGeneric());
				}
			}
		}
	}

	public int[] mapToEdit() {
		int[] ret = new int[getLength() - 2];
		int element_start = 0;
		int element_idx = 0;

		for(int i = 0; i < ret.length; i += 1) {
			int len = elementList.get(element_idx).getLength();
			ret[i] = element_idx;

			if(i - element_start >= len - 1) {
				element_idx += 1;
				element_start = i + 1;
			}
		}

		return ret;
	}

	public void removeElement(int edit_idx) {
		if((elementList.size() > 1 && edit_idx >= 0) || (elementList.size() == 1 && edit_idx >= 1)) {
			int[] elements = mapToEdit();
			
			if (edit_idx < elements.length) {
				int start_idx = getStartingIdx(elements[edit_idx]);
				ExpressionElement e = elementList.get(elements[edit_idx]);
	
				if(e instanceof Expression) {
					if(edit_idx == start_idx || edit_idx == start_idx + e.getLength() - 1) {
						elementList.remove(elements[edit_idx]);
					} else {
						edit_idx -= (start_idx + 1);
						((Expression)e).removeElement(edit_idx);
					}
				} 
				else {
					elementList.remove(elements[edit_idx]);
				}
			}
		}
		else if(edit_idx == 0) {
			elementList.set(0, new OperandGeneric());
		}
	}

	public Operand<?> setOperand(int idx, Operand<?> o) {
		if(idx >= 0 && idx < elementList.size() &&
				elementList.get(idx) instanceof Operand<?>) {
			
			elementList.set(idx, o);
			return (Operand<?>)elementList.get(idx);
		}
		else {
			return null;
		}
	}

	public Operator setOperator(int idx, Operator o) {
		if(elementList.get(idx) instanceof Operator) {
			elementList.set(idx, o);
			return (Operator)elementList.get(idx); 
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the number of elements in the top level of the expression
	 */
	public int size() {
		return elementList.size();
	}
	
	@Override
	public String toString() {
		String ret = "(" + elementList.get(0).toString();
		for(int i = 0; i < elementList.size(); i += 1) {
			ret += elementList.get(i).toString();
		}

		ret += ")";
		return ret;
	}
	
	@Override
	public String[] toStringArray() {
		String[] ret = new String[this.getLength()];
		ret[0] = "(";

		int idx = 1;
		for(ExpressionElement e: elementList) {
			String[] temp = e.toStringArray();
			for(int i = 0; i < temp.length; i += 1) {
				ret[idx + i] = temp[i];
			}
			idx += temp.length;
		}

		ret[ret.length - 1] = ")";
		return ret;
	}

	protected void clear() {
		elementList.clear();
	}

	private Operand<?> evaluate(Operator op, Operand<?> arg1, Operand<?> arg2) throws ExpressionEvaluationException {
		if(op == null || arg1 == null || arg2 == null) {
			throw new ExpressionEvaluationException(ExpressionEvaluationException.ERR_FORMAT);
		}
		
		System.out.println("evaluating " + arg1.getValue() + " " + op.toString() + " " + arg2.getValue());
		
		if(op.matchTypeToArg(arg1) && op.matchTypeToArg(arg2)) {
			// Evaluate the operation
			if(op.getType() == Operator.ARITH_OP || op.getType() == Operator.BOOL_OP) {
				return evaluateFloat(op, (FloatMath)arg1, (FloatMath)arg2);
			} else if(op.getType() == Operator.LOGIC_OP) {
				return evaluateBoolean(op, (BoolMath)arg1, (BoolMath)arg2);
			} else if(op.getType() == Operator.POINT_OP) {
				return evaluatePoint(op, (PointMath)arg1, (PointMath)arg2);
			} else {
				throw new ExpressionEvaluationException(ExpressionEvaluationException.ERR_INVALID_OP);
			}
		} else {
			throw new ExpressionEvaluationException(ExpressionEvaluationException.ERR_TYPE_MISMATCH);
		}
	}

	private Operand<?> evaluateBoolean(Operator op, BoolMath o1, BoolMath o2) {
		boolean b1 = o1.getBoolValue();
		boolean b2 = o2.getBoolValue();
		
		switch(op) {
		case AND:	return new OperandBool(b1 && b2);
		case OR:	return new OperandBool(b1 || b2);
		default:	return null;
		}
	}

	private Operand<?> evaluateFloat(Operator op, FloatMath o1, FloatMath o2) 
			throws ExpressionEvaluationException {
		if (op.getType() == Operator.ARITH_OP) {
			// Arithmetic evaluation
			Float v1 = o1.getArithValue();
			Float v2 = o2.getArithValue();
			
			if(v1.isNaN() || v2.isNaN()) {
				throw new ExpressionEvaluationException(ExpressionEvaluationException.ERR_FLOAT_NAN);
			}
			
			switch (op) {
			case ADD:	return new OperandFloat(v1 + v2);
			case SUB:	return new OperandFloat(v1 - v2);
			case MULT:	return new OperandFloat(v1 * v2);
			case DIV:	return new OperandFloat(v1 / v2);
			case MOD:	return new OperandFloat(v1 % v2);
			case IDIV:
				Integer val = v1.intValue() / v2.intValue();
				return new OperandFloat(val.floatValue());
			default:
			}
			
		} else if (op.getType() == Operator.BOOL_OP) {
			// Logic evaluation
			float v1 = o1.getArithValue().floatValue();
			float v2 = o2.getArithValue().floatValue();
			
			switch (op) {
			case GRTR:		return new OperandBool(v1 > v2);
			case LESS:		return new OperandBool(v1 < v2);
			case EQUAL:		return new OperandBool(v1 == v2);
			case NEQUAL:	return new OperandBool(v1 != v2);
			case GREQ:		return new OperandBool(v1 >= v2);
			case LSEQ:		return new OperandBool(v1 <= v2);
			default:
			}
		}
		
		return null;
	}

	private Operand<?> evaluateNot(Operand<?> arg) throws ExpressionEvaluationException  {
		if(arg instanceof BoolMath) {
			System.out.println("evaluating !" + arg.getValue());
			return new OperandBool(!((BoolMath)arg).getBoolValue());
		} else {
			throw new ExpressionEvaluationException(ExpressionEvaluationException.ERR_TYPE_MISMATCH);
		}
	}

	private Operand<?> evaluatePoint(Operator op, PointMath o1, PointMath o2) {
		Point p1 = o1.getPointValue();
		Point p2 = o2.getPointValue();
		
		switch(op) {
		case ADD:	return new OperandPoint(p1.add(p2));
		case SUB:	return new OperandPoint(p1.sub(p2));
		default:		return null;
		}
	}
}