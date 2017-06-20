package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;

public class ScreenSelectPasteOpt extends ST_ScreenOptionsMenu {

	public ScreenSelectPasteOpt(RobotRun r) {
		super(ScreenMode.SELECT_PASTE_OPT, r);
	}

	@Override
	String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}

	@Override
	void loadOptions() {
		options.addLine("1 Logic");
		options.addLine("2 Position");
		options.addLine("3 Pos ID");
		options.addLine("4 R Logic");
		options.addLine("5 R Position");
		options.addLine("6 R Pos ID");
		options.addLine("7 RM Pos ID");
	}

	@Override
	public void actionEntr() {
		switch(options.getLineIdx()) {
		case 0: robotRun.pasteInstructions(Fields.CLEAR_POSITION);
				break;
		case 1: robotRun.pasteInstructions(Fields.PASTE_DEFAULT);
				break;
		case 2: robotRun.pasteInstructions(Fields.NEW_POSITION);
				break;
		case 3: robotRun.pasteInstructions(Fields.PASTE_REVERSE | Fields.CLEAR_POSITION);
				break;
		case 4: robotRun.pasteInstructions(Fields.PASTE_REVERSE);
				break;
		case 5: robotRun.pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION);
				break;
		case 6: robotRun.pasteInstructions(Fields.PASTE_REVERSE | Fields.REVERSE_MOTION);
				break;
		case 7: robotRun.pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION | Fields.REVERSE_MOTION);
				break;
		}
				
		ScreenState prev = null;
		while (!robotRun.screenStates.isEmpty()) {
			prev = robotRun.screenStates.peek();
			
			if (prev.mode == ScreenMode.NAV_INSTR_MENU) {
				break;
			}
			
			robotRun.screenStates.pop();
		}
		
		robotRun.lastScreen();
	}
}
