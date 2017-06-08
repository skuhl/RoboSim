package programming;

import expression.Expression;
import expression.Operand;
import expression.OperandBool;
import expression.OperandFloat;
import expression.OperandPoint;
import geom.Point;
import global.Fields;
import global.RMath;
import processing.core.PVector;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;

public class RegisterStatement extends Instruction {

	private Register reg;  //the register to be modified by this instruction
	private int posIdx;  //used if editing a single value in a position register
	private Expression expr;  //the expression whose value will be stored in 'reg' after evaluation

	/**
	 * Creates a register statement with a given register and a blank Expression.
	 *
	 * @param reg - The destination register for this register expression. The
	 *              result of the expression 'expr' will be assigned to 'reg'
	 *              upon successful execution of this statement.
	 * @param expr - The expression to be evaluated in conjunction with the execution
	 *               of this statement. The value of this expression, if valid for the
	 *               register 
	 */
	public RegisterStatement(Register r) {
		reg = r;
		posIdx = -1;
		expr = new Expression();
	}

	public RegisterStatement(Register r, Expression e) {
		reg = r;
		posIdx = -1;
		expr = e;
	}

	public RegisterStatement(Register r, int i) {
		reg = r;
		posIdx = i;
		expr = new Expression();
	}

	public RegisterStatement(Register r, int idx, Expression e) {
		reg = r;
		posIdx = idx;
		expr = e;
	}
	
	@Override
	public Instruction clone() {
		Instruction copy = new RegisterStatement(reg, posIdx, expr.clone());    
		return copy;
	}
	
	@Override
	public int execute() {
		Operand<?> result = expr.evaluate();
		
		if(result instanceof OperandFloat) {
			float fl = ((OperandFloat)result).getArithValue();
			if(reg instanceof DataRegister) {
				((DataRegister)reg).value = fl;
				return 1;
			}
			else if(reg instanceof PositionRegister) {
				PositionRegister pReg = (PositionRegister)reg;
				PVector wPos = RMath.vToWorld(pReg.point.position);
				PVector wpr = RMath.nQuatToWEuler(pReg.point.orientation);
				// Update position value
				
				switch(posIdx) {
				case 0:	wPos.x = fl; break;
				case 1: wPos.y = fl; break;
				case 2: wPos.z = fl; break;
				case 3: wpr.x = fl; break;
				case 4: wpr.y = fl; break;
				case 5: wpr.z = fl; break;
				default: return 0;
				}
				
				pReg.point.position = RMath.vFromWorld(wPos);
				pReg.point.orientation = RMath.wEulerToNQuat(wpr);
				return 1;
			}
		}
		else if(result instanceof OperandBool) {
			boolean b = ((OperandBool)result).getBoolValue();
			if(reg instanceof IORegister) {
				((IORegister)reg).state = b ? Fields.ON : Fields.OFF;
				return 1;
			}
		}
		else if(result instanceof OperandPoint) {
			Point p = ((OperandPoint)result).getPointValue();
			if(reg instanceof PositionRegister) {
				((PositionRegister)reg).point = p;
				return 1;
			}
		}

		return 0;
	}
	
	public Expression getExpr() {
		return expr;
	}
	
	public int getPosIdx() { 
		return posIdx; 
	}

	public Register getReg() { 
		return reg; 
	}

	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	public void setPosIdx(int posIdx) {
		this.posIdx = posIdx;
	}

	public Register setRegister(Register r) {
		reg = r;
		posIdx = -1;
		return reg;
	}

	public Register setRegister(Register r, int idx) {
		reg = r;
		posIdx = idx;
		return reg;
	}

	/**
	 * Convert the entire statement to a set of Strings, where each
	 * operator and operand is a separate String Object.
	 */
	@Override
	public String[] toStringArray() {
		String[] ret;
		String[] exprString = expr.toStringArray();
		String rString = "";
		int rLen;

		if(reg instanceof DataRegister) { rString  = "R["; }
		else if(reg instanceof IORegister) { rString  = "IO["; }
		else if(reg instanceof PositionRegister) { rString  = "PR["; }
		else { rString = "["; }

		if(posIdx == -1) {
			ret = new String[2 + expr.getLength()];

			ret[0] = rString;
			ret[1] = (reg == null || reg.idx == -1) ? "...] =" : (reg.idx + 1) + "] =";
			rLen = 2;
		} else {
			ret = new String[3 + expr.getLength()];

			ret[0] = rString;
			ret[1] = (reg == null || reg.idx == -1) ? "...," : (reg.idx + 1) + ",";
			ret[2] = (reg == null || reg.idx == -1) ? "...] =" : (posIdx + 1) + "] =";
			rLen = 3;
		}

		for(int i = 0; i < exprString.length; i += 1) {
			ret[i + rLen] = exprString[i];
		}

		return ret;
	}
}