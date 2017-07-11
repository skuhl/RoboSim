package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import expression.AtomicExpression;
import expression.Expression;
import expression.ExpressionElement;
import expression.Operand;
import expression.RobotPoint;
import geom.Point;
import global.Fields;
import programming.CallInstruction;
import programming.CamMoveToObject;
import programming.FrameInstruction;
import programming.IOInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.LabelInstruction;
import programming.MotionInstruction;
import programming.PosMotionInst;
import programming.Program;
import programming.RegisterStatement;
import programming.SelectStatement;
import regs.PositionRegister;
import robot.RoboticArm;
import screen.ScreenMode;

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
		contents.setLines(loadInstructions(robotRun.getActiveProg(), true));
		robotRun.setActiveInstIdx(contents.getCurrentItemIdx());
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
		contents.setColumnIdx(0);
	}
	
	public void actionBwd() {
		contents.setColumnIdx(0);
	}
	
	public void actionItem() {
		robotRun.nextScreen(ScreenMode.JUMP_TO_LINE);
	}
	
	@Override
	public void actionUp() {
		if (!robotRun.isProgExec()) {
			// Lock movement when a program is running
			Instruction i = robotRun.getActiveInstruction();
			int prevLine = contents.getItemLineIdx();
			contents.moveUp(robotRun.isShift());
			int curLine = contents.getItemLineIdx();

			// special case for select statement column navigation
			if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
				if (prevLine == 1) {
					contents.setColumnIdx(contents.getColumnIdx() + 3);
				}
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
			contents.moveDown(robotRun.isShift());
			int curLine = contents.getItemLineIdx();

			// special case for select statement column navigation
			if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
				if (prevIdx >= 3) {
					contents.setColumnIdx(prevIdx - 3);
				} else {
					contents.setColumnIdx(0);
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
			contents.setColumnIdx(0);

			if (contents.getItemLineIdx() == 0) {
				contents.setLineIdx(contents.getLineIdx() + 1);
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
			getEditScreen(ins);
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
	
	private void getEditScreen(Instruction ins) {
		int sdx = contents.getItemColumnIdx();
		
		if (ins instanceof MotionInstruction) {
			MotionInstruction mInst = (MotionInstruction)ins;
			
			if(sdx == 2) {
				// Motion type
				robotRun.nextScreen(ScreenMode.SET_MINST_TYPE);
			} 
			else if(sdx == 3) {
				// Position type
				robotRun.nextScreen(ScreenMode.SET_MINST_REG_TYPE);	
			} 
			else if(sdx == 4) {
				
				if (mInst instanceof CamMoveToObject) {
					CamMoveToObject cMInst = (CamMoveToObject)mInst;
					
					if (cMInst.getScene() == null) {
						cMInst.setScene(robotRun.getActiveScenario());
					}
					
					// Set World Object reference
					robotRun.nextScreen(ScreenMode.SET_MINST_OBJ);
				} 
				else {
					// Position index
					robotRun.nextScreen(ScreenMode.SET_MINST_IDX);
				}
				
			} else if (sdx == 5) {
				// Speed modifier
				robotRun.nextScreen(ScreenMode.SET_MINST_SPD);
				
			} else if (sdx == 6) {
				// Termination
				robotRun.nextScreen(ScreenMode.SET_MINST_TERM);
				
			} else if (sdx == 7) {
				// Offset type
				robotRun.nextScreen(ScreenMode.SET_MINST_OFF_TYPE);
				
			} else if (mInst instanceof PosMotionInst) {
				PosMotionInst pMInst = (PosMotionInst)mInst;
				
				if (pMInst.getOffsetType() == Fields.OFFSET_PREG) {
					if (sdx == 8) {
						// Offset index
						robotRun.nextScreen(ScreenMode.SET_MINST_OFFIDX);
					} 
					else if (sdx == 9) {
						// Circular position type
						robotRun.nextScreen(ScreenMode.SET_MINST_CREG_TYPE);
					} 
					else if (sdx == 10) {
						// Circular position index
						robotRun.nextScreen(ScreenMode.SET_MINST_CIDX);
					}
				} 
				else if (pMInst.getOffsetType() == Fields.OFFSET_NONE) {
					if (sdx == 8) {
						// Circular position type
						robotRun.nextScreen(ScreenMode.SET_MINST_CREG_TYPE);
					} 
					else if (sdx == 9) {
						// Circular position index
						robotRun.nextScreen(ScreenMode.SET_MINST_CIDX);
					}
				}
			}
		} 
		else if (ins instanceof FrameInstruction) {
			switch (sdx) {
			case 1:
				robotRun.nextScreen(ScreenMode.SET_FRAME_INSTR_TYPE);
				break;
			case 2:
				robotRun.nextScreen(ScreenMode.SET_FRAME_INSTR_IDX);
				break;
			}
		} 
		else if (ins instanceof IOInstruction) {
			switch (sdx) {
			case 1:
				robotRun.nextScreen(ScreenMode.SET_IO_INSTR_IDX);
				break;
			case 2:
				robotRun.nextScreen(ScreenMode.SET_IO_INSTR_STATE);
				break;
			}
		} 
		else if (ins instanceof LabelInstruction) {
			robotRun.nextScreen(ScreenMode.SET_LBL_NUM);
		} 
		else if (ins instanceof JumpInstruction) {
			robotRun.nextScreen(ScreenMode.SET_JUMP_TGT);
		} 
		else if (ins instanceof CallInstruction) {
			if (((CallInstruction) ins).getTgtDevice() != null) {
				robotRun.editIdx = ((CallInstruction) ins).getTgtDevice().RID;

			} else {
				robotRun.editIdx = -1;
			}

			robotRun.nextScreen(ScreenMode.SET_CALL_PROG);
		} else if (ins instanceof IfStatement) {
			IfStatement stmt = (IfStatement) ins;
			
			if (stmt.getExpr() instanceof Expression) {
				int len = stmt.getExpr().getLength();

				if (sdx >= 3 && sdx < len + 1) {
					editExpression((Expression) stmt.getExpr(), sdx - 3);
				} else if (sdx == len + 2) {
					robotRun.nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else if (sdx == len + 3) {
					if (stmt.getInstr() instanceof JumpInstruction) {
						robotRun.nextScreen(ScreenMode.SET_JUMP_TGT);
					} else if (stmt.getInstr() instanceof CallInstruction) {
						robotRun.nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			} else if (stmt.getExpr() instanceof AtomicExpression) {
				if (sdx == 2) {
					robotRun.opEdit = stmt.getExpr().getArg1();
					robotRun.editIdx = 0;
					robotRun.nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (sdx == 3) {
					robotRun.opEdit = stmt.getExpr();
					robotRun.nextScreen(ScreenMode.SET_EXPR_OP);
				} else if (sdx == 4) {
					robotRun.opEdit = stmt.getExpr().getArg2();
					robotRun.editIdx = 2;
					robotRun.nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (sdx == 5) {
					robotRun.nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else if (sdx == 6) {
					if (stmt.getInstr() instanceof JumpInstruction) {
						robotRun.nextScreen(ScreenMode.SET_JUMP_TGT);
					} else if (stmt.getInstr() instanceof CallInstruction) {
						robotRun.nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			}
		} else if (ins instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement) ins;
			robotRun.editIdx = (sdx - 3) / 3;
			
			if (sdx == 2) {
				robotRun.opEdit = stmt.getArg();
				robotRun.editIdx = -1;
				robotRun.nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((sdx - 3) % 3 == 0 && sdx > 2) {
				robotRun.opEdit = stmt.getCases().get((sdx - 3) / 3);
				robotRun.nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((sdx - 3) % 3 == 1) {
				robotRun.nextScreen(ScreenMode.SET_SELECT_STMT_ACT);
			} else if ((sdx - 3) % 3 == 2) {
				Instruction toExec = stmt.getInstrs().get(robotRun.editIdx);
				if (toExec instanceof JumpInstruction) {
					robotRun.nextScreen(ScreenMode.SET_JUMP_TGT);
				} else if (toExec instanceof CallInstruction) {
					robotRun.nextScreen(ScreenMode.SET_CALL_PROG);
				}
			}
		} else if (ins instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement) ins;
			int len = stmt.getExpr().getLength();
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
			
			if (sdx == 1) {
				robotRun.nextScreen(ScreenMode.SET_REG_EXPR_TYPE);
			} else if (sdx == 2) {
				robotRun.nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			} else if (sdx == 3 && stmt.getPosIdx() != -1) {
				robotRun.nextScreen(ScreenMode.SET_REG_EXPR_IDX2);
			} else if (sdx >= rLen + 1 && sdx <= len + rLen) {
				editExpression(stmt.getExpr(), sdx - (rLen + 2));
			}
		}
	}
	
	private void editExpression(Expression expr, int selectIdx) {
		int[] elements = expr.mapToEdit();
		
		try {
			robotRun.opEdit = expr;
			ExpressionElement e = expr.get(elements[selectIdx]);
	
			if (e instanceof Expression) {
				// if selecting the open or close paren
				if (selectIdx == 0 || selectIdx == e.getLength() || elements[selectIdx - 1] != elements[selectIdx]
						|| elements[selectIdx + 1] != elements[selectIdx]) {
					robotRun.nextScreen(ScreenMode.SET_EXPR_ARG);
				} else {
					int startIdx = expr.getStartingIdx(elements[selectIdx]);
					editExpression((Expression) e, selectIdx - startIdx - 1);
				}
			} else if (e instanceof Operand) {
				editOperand((Operand<?>) e, selectIdx, elements[selectIdx]);
			} else {
				robotRun.editIdx = elements[selectIdx];
				robotRun.nextScreen(ScreenMode.SET_EXPR_OP);
			}
			
		} catch (ArrayIndexOutOfBoundsException AIOOBEx) {
			Fields.setMessage("Invalid expression index: %d!\n", selectIdx);
		}
	}
	
	/**
	 * Accepts an ExpressionOperand object and forwards the UI to the
	 * appropriate menu to edit said object based on the operand type.
	 *
	 * @param o
	 *            - The operand to be edited.
	 * @ins_idx - The index of the operand's container ExpressionElement list
	 *          into which this operand is stored.
	 *
	 */
	private void editOperand(Operand<?> o, int selectIdx, int startIdx) {
		robotRun.editIdx = startIdx;
		
		switch (o.getType()) {
		case Operand.UNINIT: // Uninit
			robotRun.nextScreen(ScreenMode.SET_EXPR_ARG);
			break;
		case Operand.FLOAT: // Float const
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.INPUT_CONST);
			break;
		case Operand.BOOL: // Bool const
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.SET_BOOL_CONST);
			break;
		case Operand.CAM_MATCH:
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.SET_OBJ_OPERAND_TGT);
		case Operand.DREG: // Data reg
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.INPUT_DREG_IDX);
			break;
		case Operand.IOREG: // IO reg
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.INPUT_IOREG_IDX);
			break;
		case Operand.PREG: // Pos reg
			robotRun.opEdit = o;
			robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		case Operand.PREG_IDX: // Pos reg at index
			robotRun.opEdit = o;
			if(selectIdx == startIdx)
				robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX1);
			else
				robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX2);
			break;
		case Operand.ROBOT: // Robot point
			RobotPoint rp = (RobotPoint)o;
			rp.setType(!rp.isCartesian());
			break;
		}
	}
}
