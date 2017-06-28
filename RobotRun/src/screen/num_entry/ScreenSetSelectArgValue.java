package screen.num_entry;

import core.RobotRun;
import expression.Operand;
import expression.OperandDReg;
import expression.OperandFloat;
import programming.SelectStatement;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetSelectArgValue extends ST_ScreenNumEntry {

	public ScreenSetSelectArgValue(RobotRun r) {
		super(ScreenMode.SET_SELECT_ARGVAL, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Input value/ register index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			SelectStatement s = (SelectStatement) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			float f = Float.parseFloat(workingText.toString());

			if (robotRun.opEdit.getType() == Operand.UNINIT) {
				robotRun.opEdit = new OperandFloat(f);
				s.setOperand(robotRun.editIdx, robotRun.opEdit);
				
			} else if (robotRun.opEdit.getType() == Operand.DREG) {
				if (f >= 1f && f <= 100f) {
					robotRun.opEdit = new OperandDReg(r.getDReg((int) f - 1));
					s.setOperand(robotRun.editIdx, robotRun.opEdit);
				}
			}	
			
		} catch (NumberFormatException NFex) {
			//TODO display error to user
		}

		robotRun.popScreenStack(1);
		robotRun.lastScreen();
	}
}
