package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import enums.ScreenMode;
import expression.Expression;
import geom.Point;
import global.Fields;
import programming.IfStatement;
import programming.Instruction;
import programming.MotionInstruction;
import programming.PosMotionInst;
import programming.Program;
import programming.RegisterStatement;
import programming.SelectStatement;
import regs.PositionRegister;
import robot.RoboticArm;

public class ScreenNavProgInstructions extends ST_ScreenListContents {

	public ScreenNavProgInstructions(RobotRun r) {
		super(ScreenMode.NAV_PROG_INSTR, r);
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}

	@Override
	protected void loadOptions() {
		Instruction inst = robotRun.getActiveInstruction();
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		int selectedReg = robotRun.selectedMInstRegState();
		
		if (inst instanceof PosMotionInst && selectedReg > 0) {
			// Show the position associated with the active motion
			// instruction
			PosMotionInst mInst = (PosMotionInst) inst;
			boolean isCart = false;
			Point pt = null;
			
			if (selectedReg == 6) {
				PositionRegister pReg = r.getPReg(mInst.getOffsetIdx());
				
				if (pReg != null) {
					isCart = pReg.isCartesian;
					pt = pReg.point;
				}
				
			} else if (selectedReg == 4 || selectedReg == 3) {
				isCart = true;
				pt = r.getCPosition(mInst, p);
				
			} else if (selectedReg == 1 || selectedReg == 2) {
				isCart = mInst.getMotionType() != Fields.MTYPE_JOINT;
				pt = r.getPosition(mInst, p);
			}

			if (pt != null) {
				String[] pregEntry = pt.toLineStringArray(isCart);

				for (String line : pregEntry) {
					options.addLine(line);
				}
				
			} else {
				options.addLine("Uninitialized");
			}
		}
	}

