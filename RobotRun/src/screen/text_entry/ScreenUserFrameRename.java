package screen.text_entry;

import core.RobotRun;
import frame.UserFrame;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenUserFrameRename extends ST_ScreenTextEntry {

	private int frameIdx;
	
	public ScreenUserFrameRename(RobotRun r, int frameIdx) {
		super(ScreenMode.UFRAME_RENAME, r);
		this.frameIdx = frameIdx;
	}

	@Override
	protected String loadHeader() {
		return String.format("USER: %d RENAME", frameIdx);
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
		
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame selectedFrame = r.getUserFrame(frameIdx);
		selectedFrame.setName(workingText.toString());
		
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 1);
		robotRun.lastScreen();
	}
}
