package screen.edit_item;

import core.RobotRun;
import expression.AtomicExpression;
import expression.BoolMath;
import expression.Expression;
import expression.ExpressionElement;
import expression.FloatMath;
import expression.OperandGeneric;
import expression.Operator;
import expression.PointMath;
import programming.ExpressionEvaluation;
import programming.IfStatement;
import programming.Instruction;
import programming.RegisterStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.ScreenMode;

public class ScreenSetExpressionOp extends ST_ScreenEditItem {
	
	public ScreenSetExpressionOp(RobotRun r) {
		super(ScreenMode.SET_EXPR_OP, r);
	}

	@Override
	protected void loadOptions() {
		if (robotRun.opEdit instanceof Expression) {
			Instruction instr = robotRun.getActiveInstruction();
			Expression expr = (Expression)robotRun.opEdit;
			int idx = contents.getItemColumnIdx() - ((ExpressionEvaluation)instr).getHeaderLength();
			int[] elements = expr.mapToEdit();
			ExpressionElement prev;
			
			if(idx > 0 && idx < elements.length) {
				prev = expr.get(elements[idx - 1]);
				
				if(prev instanceof Expression) {
					prev = ((Expression)prev).evaluate();
				}
			} else {
				prev = null;
			}
			
			if(instr instanceof RegisterStatement) {
				RegisterStatement r = (RegisterStatement)instr;
				
				if(r.getReg() instanceof DataRegister) {
					loadArithOps();
				} else if(r.getReg() instanceof IORegister) {
					if(prev == null || prev instanceof PointMath || prev instanceof OperandGeneric) {
						loadArithOps();
						loadBoolOps();
						loadLogicOps();
					} else if(prev instanceof FloatMath) {
						loadArithOps();
						loadBoolOps();
					} else if(prev instanceof BoolMath) {
						loadLogicOps();
					}
				} 
				else if(r.getReg() instanceof PositionRegister) {
					if(r.getPosIdx() == -1) {
						loadPointOps();
					} else {
						loadArithOps();
					}
				}
			}
			else if(instr instanceof IfStatement) {
				if(prev == null || prev instanceof PointMath || prev instanceof OperandGeneric) {
					loadArithOps();
					loadBoolOps();
					loadLogicOps();
				} else if(prev instanceof FloatMath) {
					loadArithOps();
					loadBoolOps();
				} else if(prev instanceof BoolMath) {
					loadLogicOps();
				}
			}
		} else if(robotRun.opEdit instanceof AtomicExpression) {
			options.addLine("1. ... =  ...");
			options.addLine("2. ... <> ...");
			options.addLine("3. ... >  ...");
			options.addLine("4. ... <  ...");
			options.addLine("5. ... >= ...");
			options.addLine("6. ... <= ...");
		}
	}
	
	private void loadArithOps() {
		int idx = (options.size() + 1);
		
		options.addLine(0, idx++ + ". + ");
		options.addLine(1, idx++ + ". - ");
		options.addLine(2, idx++ + ". * ");
		options.addLine(3, idx++ + ". / (Division)");
		options.addLine(4, idx++ + ". | (Integer Division)");
		options.addLine(5, idx++ + ". % (Modulus)");
	}
	
	private void loadBoolOps() {
		int idx = (options.size() + 1);
		
		options.addLine(6, idx++ + ". = ");
		options.addLine(7, idx++ + ". <> (Not Equal)");
		options.addLine(8, idx++ + ". > ");
		options.addLine(9, idx++ + ". < ");
		options.addLine(10, idx++ + ". >= ");
		options.addLine(11, idx++ + ". <= ");
	}
	
	private void loadLogicOps() {
		int idx = (options.size() + 1);
		
		options.addLine(12, idx++ + ". AND ");
		options.addLine(13, idx++ + ". OR ");
		options.addLine(14, idx++ + ". NOT ");
	}
		
	private void loadPointOps() {
		int idx = (options.size() + 1);
		
		options.addLine(15, idx++ + ". + ");
		options.addLine(16, idx++ + ". - ");
	}

	@Override
	public void actionEntr() {
		if (robotRun.opEdit instanceof Expression) {
			Expression expr = (Expression)robotRun.opEdit;
			
			switch (options.getCurrentItemIdx()) {
			case 0: expr.setOperator(robotRun.editIdx, Operator.ADD); break;
			case 1: expr.setOperator(robotRun.editIdx, Operator.SUB); break;
			case 2: expr.setOperator(robotRun.editIdx, Operator.MULT); break;
			case 3: expr.setOperator(robotRun.editIdx, Operator.DIV); break;
			case 4: expr.setOperator(robotRun.editIdx, Operator.IDIV); break;
			case 5: expr.setOperator(robotRun.editIdx, Operator.MOD); break;
			case 6: expr.setOperator(robotRun.editIdx, Operator.EQUAL); break;
			case 7: expr.setOperator(robotRun.editIdx, Operator.NEQUAL); break;
			case 8: expr.setOperator(robotRun.editIdx, Operator.GRTR); break;
			case 9: expr.setOperator(robotRun.editIdx, Operator.LESS); break;
			case 10: expr.setOperator(robotRun.editIdx, Operator.GREQ); break;
			case 11: expr.setOperator(robotRun.editIdx, Operator.LSEQ); break;
			case 12: expr.setOperator(robotRun.editIdx, Operator.AND); break;
			case 13: expr.setOperator(robotRun.editIdx, Operator.OR); break;
			case 14: expr.setOperator(robotRun.editIdx, Operator.NOT); break;
			case 15: expr.setOperator(robotRun.editIdx, Operator.PT_ADD); break;
			case 16: expr.setOperator(robotRun.editIdx, Operator.PT_SUB); break;
			}
		}
		else if (robotRun.opEdit instanceof AtomicExpression) {
			AtomicExpression atmExpr = (AtomicExpression)robotRun.opEdit;

			switch (options.getLineIdx()) {
			case 0: atmExpr.setOperator(Operator.EQUAL); break;
			case 1: atmExpr.setOperator(Operator.NEQUAL); break;
			case 2: atmExpr.setOperator(Operator.GRTR); break;
			case 3:	atmExpr.setOperator(Operator.LESS); break;
			case 4: atmExpr.setOperator(Operator.GREQ); break;
			case 5: atmExpr.setOperator(Operator.LSEQ); break;
			}
		}
		
		robotRun.lastScreen();
	}
}
