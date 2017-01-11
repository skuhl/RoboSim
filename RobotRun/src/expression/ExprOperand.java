package expression;

import geom.Point;
import global.Fields;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;

public class ExprOperand implements ExpressionElement {
	public int type;

	Float dataVal = null;
	private Boolean boolVal = null;
	int posIdx = 0;
	Register regVal = null;
	Point pointVal = null;

	//default constructor
	public ExprOperand() {
		type = ExpressionElement.UNINIT;
	}

	//create boolean operand
	public ExprOperand(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
	}

	//create data register operand
	public ExprOperand(DataRegister dReg) {
		type = ExpressionElement.DREG;
		regVal = dReg;
	}

	//create floating point operand
	public ExprOperand(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
	}

	//create IO register operand
	public ExprOperand(IORegister ioReg) {
		type = ExpressionElement.IOREG;
		regVal = ioReg;
	}

	//create point operand (used during evaulation only) 
	public ExprOperand(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
	}

	//create position register operand
	public ExprOperand(PositionRegister pReg){
		type = ExpressionElement.PREG;
		regVal = pReg;
	}

	//create position register operand on a given value of the register's position
	public ExprOperand(PositionRegister pReg, int j){
		type = ExpressionElement.PREG_IDX;
		posIdx = j;
		regVal = pReg;
	}

	public ExprOperand clone() {
		switch(type) {
		case ExpressionElement.UNINIT: return new ExprOperand();
		case ExpressionElement.SUBEXP: return ((Expression)this).clone();
		case ExpressionElement.FLOAT:  return new ExprOperand(dataVal);
		case ExpressionElement.BOOL:   return new ExprOperand(getBoolVal());
		case ExpressionElement.DREG:   return new ExprOperand((DataRegister)regVal);
		case ExpressionElement.IOREG:  return new ExprOperand((IORegister)regVal);
		case ExpressionElement.PREG:   return new ExprOperand((PositionRegister)regVal);
		case ExpressionElement.PREG_IDX: return new ExprOperand((PositionRegister)regVal, posIdx);
		case ExpressionElement.POSTN:  return new ExprOperand(pointVal);
		default:                       return null;
		}
	}

	public Boolean getBoolVal() {
		if(type == ExpressionElement.BOOL) {
			return boolVal;
		} else if(type == ExpressionElement.IOREG) {
			return (((IORegister)regVal).state == Fields.ON);
		} else {
			return null;
		}
	}

	public Float getDataVal() {
		if(type == ExpressionElement.FLOAT) {
			return dataVal;
		} else if(type == ExpressionElement.DREG) {
			return ((DataRegister)regVal).value;
		} else if(type == ExpressionElement.PREG_IDX) {
			return ((PositionRegister)regVal).getPointValue(posIdx);
		} else {
			return null;
		}
	}

	public int getLength() {
		return (type == PREG_IDX) ? 2 : 1;
	}

	public Point getPointVal() {
		if(type == ExpressionElement.PREG) {
			return ((PositionRegister)regVal).point;
		} else if(type == ExpressionElement.POSTN) {
			return pointVal;
		} else {
			return null;
		}
	}

	public Integer getPosIdx() {
		if (type == ExpressionElement.PREG_IDX) {
			return posIdx;
		}

		return null;
	}

	public int getRegIdx() {
		if (regVal == null) {
			return -1;
		}
		
		return regVal.idx;
	}

	public ExprOperand reset() {
		type = ExpressionElement.UNINIT;
		regVal = null;
		pointVal = null;
		return this;
	}

	public ExprOperand set(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
		return this;
	}

	public ExprOperand set(DataRegister dReg) {
		type = ExpressionElement.DREG;
		regVal = dReg;
		return this;
	}

	public ExprOperand set(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
		return this;
	}

	public ExprOperand set(IORegister ioReg) {
		type = ExpressionElement.IOREG;
		regVal = ioReg;
		return this;
	}

	public ExprOperand set(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
		return this;
	}

	public ExprOperand set(PositionRegister pReg) {
		regVal = pReg;
		return this;
	}

	public ExprOperand set(PositionRegister pReg, int pdx) {
		type = ExpressionElement.PREG_IDX;
		posIdx = pdx;
		regVal = pReg;
		return this;
	}
	
	public ExprOperand set(int pdx) {
		posIdx = pdx;
		return this;
	}

	public void setBoolVal(Boolean boolVal) {
		this.boolVal = boolVal;
	}

	public String toString(){
		String s = "";
		switch(type){
		case UNINIT:
			s = "...";
			break;
		case SUBEXP: 
			s = ((AtomicExpression)this).toString();
			break;
		case FLOAT:
			s += dataVal;
			break;
		case BOOL:
			s += getBoolVal() ? "TRUE" : "FALSE";
			break;
		case DREG:
		case IOREG:
		case PREG:
			if (regVal == null) {
				s += "[...]";
			} else {
				s += regVal.toString();
			}
			
			break;
		case PREG_IDX:
			if (regVal == null) {
				s += "[...]";
				
			} else {
				s += ((PositionRegister)regVal).toString(posIdx);
			}
			break;
		}

		return s;
	}

	public String[] toStringArray() {
		if(type == PREG_IDX) {
			String rNum = (regVal == null || regVal.idx < 0) ? "..." : Integer.toString(regVal.idx + 1);
			String pIdx = (posIdx == -1) ? "..." : Integer.toString(posIdx + 1);

			return new String[] { "PR[" + rNum + ",", " " + pIdx + "]" };
		} else {
			return new String[] { this.toString() };
		}
	}
}