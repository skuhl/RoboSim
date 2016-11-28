package programming;
import global.Fields;
import robot.RobotRun;

public class IOInstruction extends Instruction {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	int state;
	int reg;

	public IOInstruction(RobotRun robotRun){
		super();
		this.robotRun = robotRun;
		state = Fields.OFF;
		reg = -1;
	}

	public IOInstruction(RobotRun robotRun, int r, int t) {
		super();
		this.robotRun = robotRun;
		state = t;
		reg = r;
	}

	public int getState(){ return state; }
	public void setState(int s){ state = s; }
	public int getReg(){ return reg; }
	public void setReg(int r){ reg = r; }

	public int execute() {
		robotRun.getArmModel().endEffectorState = state;
		robotRun.getArmModel().checkPickupCollision(this.robotRun.activeScenario);
		return 0;
	}

	public Instruction clone() {
		Instruction copy = new IOInstruction(this.robotRun, state, reg);
		copy.setIsCommented( isCommented() );

		return copy;
	}

	public String[] toStringArray() {
		String[] fields = new String[2];
		// Register index
		if (reg == -1) {
			fields[0] = "IO[...] =";
		} else {
			fields[0] = String.format("IO[%d] =", reg + 1);
		}
		// Register value
		if (state == Fields.ON) {
			fields[1] = "ON";
		} else {
			fields[1] = "OFF";
		}

		return fields;
	}
} // end ToolInstruction class