package screen.text_entry;

import core.RobotRun;
import frame.ToolFrame;
import io.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenToolFrameRename extends ST_ScreenTextEntry {
	
	private int frameIdx;
	
	public ScreenToolFrameRename(RobotRun r, int frameIdx) {
		super(ScreenMode.TFRAME_RENAME, String.format("TOOL: %d RENAME",
				frameIdx + 1), r);
		this.frameIdx = frameIdx;
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
		ToolFrame selectedFrame = r.getToolFrame(frameIdx);
		selectedFrame.setName(workingText.toString());
		
		DataManagement.saveRobotData(robotRun.getActiveRobot(), 2);
		robotRun.lastScreen();
	}

	@Override
	protected String loadHeader() {
		return "";
	}
}
