package screen.text_entry;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;
import screen.ScreenType;
import ui.DisplayLine;

public abstract class ST_ScreenTextEntry extends Screen {
	static final int TEXT_ENTRY_LEN = 16;
	static final char[][] LETTERS = new char[][] {
		{ 'a', 'b', 'c', 'd', 'e', 'f' }, 
		{ 'g', 'h', 'i', 'j', 'k', 'l' },
		{ 'm', 'n', 'o', 'p', 'q', 'r' }, 
		{ 's', 't', 'u', 'v', 'w', 'x' }, 
		{ 'y', 'z', '_', '@', '*', '.' }
	};
	
	/**
	 * Used for comment name input. The user can cycle through the six states
	 * for each function button in this mode:
	 *
	 * F1 -> A-F/a-f F2 -> G-L/g-l F3 -> M-R/m-r F4 -> S-X/s-x F5 -> Y-Z/y-z,
	 * _, @, *, .
	 */
	protected int[] letterStates;
	protected StringBuilder workingText;
	
	public ST_ScreenTextEntry(ScreenMode m, RobotRun r) {
		super(m, r);
		letterStates = new int[5];
		workingText = new StringBuilder("\0");
	}
	
	public String getWorkingText() {
		return workingText.toString();
	}
	
	@Override
	protected void loadContents() {
		contents.addLine("\0");
		DisplayLine line = new DisplayLine();
		
		if (workingText != null) {
			// Give each letter in the name a separate column
			for (int idx = 0; idx < workingText.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
				line.add(Character.toString(workingText.charAt(idx)));
			}
		}

		contents.addLine(line);
	}
		
	@Override
	protected void loadOptions() {
		options.addLine("1. Uppercase");
		options.addLine("2. Lowercase");
	}
	
	@Override
	protected void loadLabels() {
		if (options.getLineIdx() == 0) {
			// F1 - F5
			labels[0] = "[ABCDEF]";
			labels[1] = "[GHIJKL]";
			labels[2] = "[MNOPQR]";
			labels[3] = "[STUVWX]";
			labels[4] = "[YZ_@*.]";
		} else {
			labels[0] = "[abcdef]";
			labels[1] = "[ghijkl]";
			labels[2] = "[mnopqr]";
			labels[3] = "[stuvwx]";
			labels[4] = "[yz_@*.]";
		}
	}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(1, 0, 0, -1, 0);
	}
	
	@Override
	public void actionKeyPress(char key) {
		if (((key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z') || (key >= '0' && key <= '9')
				|| key == '-' || key == '.' || key == '@' || key == '*' || key == '_')) {
			
			int columnIdx = contents.getColumnIdx();

			if (workingText.length() == 0 || columnIdx >= workingText.length()) {
				workingText.append(key);
				columnIdx++;

			} else if (workingText.length() < TEXT_ENTRY_LEN) {
				workingText.insert(columnIdx, key);
				++columnIdx;
			}
			// Edge case of adding a character to an empty text entry
			if (workingText.length() == 1 && workingText.charAt(0) != '\0') {
				workingText.append('\0');
				columnIdx += 2;
			}

			contents.setSelectedColumnIdx(Math.min(columnIdx, workingText.length() - 1));
			robotRun.updatePendantScreen();
		}
	}

	@Override
	public void actionUp() {
		options.moveUp(false);
		resetStates();
	}

	@Override
	public void actionDn() {
		options.moveDown(false);
		resetStates();
	}

	@Override
	public void actionLt() {
		if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			contents.setSelectedColumnIdx(Math.max(0, contents.getColumnIdx() - 1));
			// Reset function key states
			for (int idx = 0; idx < letterStates.length; ++idx) {
				letterStates[idx] = 0;
			}

		} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
			contents.setSelectedColumnIdx(Math.max(1, contents.getColumnIdx() - 1));

		} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
			contents.setSelectedColumnIdx(
					contents.getColumnIdx() - ((contents.getColumnIdx() - 4 >= options.size()) ? 4 : 0));
		}
	}

	@Override
	public void actionRt() {
		if (robotRun.isShift()) {
			// Delete key function
			if (workingText.length() >= 1) {
				workingText.deleteCharAt(contents.getColumnIdx());
				contents.setSelectedColumnIdx(Math.max(0, Math.min(contents.getColumnIdx(), workingText.length() - 1)));
			}

		} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
			contents.setSelectedColumnIdx(
					contents.getColumnIdx() + ((contents.getColumnIdx() + 4 < options.size()) ? 4 : 0));

		} else {
			// Add an insert element if the length of the current
			// comment is less than 16
			int len = workingText.length();
			int columnIdx = contents.getColumnIdx();
			
			if (len <= TEXT_ENTRY_LEN && columnIdx == len - 1 &&
					(len == 0 || workingText.charAt(len - 1) != '\0')) {

				workingText.append('\0');
			}

			contents.setSelectedColumnIdx(Math.min(columnIdx + 1,  workingText.length() - 1));
			robotRun.updatePendantScreen();
		}
		
		resetStates();
	}
	
	@Override
	public void actionBkspc() { 
		// Delete/Backspace function
		if (workingText.length() >= 1) {
			int colIdx = contents.getColumnIdx();

			if (colIdx < 1) {
				// Remove the beginning character
				workingText.deleteCharAt(0);

			} else if (colIdx < workingText.length()) {
				// Remove the character
				workingText.deleteCharAt(colIdx - 1);
			}

			contents.setSelectedColumnIdx(Math.max(0, Math.min(colIdx - 1, workingText.length() - 1)));
		}

		for (int idx = 0; idx < letterStates.length; ++idx) {
			letterStates[idx] = 0;
		}
	}

	@Override
	public void actionF1() {
		editTextEntry(0);
	}

	@Override
	public void actionF2() {
		editTextEntry(1);
	}

	@Override
	public void actionF3() {
		editTextEntry(2);
	}

	@Override
	public void actionF4() {
		editTextEntry(3);
	}

	@Override
	public void actionF5() {
		editTextEntry(4);
	}

	private void editTextEntry(int fIdx) {
		char newChar = LETTERS[fIdx][letterStates[fIdx]];
		if (options.getLineIdx() == 0 && !(fIdx == 4 && letterStates[fIdx] > 1)) {
			// Use upper case character
			newChar = (char) (newChar - 32);
		}

		workingText.setCharAt(contents.getColumnIdx(), newChar);

		// Update current letter state
		letterStates[fIdx] = (letterStates[fIdx] + 1) % 6;
		for (int idx = 0; idx < letterStates.length; idx += 1) {
			// Reset all other letter states
			if (idx != fIdx) {
				letterStates[idx] = 0;
			}
		}
	}
	
	/**
	 * This method loads text to screen in such a way as to allow the user to
	 * input an arbitrary character string consisting of letters (a-z upper and
	 * lower case) and/ or special characters (_, @, *, .) via the function row,
	 * as well as numbers via the number pad. Strings are limited to 16
	 * characters and can be used to name new routines, as well as set remark
	 * fields for frames and instructions.
	 */
	@SuppressWarnings("unused")
	private DisplayLine loadTextInput(String txt) {
		contents.addLine("\0");

		DisplayLine line = new DisplayLine();
		// Give each letter in the name a separate column
		for (int idx = 0; idx < txt.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
			line.add( Character.toString(txt.charAt(idx)) );
		}

		return line;
	}
	
	private void resetStates() {
		for(int i = 0; i < 5; i += 1) {
			letterStates[i] = 0;
		}
	}
}
