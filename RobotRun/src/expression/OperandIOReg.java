package expression;

import global.Fields;
import regs.IORegister;

public class OperandIOReg extends OperandRegister<IORegister> implements BoolMath {
	public OperandIOReg(IORegister v) {
		super(v, Operand.IOREG);
	}

	@Override
	public Operand<IORegister> clone() {
		return new OperandIOReg(value);
	}

	@Override
	public Boolean getBoolValue() {
		return value.state == Fields.ON;
	}
}
