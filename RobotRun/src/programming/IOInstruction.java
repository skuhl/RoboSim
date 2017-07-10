package programming;
import global.Fields;

public class IOInstruction extends Instruction {
	boolean state;
	int reg;

	public IOInstruction(){
		super();
		state = Fields.OFF;
		reg = -1;
	}
	
	public IOInstruction(int r, boolean s) {
		super();
		state = s;
		reg = r;
	}

	@Override
	public Instruction clone() {
		Instruction copy = new IOInstruction(reg, state);
		copy.setIsCommented( isCommented() );

		return copy;
	}
	
	public int getReg(){ return reg; }
	public boolean getState(){ return state; }

	public void setReg(int r){ reg = r; }
	public void setState(boolean s){ state = s; }

	@Override
	public String[] toStringArray() {
		String[] fields = new String[2];
		// Register index
		if (reg == -1) {
			fields[0] = "IO[...] =";
		} else {
			fields[0] = String.format("IO[%d] =", reg);
		}
		// Register value
		if (state == Fields.ON) {
			fields[1] = "ON";
		} else {
			fields[1] = "OFF";
		}

		return fields;
	}
}
