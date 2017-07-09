package programming;

import expression.Expression;
import expression.Operand;
import expression.OperandBool;
import expression.OperandFloat;
import expression.OperandPoint;
import expression.Operator;
import geom.Point;
import global.Fields;
import global.RMath;
import processing.core.PConstants;
import processing.core.PVector;
import regs.DataRegister;
import regs.IORegTrace;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;

public class RegisterStatement extends Instruction implements ExpressionEvaluation {

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
	
	/**
	 * Evaluates the expression associated with this register statement.
	 * 
	 * @return	0	the expressions evaluation is successful,
	 * 			1	an error occurs when executing the register statement
	 */
	public int evalExpression() {
		Operand<?> result = expr.evaluate();
		
		if(result instanceof OperandFloat) {
			float fl = ((OperandFloat)result).getArithValue();
			
			if(reg instanceof DataRegister) {
				// Update a data register value
				((DataRegister)reg).value = fl;
				return 0;
			}
			else if(reg instanceof PositionRegister) {
				// Update a value of a position register's point
				PositionRegister pReg = (PositionRegister)reg;
				Point pt = pReg.point;
				
				if (posIdx >= 0 && posIdx < 6) {
					if (pReg.isCartesian) {
						// Update Cartesian value
						PVector wPos = RMath.vToWorld(pt.position);
						PVector wpr = RMath.nQuatToWEuler(pt.orientation);
						
						switch(posIdx) {
						case 0:	wPos.x = fl; break;
						case 1: wPos.y = fl; break;
						case 2: wPos.z = fl; break;
						case 3: wpr.x = fl; break;
						case 4: wpr.y = fl; break;
						case 5: wpr.z = fl; break;
						}
						
						pt.position = RMath.vFromWorld(wPos);
						pt.orientation = RMath.wEulerToNQuat(wpr);
						
					} else {
						// Update a joint angle value
						pt.angles[posIdx] = RMath.mod2PI(fl * PConstants.DEG_TO_RAD);
					}
					
				} else {
					// Invalid position index
					return 1;
				}
				
				return 0;
			}
		}
		else if(result instanceof OperandBool) {
			// Update an I/O register
			boolean b = ((OperandBool)result).getBoolValue();
			if(reg instanceof IORegTrace) {
				((IORegTrace)reg).setState(b ? Fields.ON : Fields.OFF);
				return 0;
			} else if(reg instanceof IORegister) {
				((IORegister)reg).setState(b ? Fields.ON : Fields.OFF);
				return 0;
			}
		}
		else if(result instanceof OperandPoint) {
			// Update a position register point
			Point p = ((OperandPoint)result).getPointValue();
			if(reg instanceof PositionRegister) {
				((PositionRegister)reg).point = p;
				return 0;
			}
		}

		return 1;
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
			
			if (reg == null || reg.idx == -1) {
				ret[1] = "...]=";
				
			} else if (reg instanceof IORegister) {
				// IO registers are 1-indexed
				ret[1] = String.format("%d] =", reg.idx);
				
			} else {
				ret[1] = String.format("%d] =", reg.idx + 1);
			}
			
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

	@Override
	public Operand<?> setOperand(int idx, Operand<?> o) {
		Operand<?> ret;
		
		if(expr instanceof Expression) {
			ret = ((Expression)expr).setOperand(idx, o);
		} else if(idx == 0) {
			ret = expr.setArg1(o);
		} else if(idx == 2) {
			ret = expr.setArg2(o);
		} else {
			ret = null;
		}
		
		return ret;
	}

	@Override
	public Operator setOperator(int idx, Operator o) {
		Operator ret;
		
		if(expr instanceof Expression) {
			ret = ((Expression)expr).setOperator(idx, o);
		} else {
			expr.setOp(o);
			ret = expr.getOp();
		}
		
		return ret;
	}

	@Override
	public Operand<?> getOperand(int idx) {
		if(expr instanceof Expression) {
			return ((Expression)expr).getOperand(idx);
		} else if(idx == 0) {
			return expr.getArg1();
		} else if(idx == 2) {
			return expr.getArg2();
		} else {
			return null;
		}
	}

	@Override
	public Operator getOperator(int idx) {
		if(expr instanceof Expression) {
			return ((Expression)expr).getOperator(idx);
		} else {
			return expr.getOp();
		}
	}
	
	@Override
	public int getHeaderLength() {
		//Number of elements before expression start
		return posIdx == -1 ? 4 : 5;
	}
}