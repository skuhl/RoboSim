package expression;

import regs.DataRegister;

public class OperandDReg extends OperandRegister<DataRegister> implements FloatMath {
	public OperandDReg(DataRegister v) {
		super(v, Operand.DREG);
	}
		
	@Override
	public Operand<DataRegister> clone() {
		return new OperandDReg(value);
	}

	@Override
	public Float getArithValue() {
		return value.value;
	}
}
