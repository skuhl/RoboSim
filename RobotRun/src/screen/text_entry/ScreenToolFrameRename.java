package screen.text_entry;

import core.RobotRun;
import frame.ToolFrame;
import global.DataManagement;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenToolFrameRename extends ST_ScreenTextEntry {

	public ScreenToolFrameRename(RobotRun r) {
		super(ScreenMode.TFRAME_RENAME, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("TOOL %d: RENAME", robotRun.curFrameIdx + 1);
	}

	@Override
	public void actionEntr() {
		// Update frame name
		if (workingText.length() > 0 && !workingText.equals("\0")) {
			if (workingText.charAt(workingText.length() - 1) == '\0') {
				// Remove insert character
				workingText.deleteCharAt(workingText.length() - 1);
			}
		}
		
		ToolFrame tFrame = robotRun.getActiveRobot().getToolFrame(robotRun.curFrameIdx);
		tFrame.setName(workingText.toString());
		
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
		robotRun.lastScreen();
	}

}
