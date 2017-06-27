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
	
	public ST_ScreenLineSelect(ScreenMode m, RobotRun r) {
		super(m, r);
		
		lineSelectState = null;
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
		
		lineSelectState = new boolean[ contents.size() ];
		
		for (int idx = 0; idx < lineSelectState.length; ++idx) {
			lineSelectState[idx] = false;
		}
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
		// Toggle line select state for the active line
		if (lineIdx >= 0 && lineIdx < lineSelectState.length) {
			lineSelectState[lineIdx] = !lineSelectState[lineIdx];
			//contents.toggleSelect(robotRun.getActiveInstIdx());
			robotRun.updatePendantScreen();
		}
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
