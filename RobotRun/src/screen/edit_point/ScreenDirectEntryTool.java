package screen.edit_point;

import core.RobotRun;
import frame.ToolFrame;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenDirectEntryTool extends ST_ScreenPointEntry {

	public ScreenDirectEntryTool(RobotRun r) {
		super(ScreenMode.DIRECT_ENTRY_TOOL, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("TOOL %d: DIRECT ENTRY", robotRun.curFrameIdx + 1);
	}

	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "[Method]";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadWorkingText() {
		ToolFrame tool = robotRun.getActiveRobot().getToolFrame(robotRun.curFrameIdx);
		String[][] entries = tool.directEntryStringArray();
		
		for(int i = 0; i < entries.length; i += 1) {
			prefixes[i] = entries[i][0];
			workingText[i] = new StringBuilder(entries[i][1]);
		}
	}
	
	@Override
	public void actionEntr() {
		// User defined x, y, z, w, p, and r values
		float[] inputs = new float[6];

		try {
			// Parse each input value
			for (int val = 0; val < inputs.length; ++val) {
				DisplayLine value = contents.get(val);
				String str = new String();
				int sdx;

				/*
				 * Combine all columns related to the value, ignoring the
				 * prefix and last columns
				 */
				for (sdx = 1; sdx < (value.size() - 1); ++sdx) {
					str += value.get(sdx);
				}

				// Ignore any trailing blank spaces
				if (!value.get(sdx).equals("\0")) {
					str += value.get(sdx);
				}

				if (str.length() < 0) {
					// No value entered
					errorMessage("All entries must have a value!");
					return;
				}

				// Remove prefix
				inputs[val] = Float.parseFloat(str);
				// Bring within range of values
				inputs[val] = Math.max(-9999f, Math.min(inputs[val], 9999f));
			}

			robotRun.createFrameDirectEntry(robotRun.teachFrame, inputs);
			robotRun.lastScreen();
			
		} catch (NumberFormatException NFEx) {
			// Not a real number
			errorMessage("Entries must be real numbers!");
		}
	}
	
	@Override
	public void actionF2() {
		robotRun.lastScreen();
	}
}
