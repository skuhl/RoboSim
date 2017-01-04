package programming;
import java.util.ArrayList;

import expression.ExprOperand;
import expression.ExpressionElement;

public class SelectStatement extends Instruction {
	private ExprOperand arg;
	private ArrayList<ExprOperand> cases;
	private ArrayList<Instruction> instrs;

	public SelectStatement() {
		setArg(new ExprOperand());
		setCases(new ArrayList<ExprOperand>());
		setInstrs(new ArrayList<Instruction>());
		addCase();
	}

	public SelectStatement(ExprOperand a) {
		setArg(a);
		setCases(new ArrayList<ExprOperand>());
		setInstrs(new ArrayList<Instruction>());
		addCase();
	}

	public SelectStatement(ExprOperand a, ArrayList<ExprOperand> cList, ArrayList<Instruction> iList) {
		setArg(a);
		setCases(cList);
		setInstrs(iList);
	}

	public void addCase() {
		getCases().add(new ExprOperand());
		getInstrs().add(new Instruction());
	}

	public void addCase(ExprOperand e, Instruction i) {
		getCases().add(e);
		getInstrs().add(i);
	}

	public Instruction clone() {   
		ExprOperand newArg = getArg().clone();
		ArrayList<ExprOperand> cList = new ArrayList<ExprOperand>();
		ArrayList<Instruction> iList = new ArrayList<Instruction>();

		for(ExprOperand o : getCases()) {
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

	public int execute() {
		for(int i = 0; i < getCases().size(); i += 1) {
			ExprOperand c = getCases().get(i);
			if(c == null) return 1;

			//println("testing case " + i + " = " + cases.get(i).getDataVal() + " against " + arg.getDataVal());

			if(c.type != ExpressionElement.UNINIT && getArg().getDataVal() == c.getDataVal()) {
				Instruction instr = getInstrs().get(i);

				if(instr instanceof JumpInstruction || instr instanceof CallInstruction) {
					//println("executing " + instrs.get(i).toString());
					instr.execute();
				}
				break;
			}
		}

		return 0;
	}

	public ExprOperand getArg() {
		return arg;
	}

	public ArrayList<ExprOperand> getCases() {
		return cases;
	}

	public ArrayList<Instruction> getInstrs() {
		return instrs;
	}

	public void setArg(ExprOperand arg) {
		this.arg = arg;
	}

	public void setCases(ArrayList<ExprOperand> cases) {
		this.cases = cases;
	}

	public void setInstrs(ArrayList<Instruction> instrs) {
		this.instrs = instrs;
	}

	public String toString() {
		return "";
	}

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
}