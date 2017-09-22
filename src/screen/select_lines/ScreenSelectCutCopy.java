package screen.select_lines;

import java.util.ArrayList;

import core.RobotRun;
import programming.Instruction;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectCutCopy extends ST_ScreenLineSelect {
	
	private ArrayList<Instruction> clipBoard;
	
	public ScreenSelectCutCopy(RobotRun r, ArrayList<Instruction> clipBoard) {
		super(ScreenMode.SELECT_CUT_COPY, r);
		this.clipBoard = clipBoard;
	}

	@Override
	public void actionF2() {
		robotRun.nextScreen(ScreenMode.SELECT_PASTE_OPT);
	}

	@Override
	public void actionF3() {
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		int size = p.getNumOfInst();
		clipBoard.clear();

		int remIdx = 0;
		for (int i = 0; i < size; i += 1) {
			
			if (lineSelectState[i]) {
				clipBoard.add(p.getInstAt(remIdx));
				r.rmInstAt(p, remIdx, true);
				
			} else {
				remIdx += 1;
			}
		}
		
		clearSelection();
	}
	
	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		clipBoard.clear();

		for (int i = 0; i < p.getNumOfInst(); i += 1) {
			if (lineSelectState[i])
				clipBoard.add(p.getInstAt(i).clone());
		}
		
		clearSelection();
	}
	
	@Override
	public void actionF5() {
		robotRun.lastScreen();
		robotRun.updateInstructions();
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = clipBoard.isEmpty() ? "" : "[Paste]";
		labels[2] = "[Cut]";
		labels[3] = "[Copy]";
		labels[4] = "[Cancel]";
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Select lines to cut/ copy (ENTER).");
	}
}
