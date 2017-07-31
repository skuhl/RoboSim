package screen.edit_point;

import core.RobotRun;
import frame.UserFrame;
import global.DataManagement;
import global.RMath;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenDirectEntryUser extends ST_ScreenPointEntry {
	
	private int frameIdx;
	
	public ScreenDirectEntryUser(RobotRun r, int frameIdx) {
		super(ScreenMode.DIRECT_ENTRY_USER, String.format("USER: %d DIRECT",
				frameIdx + 1), r);
		this.frameIdx = frameIdx;
		loadWorkingText();
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
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		String[][] entries = teachFrame.directEntryStringArray();
		
		for(int i = 0; i < entries.length; i += 1) {
			prefixes[i] = entries[i][0];
			workingText[i] = new StringBuilder(entries[i][1]);
		}
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
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
			teachFrame.setOriginDirect( RMath.vFromWorld(origin) );
			teachFrame.setOrienDirect( RMath.wEulerToNQuat(wpr) );
			teachFrame.teachDirectEntry();
			// Set the frame as active and save changes to the tmp directory
			r.setActiveUserFrame(frameIdx);
			DataManagement.saveRobotData(r, 2);
			robotRun.lastScreen();
			
		} catch (NumberFormatException NFEx) {
			// Not a real number
			errorMessage("Entries must be real numbers!");
			return;
		}
	}
	
	@Override
	public void actionF2() {
		robotRun.lastScreen();
	}
}
