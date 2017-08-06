package screen.edit_item;

import core.RobotRun;
import expression.Expression;
import expression.Operand;
import expression.OperandBool;
import expression.OperandCamObj;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandIOReg;
import expression.OperandPReg;
import expression.OperandPRegIdx;
import expression.Operator;
import expression.RobotPoint;
import programming.ExpressionEvaluation;
import programming.IfStatement;
import programming.Instruction;
import programming.RegisterStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.ScreenMode;

public class ScreenSetExpressionArg extends ST_ScreenEditItem {

	public ScreenSetExpressionArg(RobotRun r) {
		super(ScreenMode.SET_EXPR_ARG, r);
	}

	@Override
	public void actionEntr() {
		Expression expr = (Expression)robotRun.opEdit;
		Operand<?> operand;
		
		if(options.getCurrentItemIdx() == 0) {
			// set arg to new data reg
			operand = new OperandDReg(new DataRegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_DREG_IDX);
		} else if(options.getCurrentItemIdx()== 1) {
			// set arg to new preg idx
			operand = new OperandPRegIdx(new PositionRegister(), 0);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_PREG_IDX2);
			robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX1);
		} else if(options.getCurrentItemIdx() == 2) {
			// set arg to new constant
			operand = new OperandFloat();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_CONST);
		} else if(options.getCurrentItemIdx() == 3) {
			// set arg to new io reg
			operand = new OperandIOReg(new IORegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_IOREG_IDX);
		} else if(options.getCurrentItemIdx() == 4) {
			operand = new OperandCamObj();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.SET_OBJ_OPERAND_TGT);
		} else if(options.getCurrentItemIdx() == 5) {
			operand = new OperandBool();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.SET_BOOL_CONST);
		} else if(options.getCurrentItemIdx() == 6) {
			// set arg to new preg
			operand = new OperandPReg(new PositionRegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_PREG_IDX1);
		} else if(options.getCurrentItemIdx() == 7) {	
			// JPos operand
			operand = new RobotPoint(robotRun.getActiveRobot(), false);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
		} else if(options.getCurrentItemIdx() == 8) {
			// LPos operand
			operand = new RobotPoint(robotRun.getActiveRobot(), true);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
		} else if(options.getCurrentItemIdx() == 9) {
			// set arg to new expression
			operand = new Expression();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
		}
	}
	
	@Override
	protected void loadOptions() {
		if (robotRun.opEdit instanceof Expression) {
			Instruction instr = robotRun.getActiveInstruction();
			Expression expr = (Expression)robotRun.opEdit;
			int idx = contents.getItemColumnIdx() - ((ExpressionEvaluation)instr).getHeaderLength();
			int[] elements = expr.mapToEdit();
			Operator prev;
			
			if(idx > 0 && idx < elements.length && expr.get(elements[idx - 1]) instanceof Operator) {
				prev = (Operator)expr.get(elements[idx - 1]);
			} else {
				prev = null;
			}
			
			if(instr instanceof RegisterStatement) {
				RegisterStatement r = (RegisterStatement)instr;
				
				if(r.getReg() instanceof DataRegister) {
					loadFloatArgs();
				} else if(r.getReg() instanceof IORegister) {
					if(prev == null || prev.getType() == Operator.NO_OP) {
						loadFloatArgs();
						loadBoolArgs();
					} else if(prev.getType() == Operator.ARITH_OP || prev.getType() == Operator.BOOL_OP) {
						loadFloatArgs();
					} else if(prev.getType() == Operator.LOGIC_OP) {
						loadBoolArgs();
					}
				} 
				else if(r.getReg() instanceof PositionRegister) {
					if(r.getPosIdx() == -1) {
						loadPointArgs();						
					} else {
						loadFloatArgs();
					}
				}
			}
			else if(instr instanceof IfStatement) {
				if(prev == null || prev.getType() == Operator.NO_OP) {
					loadFloatArgs();
					loadBoolArgs();
				} else if(prev.getType() == Operator.ARITH_OP || prev.getType() == Operator.BOOL_OP) {
					loadFloatArgs();
				} else if(prev.getType() == Operator.LOGIC_OP) {
					loadBoolArgs();
				}
			}
			
			options.addLine(9, "(...)");
		}
	}
	
	private void loadBoolArgs() {
		options.addLine(3, "IO[x]");
		options.addLine(4, "Obj[x]");
		options.addLine(5, "BoolConst");
	}
	
	private void loadFloatArgs() {
		options.addLine(0, "R[x]");
		options.addLine(1, "PR[x, y]");
		options.addLine(2, "FConst");
	}

	private void loadPointArgs() {
		options.addLine(6, "PR[x]");
		options.addLine(7, "JPos");
		options.addLine(8, "LPos");
	}
}
