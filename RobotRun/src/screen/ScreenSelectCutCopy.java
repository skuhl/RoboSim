package screen;

import core.RobotRun;
import enums.ScreenMode;
import programming.Program;

public class ScreenSelectCutCopy extends ST_ScreenLineSelect {

	public ScreenSelectCutCopy(RobotRun r) {
		super(ScreenMode.SELECT_CUT_COPY, r);
	}

	@Override
	void loadOptions() {
		options.addLine("Select lines to cut/ copy (ENTER).");
	}

	@Override
	void loadLabels() {
		labels[0] = "";
		labels[1] = robotRun.clipBoard.isEmpty() ? "" : "[Paste]";
		labels[2] = "[Cut]";
		labels[3] = "[Copy]";
		labels[4] = "[Cancel]";
	}
	
	@Override
	public void actionF2() {
		robotRun.nextScreen(ScreenMode.SELECT_PASTE_OPT);
	}
	
	@Override
	public void actionF3() {
		Program p = robotRun.getActiveProg();
		int size = p.getNumOfInst();
		robotRun.clipBoard.clear();

		int remIdx = 0;
		for (int i = 0; i < size; i += 1) {
			
			if (contents.isSelected(i)) {
				robotRun.clipBoard.add(p.get(remIdx));
				p.rmInstAt(remIdx);
				
			} else {
				remIdx += 1;
			}
		}
	}
	
	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		robotRun.clipBoard.clear();

		for (int i = 0; i < p.getNumOfInst(); i += 1) {
			if (contents.isSelected(i))
				robotRun.clipBoard.add(p.get(i).clone());
		}
	}
	
	@Override
	public void actionF5() {
		robotRun.getScreenStates().pop();
		robotRun.updateInstructions();
	}
}
