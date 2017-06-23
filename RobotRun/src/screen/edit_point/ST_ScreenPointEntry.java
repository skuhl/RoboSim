package screen.edit_point;

import core.RobotRun;
import enums.ScreenMode;
import geom.Point;
import geom.RQuaternion;
import global.RMath;
import processing.core.PVector;
import screen.Screen;
import screen.ScreenState;
import ui.DisplayLine;

public abstract class ST_ScreenPointEntry extends Screen {
	static final int NUM_ENTRY_LEN = 9;
	protected StringBuilder[] workingText;

	public ST_ScreenPointEntry(ScreenMode m, RobotRun r) {
		super(m, r);
		workingText = loadWorkingText();
	}
	
	@Override
	protected void loadContents() {
		String[] line;

		for (int idx = 0; idx < workingText.length; idx += 1) {
			line = new String[workingText[idx].length() + 1];
			line[0] = Character.toString(workingText[idx].charAt(0));
			// Give each character in the value String it own column
			for (int sdx = 0; sdx < workingText[idx].length(); sdx += 1) {
				line[sdx + 1] = Character.toString(workingText[idx].charAt(sdx));
			}
		
			
			contents.addLine(idx, line);
		}
	}
	
	@Override
	protected void loadOptions() {}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, -1, 0);
	}
	
	protected abstract StringBuilder[] loadWorkingText();
	
	protected Point parsePosFromContents(boolean isCartesian) {
		// Obtain point inputs from UI display text
		float[] inputs = new float[6];

		try {
			for (int idx = 0; idx < inputs.length; ++idx) {
				DisplayLine value = contents.get(idx);
				String inputStr = new String();
				int sdx;

				/*
				 * Combine all columns related to the value, ignoring the prefix
				 * and last column
				 */
				for (sdx = 1; sdx < (value.size() - 1); ++sdx) {
					inputStr += value.get(sdx);
				}

				// Ignore any trailing blank spaces
				if (!value.get(sdx).equals("\0")) {
					inputStr += value.get(sdx);
				}

				inputs[idx] = Float.parseFloat(inputStr);
				// Bring the input values with the range [-9999, 9999]
				inputs[idx] = RMath.clamp(inputs[idx], -9999f, 9999f);
			}

			if (isCartesian) {
				PVector position = RMath.vFromWorld(new PVector(inputs[0], inputs[1], inputs[2]));
				PVector wpr = new PVector(inputs[3], inputs[4], inputs[5]);
				// Convert the angles from degrees to radians, then convert from
				// World to Native frame, and finally convert to a quaternion
				RQuaternion orientation = RMath.wEulerToNQuat(wpr);

				// Use default the Robot's joint angles for computing inverse
				// kinematics
				float[] defJointAngles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
				float[] jointAngles = RMath.inverseKinematics(robotRun.getActiveRobot(), defJointAngles, position,
						orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failed
					return new Point(position, orientation, defJointAngles);
				}
				
				return new Point(position, orientation, jointAngles);
			}
			
			// Bring angles within range: (0, TWO_PI)
			for (int idx = 0; idx < inputs.length; ++idx) {
				inputs[idx] = RMath.mod2PI(inputs[idx] * RMath.DEG_TO_RAD);
			}
			
			return robotRun.getActiveRobot().getToolTipNative(inputs);
		} catch (NumberFormatException NFEx) {
			// Invalid input
			System.err.println("Values must be real numbers!");
			return null;
		}
	}

	@Override
	public void actionKeyPress(char key) {
		if ((key >= '0' && key <= '9') || key == '-' || key == '.') {
			workingText[contents.getLineIdx()].insert(contents.getColumnIdx(), key);
		}
		
		robotRun.updatePendantScreen();
	}
	
	@Override
	public void actionUp() {
		contents.moveUp(false);
	}

	@Override
	public void actionDn() {
		contents.moveDown(false);
	}

	@Override
	public void actionLt() {
		contents.moveLeft();
	}

	@Override
	public void actionRt() {
		DisplayLine entry = contents.getCurrentItem();
		int idx = contents.getColumnIdx();
		int size = entry.size();

		// Delete a digit from the beginning of the number entry
		if (robotRun.isShift()) {
			if (size > 2) {
				entry.remove(idx);
			} else {
				// Leave at least one space value entry
				entry.set(idx, "\0");
			}
		} else {
			if (idx == (entry.size() - 1) && !entry.get(idx).equals("\0") && entry.size() < 10) {
				entry.add("\0");
			}

			contents.moveRight();
		}
	}
	
	@Override
	public void actionBkspc() {
		DisplayLine entry = contents.getCurrentItem();
		int idx = contents.getColumnIdx();

		if (entry.size() > 2) {
			if (idx > 1) {
				contents.setSelectedColumnIdx(--idx);
			}

			entry.remove(idx);
		} else {
			entry.set(idx, "\0");
		}
	}

	@Override
	public void actionF1() {}

	@Override
	public void actionF2() {}

	@Override
	public void actionF3() {}

	@Override
	public void actionF4() {}

	@Override
	public void actionF5() {}
}
