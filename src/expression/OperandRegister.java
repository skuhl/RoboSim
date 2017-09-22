package expression;

import regs.Register;

public abstract class OperandRegister<T extends Register> extends Operand<T> {
	protected int regIdx;
	
	public OperandRegister(T v, int t) {
		super(v, t);
		regIdx = v.idx;
	}
	
	public int getRegIdx() {
		return regIdx;
	}

	public OperandRegister<T> setValue(T v) {
		value = v;
		regIdx = v.idx;
		return this;
	}
}
