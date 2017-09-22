package screen.edit_item;

import java.util.ArrayList;

import core.RobotRun;
import global.Fields;
import programming.Instruction;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectPasteOpt extends ST_ScreenEditItem {
	
	private ArrayList<Instruction> instToPaste;
	
	public ScreenSelectPasteOpt(RobotRun r,
			ArrayList<Instruction> instToPaste) {
		
		super(ScreenMode.SELECT_PASTE_OPT, r);
		this.instToPaste = instToPaste;
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		int activeInstIdx = robotRun.getActiveInstIdx();
		int optVal = 0;
		
		switch(options.getLineIdx()) {
		case 0: optVal = Fields.CLEAR_POSITION;
				break;
		case 1: optVal = Fields.NEW_POSITION;
				break;
		case 2: optVal = Fields.PASTE_DEFAULT;
				break;
		case 3: optVal = Fields.PASTE_REVERSE | Fields.CLEAR_POSITION;
				break;
		case 4: optVal = Fields.PASTE_REVERSE | Fields.NEW_POSITION;
				break;
		case 5: optVal = Fields.PASTE_REVERSE;
				break;
		case 6: optVal = Fields.PASTE_REVERSE | Fields.REVERSE_MOTION;
				break;
		case 7: optVal = Fields.PASTE_REVERSE | Fields.NEW_POSITION |
				Fields.REVERSE_MOTION;
				break;
		}
		
		r.pasteInstructions(p, activeInstIdx, instToPaste, optVal);
				
		robotRun.lastScreen();
		robotRun.lastScreen();
		
		if(robotRun.getLastScreen().mode == ScreenMode.NAV_PROG_INSTR) {
			robotRun.getLastScreen().setContentIdx(contents.getLineIdx());
			robotRun.lastScreen();
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("1 Logic");
		options.addLine("2 Position");
		options.addLine("3 Pos ID");
		options.addLine("4 R Logic");
		options.addLine("5 R Position");
		options.addLine("6 R Pos ID");
		options.addLine("7 RM Pos ID");
	}
}
