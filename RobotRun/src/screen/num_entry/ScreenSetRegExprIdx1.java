package screen.num_entry;

import core.RobotRun;
import global.Fields;
import programming.RegisterStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetRegExprIdx1 extends ST_ScreenNumEntry {

	public ScreenSetRegExprIdx1(RobotRun r) {
		super(ScreenMode.SET_REG_EXPR_IDX1, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select register index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int idx = Integer.parseInt(workingText.toString());
			RegisterStatement regStmt = (RegisterStatement) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			Register reg = regStmt.getReg();
			int ubound = (reg instanceof IORegister) ? r.numOfEndEffectors()
					: 100;

			if (idx < 1 || idx > ubound) {
				// Index is out of bounds
				Fields.setMessage("Index must be within the range 1 and %d",
						ubound);

			} else {

				if (regStmt.getReg() instanceof DataRegister) {
					regStmt.setRegister(r.getDReg(idx - 1));

				} else if (regStmt.getReg() instanceof IORegister) {
					regStmt.setRegister(r.getIOReg(idx));

				} else if (regStmt.getReg() instanceof PositionRegister) {
					if (regStmt.getPosIdx() < 0) {
						// Update a position register operand
						regStmt.setRegister(r.getPReg(idx - 1));

					} else {
						// Update a position register index operand
						regStmt.setRegister(r.getPReg(idx - 1), regStmt.getPosIdx());
					}
				}
				
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			// Not an integer
			errorMessage("The index must be an integer");
		}
	}
}
