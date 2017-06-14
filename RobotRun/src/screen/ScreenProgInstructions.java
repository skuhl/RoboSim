package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.Instruction;
import programming.MotionInstruction;
import programming.SelectStatement;
import robot.RoboticArm;

public class ScreenProgInstructions extends ST_ScreenListContents {

	public ScreenProgInstructions(ScreenMode m, RobotRun r) {
		super(m, r);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void actionUp() {
		if (!robotRun.isProgExec()) {
			try {
				// Lock movement when a program is running
				Instruction i = robotRun.getActiveInstruction();
				int prevLine = robotRun.getSelectedLine();
				robotRun.setActiveInstIdx(contents.moveUp(robotRun.isShift()));
				int curLine = robotRun.getSelectedLine();

				// special case for select statement column navigation
				if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
					if (prevLine == 1) {
						contents.setColumnIdx(contents.getColumnIdx() + 3);
					}
				}

			} catch (IndexOutOfBoundsException IOOBEx) {
				// Issue with loading a program, not sure if this helps ...
				IOOBEx.printStackTrace();
			}

			Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
				contents.getLineIdx(), contents.getColumnIdx(),
				robotRun.getActiveInstIdx(),
				contents.getRenderStart());
		}
	}

	@Override
	public void actionDn() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			Instruction i = robotRun.getActiveInstruction();
			int prevIdx = robotRun.getSelectedIdx();
			robotRun.setActiveInstIdx(contents.moveDown(robotRun.isShift()));
			int curLine = robotRun.getSelectedLine();

			// special case for select statement column navigation
			if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
				if (prevIdx >= 3) {
					contents.setColumnIdx(prevIdx - 3);
				} else {
					contents.setColumnIdx(0);
				}
			}

			Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
					contents.getLineIdx(), contents.getColumnIdx(),
					robotRun.getActiveInstIdx(),
					contents.getRenderStart());
		}
	}
	
	@Override
	public void actionLt() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			contents.moveLeft();
		}
	}
	
	public void actionRt() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			contents.moveRight();
		}
	}
	
	@Override
	void loadHeader() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void loadContents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void loadOptions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void loadLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadVars() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPrev() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		if (r.numOfPrograms() != 0) {
			robotRun.setActiveProgIdx( contents.getActiveIndex() );
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}

	@Override
	public void actionF1() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionF2() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionF3() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionF4() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void actionF5() {
		// TODO Auto-generated method stub
		
	}

}
