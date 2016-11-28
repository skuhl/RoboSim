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
	private int regIdx = -1;
	int posIdx = 0;
	Register regVal = null;
	Point pointVal = null;

	//default constructor
	public ExprOperand() {
		type = ExpressionElement.UNINIT;
	}

	//create floating point operand
	public ExprOperand(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
	}

	//create boolean operand
	public ExprOperand(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
	}

	//create data register operand
	public ExprOperand(DataRegister dReg, int i) {
		type = ExpressionElement.DREG;
		setRegIdx(i);
		regVal = dReg;
	}

	//create IO register operand
	public ExprOperand(IORegister ioReg, int i) {
		type = ExpressionElement.IOREG;
		setRegIdx(i);
		regVal = ioReg;
	}

	//create position register operand
	public ExprOperand(PositionRegister pReg, int i){
		type = ExpressionElement.PREG;
		setRegIdx(i);
		regVal = pReg;
	}

	//create position register operand on a given value of the register's position
	public ExprOperand(PositionRegister pReg, int i, int j){
		type = ExpressionElement.PREG_IDX;
		setRegIdx(i);
		posIdx = j;
		regVal = pReg;
	}

	//create point operand (used during evaulation only) 
	public ExprOperand(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
	}

	public Integer getRdx() {
		if (type == ExpressionElement.DREG ||
				type == ExpressionElement.PREG ||
				type == ExpressionElement.PREG_IDX ||
				type == ExpressionElement.IOREG) {

			return getRegIdx();
		}

		return null;
	}

	public Integer getPosIdx() {
		if (type == ExpressionElement.PREG_IDX) {
			return posIdx;
		}

		return null;
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

	public Boolean getBoolVal() {
		if(type == ExpressionElement.BOOL) {
			return boolVal;
		} else if(type == ExpressionElement.IOREG) {
			return (((IORegister)regVal).state == Fields.ON);
		} else {
			return null;
		}
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

	public ExprOperand set(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
		return this;
	}

	public ExprOperand set(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
		return this;
	}

	public ExprOperand set(DataRegister dReg, int i) {
		type = ExpressionElement.DREG;
		setRegIdx(i);
		regVal = dReg;
		return this;
	}

	public ExprOperand set(IORegister ioReg, int i) {
		type = ExpressionElement.IOREG;
		setRegIdx(i);
		regVal = ioReg;
		return this;
	}

	public ExprOperand set(PositionRegister pReg, int i) {
		if(type != ExpressionElement.PREG_IDX)
			type = ExpressionElement.PREG;

		setRegIdx(i);
		regVal = pReg;
		return this;
	}

	public ExprOperand set(PositionRegister pReg, int i, int j) {
		type = ExpressionElement.PREG_IDX;
		setRegIdx(i);
		posIdx = j;
		regVal = pReg;
		return this;
	}

	public ExprOperand set(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
		return this;
	}

	public ExprOperand reset() {
		type = ExpressionElement.UNINIT;
		setRegIdx(-1);
		regVal = null;
		pointVal = null;
		return this;
	}

	public int getLength() {
		return (type == PREG_IDX) ? 2 : 1;
	}

	public ExprOperand clone() {
		switch(type) {
		case ExpressionElement.UNINIT: return new ExprOperand();
		case ExpressionElement.SUBEXP: return ((Expression)this).clone();
		case ExpressionElement.FLOAT:  return new ExprOperand(dataVal);
		case ExpressionElement.BOOL:   return new ExprOperand(getBoolVal());
		case ExpressionElement.DREG:   return new ExprOperand((DataRegister)regVal, getRegIdx());
		case ExpressionElement.IOREG:  return new ExprOperand((IORegister)regVal, getRegIdx());
		case ExpressionElement.PREG:   return new ExprOperand((PositionRegister)regVal, getRegIdx());
		case ExpressionElement.PREG_IDX: return new ExprOperand((PositionRegister)regVal, getRegIdx(), posIdx);
		case ExpressionElement.POSTN:  return new ExprOperand(pointVal);
		default:                       return null;
		}
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
			String rNum = (getRegIdx() == -1) ? "..." : ""+getRegIdx();
			s += "R[" + rNum + "]";
			break;
		case IOREG:
			rNum = (getRegIdx() == -1) ? "..." : ""+getRegIdx();
			s += "IO[" + rNum + "]";
			break;
		case PREG:
			rNum = (getRegIdx() == -1) ? "..." : ""+getRegIdx();
			s += "PR[" + rNum + "]";
			break;
		case PREG_IDX:
			rNum = (getRegIdx() == -1) ? "..." : ""+getRegIdx();
			String pIdx = (posIdx == -1) ? "..." : ""+posIdx;
			s += "PR[" + rNum + ", " + pIdx + "]";
			break;
		}

		return s;
	}

	public String[] toStringArray() {
		if(type == PREG_IDX) {
			String rNum = (getRegIdx() == -1) ? "..." : ""+getRegIdx();
			String pIdx = (posIdx == -1) ? "..." : ""+posIdx;

			return new String[] { "PR[" + rNum + ",", " " + pIdx + "]" };
		} else {
			return new String[] { this.toString() };
		}
	}

	public int getRegIdx() {
		return regIdx;
	}

	public void setRegIdx(int regIdx) {
		this.regIdx = regIdx;
	}

	public void setBoolVal(Boolean boolVal) {
		this.boolVal = boolVal;
	}
}