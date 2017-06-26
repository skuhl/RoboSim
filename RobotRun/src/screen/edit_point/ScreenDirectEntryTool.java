package screen.edit_point;

import core.RobotRun;
import enums.ScreenMode;
import frame.UserFrame;
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
	public void actionEntr() {
		// User defined x, y, z, w, p, and r values
		float[] inputs = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };

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
					robotRun.updatePendantScreen();
					System.err.println("All entries must have a value!");
					return;
				}

				// Remove prefix
				inputs[val] = Float.parseFloat(str);
				// Bring within range of values
				inputs[val] = Math.max(-9999f, Math.min(inputs[val], 9999f));
			}

			robotRun.createFrameDirectEntry(robotRun.teachFrame, inputs);
			
		} catch (NumberFormatException NFEx) {
			// Invalid number
			System.err.println("Entries must be real numbers!");
			return;
		}

		if (robotRun.teachFrame instanceof UserFrame) {
			robotRun.nextScreen(ScreenMode.UFRAME_DETAIL);
			
		} else {
			robotRun.nextScreen(ScreenMode.TFRAME_DETAIL);
		}
	}
	
	@Override
	public void actionF2() {
		robotRun.lastScreen();
	}
}