	@Override
	protected void loadLabels() {
		Instruction inst = robotRun.getActiveInstruction();

		// F1, F4, F5f
		labels[0] = "[New Pt]";
		labels[1] = "[New Ins]";
		labels[2] = "";
		labels[3] = "[Edit]";
		labels[4] = (contents.getColumnIdx() == 0) ? "[Opt]" : "";
					
		if (inst instanceof MotionInstruction) {
			labels[2] = "[Ovr Pt]";
			
			int regState = robotRun.selectedMInstRegState();
			/* Only display edit labelsion for a motion instruction's
			 * primary position referencing a position */
			if (regState == 1) {
				labels[4] = "[Reg]";
			}
		} 
		else if (inst instanceof IfStatement) {
			IfStatement stmt = (IfStatement) inst;
			int selectIdx = contents.getItemColumnIdx();

			if (stmt.getExpr() instanceof Expression) {
				if (selectIdx > 1 && selectIdx < stmt.getExpr().getLength() + 1) {
					labels[2] = "[Insert]";
				}
				if (selectIdx > 2 && selectIdx < stmt.getExpr().getLength() + 1) {
					labels[4] = "[Delete]";
				}
			}
		} 
		else if (inst instanceof SelectStatement) {
			int selectIdx = contents.getItemColumnIdx();

			if (selectIdx >= 3) {
				labels[2] = "[Insert]";
				labels[4] = "[Delete]";
			}
		} 
		else if (inst instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement) inst;
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
			int selectIdx = contents.getItemColumnIdx();

			if (selectIdx > rLen && selectIdx < stmt.getExpr().getLength() + rLen) {
				labels[2] = "[Insert]";
			}
			if (selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
				labels[4] = "[Delete]";
			}
		}
	}
	
	public void actionFwd() {
		contents.setSelectedColumnIdx(0);
	}
	
	public void actionBwd() {
		contents.setSelectedColumnIdx(0);
	}
	
	public void actionItem() {
		robotRun.nextScreen(ScreenMode.JUMP_TO_LINE);
	}
	
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
	public void actionLt() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			contents.moveLeft();
		}
	}
	
	public void actionRt() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			contents.moveRight();
		}
	}

	@Override
	public void actionEntr() {}

	@Override
	public void actionF1() {
		if (robotRun.isShift()) {
			robotRun.newPosMotionInst();
			contents.setSelectedColumnIdx(0);

			if (contents.getItemLineIdx() == 0) {
				contents.setSelectedLineIdx(contents.getLineIdx() + 1);
				robotRun.updatePendantScreen();
				if (contents.getItemLineIdx() == 0) {
					robotRun.setActiveInstIdx(robotRun.getActiveInstIdx() + 1);
				}
			}
		}
	}

	@Override
	public void actionF2() {
		robotRun.nextScreen(ScreenMode.SELECT_INSTR_INSERT);
	}

	@Override
	public void actionF3() {
		RoboticArm r = robotRun.getActiveRobot();
		Instruction inst = robotRun.getActiveInstruction();
		int selectIdx = contents.getItemColumnIdx();

		if (inst instanceof PosMotionInst) {
			r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
			
			Point pt = r.getToolTipUser();
			Program p = robotRun.getActiveProg();
			int actInst = robotRun.getActiveInstIdx();

			if (contents.getItemLineIdx() == 1) {
				// Update the secondary position in a circular motion
				// instruction
				r.updateMCInstPosition(p, actInst, pt);

			} else {
				// Update the position associated with the active motion
				// instruction
				r.updateMInstPosition(p, actInst, pt);
			}

			PosMotionInst mInst = (PosMotionInst) inst;

			// Update the motion instruction's fields
			CoordFrame coord = r.getCurCoordFrame();

			if (coord == CoordFrame.JOINT) {
				mInst.setMotionType(Fields.MTYPE_JOINT);

			} else {
				/*
				 * Keep circular motion instructions as circular motion
				 * instructions in world, tool, or user frame modes
				 */
				if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
					mInst.setMotionType(Fields.MTYPE_LINEAR);
				}					
			}
			
			mInst.setSpdMod(0.5f);
			mInst.setTFrameIdx(r.getActiveToolIdx());
			mInst.setUFrameIdx(r.getActiveUserIdx());

		} else if (inst instanceof IfStatement) {
			IfStatement stmt = (IfStatement) inst;

			if (stmt.getExpr() instanceof Expression && selectIdx >= 2) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((Expression) stmt.getExpr()).insertElement(selectIdx - 3);
				robotRun.updatePendantScreen();
				actionRt();
			}
		} else if (inst instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement) inst;

			if (selectIdx >= 3) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				stmt.addCase();
				robotRun.updatePendantScreen();
				actionDn();
			}
		} else if (inst instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement) inst;
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

			if (selectIdx > rLen) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				stmt.getExpr().insertElement(selectIdx - (rLen + 2));
				robotRun.updatePendantScreen();
				actionRt();
			}
		}

		robotRun.updatePendantScreen();
	}

	@Override
	public void actionF4() {
		Instruction ins = robotRun.getActiveInstruction();

		if (ins != null) {
			int selectIdx = contents.getItemColumnIdx();
			robotRun.getEditScreen(ins, selectIdx);
		}
	}
	
	@Override
	public void actionF5() {
		RoboticArm r = robotRun.getActiveRobot();
		Instruction inst = robotRun.getActiveInstruction();
		int selectIdx = contents.getItemColumnIdx();	

		if (selectIdx == 0) {
			robotRun.nextScreen(ScreenMode.NAV_INSTR_MENU);
			
		} else if (inst instanceof MotionInstruction) {
			int regState = robotRun.selectedMInstRegState();
			
			if (regState == 1) {
				// Only allow editing of primary position of motion instruction
				robotRun.nextScreen(ScreenMode.EDIT_PROG_POS);	
			}
			
		} else if (inst instanceof IfStatement) {
			IfStatement stmt = (IfStatement) inst;
			if (stmt.getExpr() instanceof Expression) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				((Expression) stmt.getExpr()).removeElement(selectIdx - 3);
			}
		} else if (inst instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement) inst;
			if (selectIdx >= 3) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				stmt.deleteCase((selectIdx - 3) / 3);
			}
		} else if (inst instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement) inst;
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
			
			if (selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
				r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx());
				stmt.getExpr().removeElement(selectIdx - (rLen + 2));
			}
		}
	}
}
