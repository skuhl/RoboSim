package screen.cnfrm_cncl;

import core.RobotRun;
import geom.Point;
import global.Fields;
import programming.Instruction;
import programming.PosMotionInst;
import programming.Program;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenConfirmRenumber extends ST_ScreenConfirmCancel {

	public ScreenConfirmRenumber(ScreenState prevState, RobotRun r) {
		super(ScreenMode.CONFIRM_RENUM, prevState, r);
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg(), false));
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Renumber program positions?");
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, -1, 0);
	}
	
	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		Point[] pTemp = new Point[1000];
		int posIdx = 0;

		// make a copy of the current positions in p
		for (int i = 0; i < 1000; i += 1) {
			pTemp[i] = p.getPosition(i);
		}

		p.clearPositions();

		// rearrange positions
		for (int i = 0; i < p.getNumOfInst(); i += 1) {
			Instruction instr = p.get(i);

			if (instr instanceof PosMotionInst) {
				// Update the primary position
				PosMotionInst mInst = ((PosMotionInst) instr);
				int oldPosNum = mInst.getPosIdx();
				
				if (oldPosNum >= 0 && oldPosNum < pTemp.length) {
					p.setPosition(posIdx, pTemp[oldPosNum]);
					mInst.setPosIdx(posIdx++);
				}

				if (mInst.getMotionType() == Fields.MTYPE_CIRCULAR) {

					/*
					 * Update position for secondary point of a circular
					 * motion instruction
					 */
					oldPosNum = mInst.getCircPosIdx();
					
					if (oldPosNum >= 0 && oldPosNum < pTemp.length) {
						p.setPosition(posIdx, pTemp[oldPosNum]);
						mInst.setCircPosIdx(posIdx++);
					}
				}
			}
		}

		robotRun.popScreenStack(1);
		robotRun.updateInstructions();
	}
	
	@Override
	public void actionF5() {
		robotRun.popScreenStack(1);
		robotRun.updateInstructions();
	}
}