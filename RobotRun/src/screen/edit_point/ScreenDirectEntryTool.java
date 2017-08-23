package screen.edit_point;

import core.RobotRun;
import frame.ToolFrame;
import global.RMath;
import io.DataManagement;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenDirectEntryTool extends ST_ScreenPointEntry {
	
	/**
	 * TODO comment this
	 * 
	 * @param r
	 * @param idx
	 * @return
	 */
	private static String loadHeader(RoboticArm r, int idx) {
		return String.format("TOOL: %s DIRECT", r.toolLabel(idx));
	}
	
	private int frameIdx;
	
	public ScreenDirectEntryTool(RobotRun r, int frameIdx) {
		super(ScreenMode.DIRECT_ENTRY_TOOL, loadHeader(r.getActiveRobot(),
				frameIdx), r);
		this.frameIdx = frameIdx;
		loadWorkingText();
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
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
			
			PVector origin = new PVector(inputs[0], inputs[1], inputs[2]);
			PVector wpr = new PVector(inputs[3], inputs[4], inputs[5]);
			// Set the direct entry values as the current frame values
			teachFrame.setTCPDirect(origin);
			teachFrame.setOrienDirect( RMath.wEulerToNQuat(wpr) );
			teachFrame.teachDirectEntry();
			// Set the frame as active and save changes to the tmp directory
			r.setActiveToolFrame(frameIdx);
			DataManagement.saveRobotData(r, 2);
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
	
	@Override
	protected String loadHeader() {
		return "";
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
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		String[][] entries = teachFrame.directEntryStringArray();
		
		for(int i = 0; i < entries.length; i += 1) {
			prefixes[i] = entries[i][0];
			workingText[i] = new StringBuilder(entries[i][1]);
		}
	}
}
