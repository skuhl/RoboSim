package screen.edit_point;

import core.RobotRun;
import geom.Point;
import geom.RQuaternion;
import global.Fields;
import global.RMath;
import processing.core.PVector;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;
import ui.DisplayLine;

public abstract class ST_ScreenPointEntry extends Screen {
	static final int NUM_ENTRY_LEN = 9;
	protected String[] prefixes;
	protected StringBuilder[] workingText;

	public ST_ScreenPointEntry(ScreenMode m, RobotRun r) {
		super(m, r);
		workingText = new StringBuilder[6];
		prefixes = new String[6];
	}
	
	public ST_ScreenPointEntry(ScreenMode m, String header, RobotRun r) {
		super(m, header, r);
		workingText = new StringBuilder[6];
		prefixes = new String[6];
	}
	
	@Override
	public void actionArrowDn() {
		contents.moveDown(false);
	}
	
	@Override
	public void actionArrowLt() {
		if(contents.getColumnIdx() > 1) {
			contents.moveLeft();
		}
	}
	
	@Override
	public void actionArrowRt() {
		StringBuilder entry = workingText[contents.getLineIdx()];
		int idx = contents.getColumnIdx() - 1;
		int size = entry.length();

		// Delete a digit from the beginning of the number entry
		if (robotRun.isShift()) {
			if (size > 2) {
				entry.deleteCharAt(idx);
			} else {
				// Leave at least one space value entry
				entry.setCharAt(idx, '\0');
			}
			
		} else {
			if (idx == (size - 1) && entry.charAt(idx) != '\0' && size < 10) {
				entry.append("\0");
				contents.getCurrentItem().add("\0");
			}

			contents.moveRight();
		}
	}
	
	@Override
	public void actionArrowUp() {
		contents.moveUp(false);
	}
	
	@Override
	public void actionBkspc() {
		StringBuilder entry = workingText[contents.getLineIdx()]; 
		int idx = contents.getColumnIdx() - 1;

		if (entry.length() > 1) {
			
			if (idx > 0) {
				contents.setColumnIdx(idx--);
			}
			
			entry.deleteCharAt(idx);
			
		} else {
			entry.setCharAt(idx, '\0');
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

	@Override
	public void actionKeyPress(char key) {
		int idx = contents.getColumnIdx() - 1;
		
		if (idx >= 0 && ((key >= '0' && key <= '9') || key == '-' || key == '.')) {
			StringBuilder line = workingText[contents.getLineIdx()];
			
			if(line.charAt(idx) == '\0') {
				line.setCharAt(idx, key);
				actionArrowRt();
				
			} else if (line.length() < 10) {
				line.insert(idx, key);
				actionArrowRt();
			}
		}
		
		robotRun.updatePendantScreen();
	}

	/**
	 * Sets the application message to the given String.
	 * 
	 * @param msg	The message to render in the applicaiton UI
	 */
	protected void errorMessage(String msg) {
		Fields.setMessage(msg);
	}

	/**
	 * Similar to errorMessage(String), except that this accepts a format
	 * String and its arguments.
	 * 
	 * @param format	The format String
	 * @param args		The arguments for the format String
	 */
	protected void errorMessage(String format, Object... args) {
		errorMessage( String.format(format, args) );
	}
	
	@Override
	protected void loadContents() {
		String[] line;

		for (int idx = 0; idx < workingText.length; idx += 1) {
			line = new String[workingText[idx].length() + 1];
			line[0] = prefixes[idx];
			// Give each character in the value String it own column
			for (int sdx = 1; sdx < line.length; sdx += 1) {
				line[sdx] = Character.toString(workingText[idx].charAt(sdx - 1));
			}

			contents.addLine(line);
		}
	}

	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	protected void loadOptions() {}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, -1, 0);
	}

	protected abstract void loadWorkingText();

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
					Fields.setMessage("No joint angles could be found for the given position and orientation");
					return null;
				}
				
				return new Point(position, orientation, jointAngles);
			}
			
			// Bring angles within range: (0, TWO_PI)
			for (int idx = 0; idx < inputs.length; ++idx) {
				inputs[idx] = RMath.mod2PI(inputs[idx] * RMath.DEG_TO_RAD);
			}
			
			return robotRun.getActiveRobot().getToolTipNative(inputs);
			
		} catch (NumberFormatException NFEx) {
			Fields.setMessage("All input must be real numbers");
			return null;
		}
	}
}
