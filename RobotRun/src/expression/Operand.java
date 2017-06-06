package expression;

import core.RobotRun;
import geom.Point;
import geom.WorldObject;
import global.Fields;
import global.RMath;
import processing.core.PVector;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;

public class Operand implements ExpressionElement {
	public int type;

	private WorldObject objProto = null;
	private Float dataVal = null;
	private Boolean boolVal = null;
	private int posIdx = 0;
	private Register regVal = null;
	private Point pointVal = null;

	//default constructor
	public Operand() {
		type = ExpressionElement.UNINIT;
	}

	//create boolean operand
	public Operand(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
	}

	//create data register operand
	public Operand(DataRegister dReg) {
		type = ExpressionElement.DREG;
		regVal = dReg;
	}

	//create floating point operand
	public Operand(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
	}

	//create IO register operand
	public Operand(IORegister ioReg) {
		type = ExpressionElement.IOREG;
		regVal = ioReg;
	}

	//create point operand (used during evaulation only) 
	public Operand(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
	}

	//create position register operand
	public Operand(PositionRegister pReg){
		type = ExpressionElement.PREG;
		regVal = pReg;
	}

	//create position register operand on a given value of the register's position
	public Operand(PositionRegister pReg, int j) {
		type = ExpressionElement.PREG_IDX;
		posIdx = j;
		regVal = pReg;
	}
	
	public Operand(WorldObject o) {
		type = ExpressionElement.CAM_MATCH;
		
	}

	@Override
	public Operand clone() {
		switch(type) {
		case ExpressionElement.UNINIT: return new Operand();
		case ExpressionElement.SUBEXP: return ((Expression)this).clone();
		case ExpressionElement.FLOAT:  return new Operand(dataVal);
		case ExpressionElement.BOOL:   return new Operand(getBoolVal());
		case ExpressionElement.DREG:   return new Operand((DataRegister)regVal);
		case ExpressionElement.IOREG:  return new Operand((IORegister)regVal);
		case ExpressionElement.PREG:   return new Operand((PositionRegister)regVal);
		case ExpressionElement.PREG_IDX: return new Operand((PositionRegister)regVal, posIdx);
		case ExpressionElement.POSTN:  return new Operand(pointVal);
		case ExpressionElement.CAM_MATCH: return new Operand(objProto);
		default:                       return null;
		}
	}

	public Boolean getBoolVal() {
		if(type == ExpressionElement.BOOL) {
			return boolVal;
		} else if(type == ExpressionElement.IOREG) {
			return (((IORegister)regVal).state == Fields.ON);
		} else if(type == ExpressionElement.CAM_MATCH) {
			return RobotRun.getInstance().getRobotCamera().taughtObjectInFrame(posIdx, RobotRun.getInstance().getActiveScenario());
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
			PositionRegister pReg = (PositionRegister)regVal;
			// Convert position into world frame reference
			if (posIdx >= 0 && posIdx < 3) {
				PVector wPos = RMath.vToWorld(pReg.point.position);
				
				if (posIdx == 0) {
					return wPos.x;
					
				} else if (posIdx == 1) {
					return wPos.y;
					
				} else if (posIdx == 2) {
					return wPos.z;
				}
				
			// Convert orientation into euler angles in the world frame
			} else if (posIdx >= 3 && posIdx < 6) {
				PVector wpr = RMath.nQuatToWEuler(pReg.point.orientation);
				
				if (posIdx == 3) {
					return wpr.x;
					
				} else if (posIdx == 4) {
					return wpr.y;
					
				} else if (posIdx == 5) {
					return wpr.z;
				}
			}
		}
		
		return null;
	}

	@Override
	public int getLength() {
		return (type == PREG_IDX) ? 2 : 1;
	}
	
	/**
	 * Determines if the position register operand represents a Cartesian point
	 * or not. If this expression operand is not a position register value,
	 * then null is returned.
	 * 
	 * @return	The type of position register, or null
	 */
	public Boolean isCart() {
		if (type == ExpressionElement.PREG) {
			return ((PositionRegister)regVal).isCartesian;
		}
		// Not a position register
		return null;
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

	public Operand reset() {
		type = ExpressionElement.UNINIT;
		regVal = null;
		pointVal = null;
		return this;
	}

	public Operand set(boolean b) {
		type = ExpressionElement.BOOL;
		setBoolVal(b);
		return this;
	}

	public Operand set(DataRegister dReg) {
		type = ExpressionElement.DREG;
		regVal = dReg;
		return this;
	}

	public Operand set(float d) {
		type = ExpressionElement.FLOAT;
		dataVal = d;
		return this;
	}

	public Operand set(IORegister ioReg) {
		type = ExpressionElement.IOREG;
		regVal = ioReg;
		return this;
	}

	public Operand set(Point p) {
		type = ExpressionElement.POSTN;
		pointVal = p;
		return this;
	}

	public Operand set(PositionRegister pReg) {
		regVal = pReg;
		return this;
	}

	public Operand set(PositionRegister pReg, int pdx) {
		type = ExpressionElement.PREG_IDX;
		posIdx = pdx;
		regVal = pReg;
		return this;
	}
	
	public Operand set(int pdx) {
		posIdx = pdx;
		return this;
	}

	public void setBoolVal(Boolean boolVal) {
		this.boolVal = boolVal;
	}

	@Override
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

	@Override
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