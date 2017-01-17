package expression;
import java.util.ArrayList;

import robot.RobotRun;

public class Expression extends AtomicExpression {
	private ArrayList<ExpressionElement> elementList;

	public Expression() {
		elementList = new ArrayList<ExpressionElement>();
		elementList.add(new ExprOperand());
	}

	public Expression(ArrayList<ExpressionElement> e) {
		elementList = e;
	}

	public Expression clone() {
		ArrayList<ExpressionElement> newList = new ArrayList<ExpressionElement>();
		for(ExpressionElement e : elementList) {
			if(e instanceof ExprOperand) {
				newList.add(((ExprOperand)e).clone());
			} else {
				newList.add((Operator)e);
			}
		}

		return new Expression(newList);
	}

	public ExprOperand evaluate() {
		if(elementList.get(0) instanceof Operator || elementList.size() % 2 != 1) { 
			RobotRun.println("Expression formatting error");
			return null;
		}

		ExprOperand result = (ExprOperand)elementList.get(0);    
		for(int i = 1; i < elementList.size(); i += 2) {
			if(!(elementList.get(i) instanceof Operator) || !(elementList.get(i + 1) instanceof ExprOperand)) {
				RobotRun.println("Expression formatting error");
				return null;
			} 
			else {
				Operator op = (Operator) elementList.get(i);
				ExprOperand nextOperand = (ExprOperand) elementList.get(i + 1);
				AtomicExpression expr = new AtomicExpression(result, nextOperand, op);

				//println(result.getDataVal() + op.toString() + nextOperand.getDataVal() + " = " + expr.evaluate().dataVal);
				result = expr.evaluate();
			}
		}

		return result;
	}

	public ExpressionElement get(int idx) {
		return elementList.get(idx);
	}

	public int getLength() {
		int len = 2;
		for(ExpressionElement e: elementList) {
			len += e.getLength();
		}

		return len;
	}

	public ExprOperand getOperand(int idx) {
		if(elementList.get(idx) instanceof ExprOperand)
			return (ExprOperand)elementList.get(idx);
		else
			return null;
	}

	public Operator getOperator(int idx) {
		if(elementList.get(idx) instanceof Operator)
			return (Operator)elementList.get(idx);
		else
			return null;
	}

	/**
	 * Returns the number of elements in the top level of the expression
	 */
	public int getSize() {
		return elementList.size();
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
			if(elementList.get(0) instanceof ExprOperand) {
				elementList.add(0, Operator.UNINIT);
			} else {
				elementList.add(0, new ExprOperand());
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
				if(e instanceof ExprOperand) {
					elementList.add(elements[edit_idx] + 1, Operator.UNINIT);
				} else {
					elementList.add(elements[edit_idx] + 1, new ExprOperand());
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
		if(elementList.size() > 1) {
			int[] elements = mapToEdit();
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

	public ExprOperand setOperand(int idx, ExprOperand o) {
		if(elementList.get(idx) instanceof ExprOperand) {
			elementList.set(idx, o);
			return (ExprOperand)elementList.get(idx);
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

	public String toString() {
		String ret = "(" + elementList.get(0).toString();
		for(int i = 0; i < elementList.size(); i += 1) {
			ret += elementList.get(i).toString();
		}

		ret += ")";
		return ret;
	}

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
}