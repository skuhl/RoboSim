package screen.text_entry;

import core.RobotRun;
import frame.ToolFrame;
import global.DataManagement;
import screen.ScreenMode;

public class ScreenToolFrameRename extends ST_ScreenTextEntry {
	
	private ToolFrame selectedFrame;
	
	public ScreenToolFrameRename(RobotRun r, ToolFrame tFrame) {
		super(ScreenMode.TFRAME_RENAME, r);
		selectedFrame = tFrame;
	}

	@Override
	protected String loadHeader() {
		return String.format("TOOL FRAME RENAME");
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
