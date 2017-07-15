package screen.text_entry;

import core.RobotRun;
import frame.UserFrame;
import global.DataManagement;
import screen.ScreenMode;

public class ScreenUserFrameRename extends ST_ScreenTextEntry {

	private UserFrame selectedFrame;
	
	public ScreenUserFrameRename(RobotRun r, UserFrame uFrame) {
		super(ScreenMode.UFRAME_RENAME, r);
		selectedFrame = uFrame;
	}

	@Override
	protected String loadHeader() {
		return "USER RENAME";
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
		
		selectedFrame.setName(workingText.toString());
		
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
		robotRun.lastScreen();
	}
}
