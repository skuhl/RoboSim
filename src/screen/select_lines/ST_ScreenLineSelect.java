package screen.select_lines;

import core.RobotRun;
import global.Fields;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenLineSelect extends Screen {
	public static final int DN = 1;
	public static final int UP = 0;
	
	protected int selStart;
	/** Used for determining what lines are selected */
	protected boolean[] lineSelectState;
	
	public ST_ScreenLineSelect(ScreenMode m, RobotRun r) {
		super(m, r);
		selStart = -1;
	}
	
	@Override
	public void actionArrowDn() {
		if (!robotRun.isProgExec()) {
			
			if(contents.getCurrentItemIdx() < robotRun.getActiveProg().getNumOfInst()-1) {
				System.out.println(contents.getItemLineIdx() + ", " + robotRun.getActiveProg().getNumOfInst());
				do {
					robotRun.setActiveInstIdx(contents.moveDown(robotRun.isShift()));
				} while(contents.getItemLineIdx() != 0);
			}

			Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
					contents.getLineIdx(), contents.getColumnIdx(),
					robotRun.getActiveInstIdx(),
					contents.getRenderStart());
		}
	}
	
	@Override
	public void actionArrowLt() {}

	@Override
	public void actionArrowRt() {}
	
	@Override
	public void actionArrowUp() {
		if (!robotRun.isProgExec()) {
			try {
				
				do {
					robotRun.setActiveInstIdx(contents.moveUp(robotRun.isShift()));
				} while(contents.getItemLineIdx() != 0);
				
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
	public void actionBkspc() {}
	
	@Override
	public void actionEntr() {
		if(contents.getCurrentItemIdx() < lineSelectState.length) {
			if(robotRun.isShift() && selStart != -1) {
				for(int i = Math.min(selStart + 1, contents.getLineIdx()); i < Math.max(selStart, contents.getLineIdx() + 1); i += 1) {
					lineSelectState[i] = !lineSelectState[i];
				}
			} else {
				selStart = contents.getCurrentItemIdx();
				lineSelectState[selStart] = !lineSelectState[selStart];
			}
			
			robotRun.updatePendantScreen();
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
	
	/**
	 * Cancel cut/ copy 
	 */
	@Override
	public void actionF5() {
		robotRun.lastScreen();
		robotRun.getLastScreen().setContentIdx(contents.getLineIdx());
		robotRun.lastScreen();
	}
	
	@Override
	public void actionKeyPress(char key) {}
	
	public boolean[] getLineSelectStates() {
		return lineSelectState;
	}

	protected void clearSelection() {
		lineSelectState = new boolean[robotRun.getActiveProg().getNumOfInst() + 1];
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadInstructions(robotRun.getActiveProg(), false));
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, 0, 0);
		lineSelectState = new boolean[robotRun.getActiveProg().getNumOfInst() + 1];
	}
}
