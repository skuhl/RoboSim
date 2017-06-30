package screen.select_lines;

import core.RobotRun;
import global.Fields;
import programming.Instruction;
import programming.SelectStatement;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenLineSelect extends Screen {
	public static final int UP = 0;
	public static final int DN = 1;
	
	/** Used for determining what lines are selected */
	protected boolean[] lineSelectState;
	protected int direction;
	
	public ST_ScreenLineSelect(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	public boolean[] getLineSelectStates() {
		return lineSelectState;
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
		lineSelectState = new boolean[contents.size()];
		direction = -1;
	}
	
	@Override
	public void actionKeyPress(char key) {}

	@Override
	public void actionUp() {
		if (!robotRun.isProgExec()) {
			try {
				// Lock movement when a program is running
				Instruction instr = robotRun.getActiveInstruction();
				int prevLine = contents.getItemLineIdx();
				int selectStart = contents.getLineIdx();
				robotRun.setActiveInstIdx(contents.moveUp(robotRun.isShift()));
				int curLine = contents.getItemLineIdx();
				
				if(robotRun.isShift()) {
					for(int i = selectStart; i >= contents.getLineIdx(); i -= 1) {
						if(direction != UP || i < selectStart) {
							lineSelectState[i] = !lineSelectState[i];
						}
					}
					
					direction = UP;
				} else {
					direction = -1;
				}
				
				// special case for select statement column navigation
				if (instr instanceof SelectStatement && curLine == 0 && prevLine == 1) {
					contents.setSelectedColumnIdx(contents.getColumnIdx() + 3);
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
			Instruction instr = robotRun.getActiveInstruction();
			int prevIdx = contents.getItemColumnIdx();
			int selectStart = contents.getLineIdx();
			robotRun.setActiveInstIdx(contents.moveDown(robotRun.isShift()));
			int curLine = contents.getItemLineIdx();
			
			if(robotRun.isShift()) {
				for(int i = selectStart; i <= contents.getLineIdx(); i += 1) {
					if(direction != DN || i > selectStart) {
						lineSelectState[i] = !lineSelectState[i];
					}
				}
				
				direction = DN;
			} else {
				direction = -1;
			}

			// special case for select statement column navigation
			if (instr instanceof SelectStatement && curLine > 0) {
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
		int idx = contents.getCurrentItemIdx();
		lineSelectState[idx] = !lineSelectState[idx];
		robotRun.updatePendantScreen();
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
}
