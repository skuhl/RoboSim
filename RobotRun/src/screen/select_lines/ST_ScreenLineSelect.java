package screen.select_lines;

import core.RobotRun;
import global.Fields;
import programming.Instruction;
import programming.MotionInstruction;
import programming.SelectStatement;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenLineSelect extends Screen {
	
	/**
	 * Used for determining what lines are selected
	 */
	protected boolean[] lineSelectState;
	
	private int lastEnterIdx;
	
	public ST_ScreenLineSelect(ScreenMode m, ScreenState prevState, int numOfItems, RobotRun r) {
		super(m, prevState, r);
		
		lineSelectState = new boolean[numOfItems];
		
		for (int idx = 0; idx < lineSelectState.length; ++idx) {
			lineSelectState[idx] = false;
		}
		
		lastEnterIdx = -1;
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg(), false));
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, 0, 0);
	}
	
	@Override
	public void actionKeyPress(char key) {}

	@Override
	public void actionUp() {
		if (!robotRun.isProgExec()) {
			try {
				// Lock movement when a program is running
				Instruction i = robotRun.getActiveInstruction();
				int prevLine = contents.getItemLineIdx();
				robotRun.setActiveInstIdx(contents.moveUp(robotRun.isShift()));
				int curLine = contents.getItemLineIdx();

				// special case for select statement column navigation
				if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
					if (prevLine == 1) {
						contents.setSelectedColumnIdx(contents.getColumnIdx() + 3);
					}
				}

			} catch (IndexOutOfBoundsException IOOBEx) {
				// Issue with loading a program, not sure if this helps ...
				IOOBEx.printStackTrace();
			}

			Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
				contents.getLineIdx(), contents.getColumnIdx(),
				robotRun.getActiveInstIdx(),
				contents.getRenderStart());
		}
	}

	@Override
	public void actionDn() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			Instruction i = robotRun.getActiveInstruction();
			int prevIdx = contents.getItemColumnIdx();
			robotRun.setActiveInstIdx(contents.moveDown(robotRun.isShift()));
			int curLine = contents.getItemLineIdx();

			// special case for select statement column navigation
			if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
				if (prevIdx >= 3) {
					contents.setSelectedColumnIdx(prevIdx - 3);
				} else {
					contents.setSelectedColumnIdx(0);
				}
			}

			Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
					contents.getLineIdx(), contents.getColumnIdx(),
					robotRun.getActiveInstIdx(),
					contents.getRenderStart());
		}
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}
	
	@Override
	public void actionEntr() {
		int lineIdx = robotRun.getActiveInstIdx();
		
		if (robotRun.isShift() && lastEnterIdx != -1 && lastEnterIdx != lineIdx) {
			/* Toggle all lines between the current line and the last line
			 * selected, while shift was set */
			int increment = (lineIdx < lastEnterIdx) ? -1 : 1;
			int size = robotRun.getActiveProg().getNumOfInst();
			
			for (int idx = lastEnterIdx + increment; idx >= 0 && idx < size;
					idx += increment) {
					
				lineSelectState[idx] = !lineSelectState[idx];
				
				if (idx == lineIdx) {
					// Reached current index
					break;
				}
			}
			
		} else {
			// Toggle line select state for the active line
			if (lineIdx >= 0 && lineIdx < lineSelectState.length) {
				lineSelectState[lineIdx] = !lineSelectState[lineIdx];
				//contents.toggleSelect(robotRun.getActiveInstIdx());
				robotRun.updatePendantScreen();
			}
		}
		
		// Save a reference to this index
		lastEnterIdx = lineIdx;
	}
	
	@Override
	public void actionBkspc() {}
	
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
	
	/**
	 * @return	A copy of the screen's line selection state list
	 */
	public boolean[] getLnSelectStates() {
		return lineSelectState.clone();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	public boolean isSelected(int idx) {
		
		if (idx >= 0 && idx < lineSelectState.length) {
			return lineSelectState[idx];
		}
		
		return false;
	}
}
