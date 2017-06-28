package screen.text_entry;

import core.RobotRun;
import frame.UserFrame;
import global.DataManagement;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenUserFrameRename extends ST_ScreenTextEntry {

	public ScreenUserFrameRename(RobotRun r) {
		super(ScreenMode.UFRAME_RENAME, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("USER %d: RENAME", robotRun.curFrameIdx + 1);
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
		
		UserFrame uFrame = robotRun.getActiveRobot().getUserFrame(robotRun.curFrameIdx);
		uFrame.setName(workingText.toString());
		
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
		robotRun.lastScreen();
	}

}
