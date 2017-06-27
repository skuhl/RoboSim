package screen.opt_menu;

import core.RobotRun;
import global.Fields;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSelectPasteOpt extends ST_ScreenOptionsMenu {

	public ScreenSelectPasteOpt(RobotRun r) {
		super(ScreenMode.SELECT_PASTE_OPT, r);
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg(), true));
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
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, s.conColIdx, s.conRenIdx, 0, 0);
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
				
		Screen prev = null;
		while (!robotRun.getScreenStack().isEmpty()) {
			prev = robotRun.getScreenStack().peek();
			
			if (prev.mode == ScreenMode.NAV_INSTR_MENU) {
				break;
			}
			
			robotRun.getScreenStack().pop();
		}
		
		robotRun.lastScreen();
	}
}
