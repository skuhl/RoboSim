package programming;
import java.util.ArrayList;

import expression.Operand;
import expression.OperandGeneric;
import expression.Operator;

//TODO fix value comparison, it definitely doesn't work

public class SelectStatement extends Instruction implements ExpressionEvaluation {
	private Operand<?> arg;
	private ArrayList<Operand<?>> cases;
	private ArrayList<Instruction> instrs;

	public SelectStatement() {
		setArg(new OperandGeneric());
		setCases(new ArrayList<Operand<?>>());
		setInstrs(new ArrayList<Instruction>());
		addCase();
	}

	public SelectStatement(Operand<?> a) {
		setArg(a);
		setCases(new ArrayList<Operand<?>>());
		setInstrs(new ArrayList<Instruction>());
		addCase();
	}

	public SelectStatement(Operand<?> a, ArrayList<Operand<?>> cList, ArrayList<Instruction> iList) {
		setArg(a);
		setCases(cList);
		setInstrs(iList);
	}

	public void addCase() {
		getCases().add(new OperandGeneric());
		getInstrs().add(new Instruction());
	}

	public void addCase(Operand<?> e, Instruction i) {
		getCases().add(e);
		getInstrs().add(i);
	}

	@Override
	public Instruction clone() {   
		Operand<?> newArg = getArg().clone();
		ArrayList<Operand<?>> cList = new ArrayList<>();
		ArrayList<Instruction> iList = new ArrayList<>();

		for(Operand<?> o : getCases()) {
			cList.add(o.clone());
		}

		for(Instruction i : getInstrs()) {
			iList.add(i.clone());
		}

		SelectStatement copy = new SelectStatement(newArg, cList, iList);
		copy.setIsCommented( isCommented() );
		
		return copy;
	}

	public void deleteCase(int idx) {
		if(getCases().size() > 1) {
			getCases().remove(idx);
		}

		if(getInstrs().size() > 1) {
			getInstrs().remove(idx);
		}
	}

	@Override
	public int execute() {
		for(int i = 0; i < getCases().size(); i += 1) {
			Operand<?> c = getCases().get(i);
			if(c == null) return -1;

			//println("testing case " + i + " = " + cases.get(i).getDataVal() + " against " + arg.getDataVal());
			
			//TODO test select statements
			if(c.getType() != Operand.UNINIT && getArg().getValue() == c.getValue()) {
				Instruction instr = getInstrs().get(i);

				if(instr instanceof JumpInstruction || instr instanceof CallInstruction) {
					//println("executing " + instrs.get(i).toString());
					return instr.execute();
					
				}
				
				break;
			}
		}

		return -1;
	}

	public Operand<?> getArg() {
		return arg;
	}

	public ArrayList<Operand<?>> getCases() {
		return cases;
	}

	public ArrayList<Instruction> getInstrs() {
		return instrs;
	}

	public void setArg(Operand<?> arg) {
		this.arg = arg;
	}

	public void setCases(ArrayList<Operand<?>> cases) {
		this.cases = cases;
	}

	public void setInstrs(ArrayList<Instruction> instrs) {
		this.instrs = instrs;
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public String[] toStringArray() {
		String[] ret = new String[2 + 4*getCases().size()];
		ret[0] = "SELECT";
		ret[1] = getArg().toString();

		for(int i = 0; i < getCases().size(); i += 1) {
			String[] iString = getInstrs().get(i).toStringArray();

			ret[i*4 + 2] = "= " + getCases().get(i).toString();
			ret[i*4 + 3] = iString[0];
			ret[i*4 + 4] = iString.length == 1 ? "..." : iString[1];
			ret[i*4 + 5] = "\n";
		}

		return ret;
	}

	@Override
	public Operand<?> setOperand(int idx, Operand<?> o) {
		if(idx == -1) {
			return arg = o;
		} else {
			return cases.set(idx, o);
		}
	}

	@Override
	public Operator setOperator(int idx, Operator o) {
		return null;
	}

	@Override
	public Operand<?> getOperand(int idx) {
		if(idx == 0) {
			return arg;
		} else {
			return cases.get(idx - 1);
		}
	}

	@Override
	public Operator getOperator(int idx) {
		return null;
	}
}