package screen.instr_edit;

import core.RobotRun;
import enums.ScreenMode;
import expression.AtomicExpression;
import expression.Expression;
import expression.Operator;
import programming.IfStatement;

public class ScreenSetExpressionOp extends ST_ScreenInstructionEdit {

	public ScreenSetExpressionOp(RobotRun r) {
		super(ScreenMode.SET_EXPR_OP, r);
	}

	@Override
	protected void loadOptions() {
		if (robotRun.opEdit instanceof Expression) {
			if (robotRun.getActiveInstruction() instanceof IfStatement) {
				options.addLine("1. + ");
				options.addLine("2. - ");
				options.addLine("3. * ");
				options.addLine("4. / (Division)");
				options.addLine("5. | (Integer Division)");
				options.addLine("6. % (Modulus)");
				options.addLine("7. = ");
				options.addLine("8. <> (Not Equal)");
				options.addLine("9. > ");
				options.addLine("10. < ");
				options.addLine("11. >= ");
				options.addLine("12. <= ");
				options.addLine("13. AND ");
				options.addLine("14. OR ");
				options.addLine("15. NOT ");
			} else {
				options.addLine("1. + ");
				options.addLine("2. - ");
				options.addLine("3. * ");
				options.addLine("4. / (Division)");
				options.addLine("5. | (Integer Division)");
				options.addLine("6. % (Modulus)");
			}
		} else {
			options.addLine("1. ... =  ...");
			options.addLine("2. ... <> ...");
			options.addLine("3. ... >  ...");
			options.addLine("4. ... <  ...");
			options.addLine("5. ... >= ...");
			options.addLine("6. ... <= ...");
		}
	}

	@Override
	public void actionEntr() {
		if (robotRun.opEdit instanceof Expression) {
			Expression expr = (Expression)robotRun.opEdit;
			
			switch (options.getLineIdx()) {
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
