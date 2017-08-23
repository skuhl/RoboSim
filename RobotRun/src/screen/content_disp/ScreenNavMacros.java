package screen.content_disp;

import core.RobotRun;
import global.Fields;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenNavMacros extends ST_ScreenListContents {

	public ScreenNavMacros(RobotRun r) {
		super(ScreenMode.NAV_MACROS, r);
	}

	@Override
	public void actionEntr() {}

	@Override
	public void actionF1() {
		RoboticArm r = robotRun.getActiveRobot();
		
		if (!r.atMacroCapacity()) {
			robotRun.nextScreen(ScreenMode.CREATE_MACRO);
			
		} else {
			Fields.setMessage("No more macros can be defined at this time");
		}
	}

	@Override
	public void actionF4() {
		if(robotRun.getActiveRobot().numOfMacros() > 0) {			
			if (contents.getColumnIdx() == 1) {
				robotRun.nextScreen(ScreenMode.SET_MACRO_PROG);
			} else if (contents.getColumnIdx() == 2) {
				robotRun.nextScreen(ScreenMode.SET_MACRO_TYPE);
			} else if (contents.getColumnIdx() == 3){
				RoboticArm r = robotRun.getActiveRobot();
				if (!r.getMacro(contents.getLineIdx()).isManual())
					robotRun.nextScreen(ScreenMode.SET_MACRO_BINDING);
			}
		}
	}
	
	@Override
	public void actionF5() {
		if(robotRun.isShift()) {
			RoboticArm r = robotRun.getActiveRobot();
			r.rmMacro(contents.getCurrentItemIdx());
			robotRun.updatePendantScreen();
		}
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadMacros());
	}

	@Override
	protected String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "[New]";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Edit]";
		labels[4] = robotRun.isShift() ? "[Delete]" : "";
	}
}
