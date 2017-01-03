package expression;
import programming.Program;
import programming.RegStmtPoint;
import regs.PositionRegister;
import regs.PositionType;
import regs.RegisterFile;
import robot.RobotRun;

/**
 * A operand that can represent either the joint angles or cartesian values
 * stored in a position register entry, or a specific value of that position,
 * from either the global Position Registers or the local positions of the active program.
 */
public class PositionOp extends RegisterOp {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	private final int posIdx;
	/* Determines whether a global Position register or a local position will be used */
	private final PositionType type;

	public PositionOp(RobotRun robotRun) {
		super(robotRun, 0);
		this.robotRun = robotRun;
		posIdx = -1;
		type = PositionType.LOCAL;
	}

	public PositionOp(RobotRun robotRun, int ldx, PositionType t) {
		super(robotRun, ldx);
		this.robotRun = robotRun;
		posIdx = -1;
		type = t;
	}

	public PositionOp(RobotRun robotRun, int ldx, int pdx, PositionType t) {
		super(robotRun, ldx);
		this.robotRun = robotRun;
		posIdx = pdx;
		type = t;
	}

	public int getPositionIdx() { return posIdx; }
	public PositionType getPositionType() { return type; }

	public Object getValue() {
		RegStmtPoint pt;

		if (type == PositionType.LOCAL) {
			// Use local position
			Program current = robotRun.activeProgram();
			// TODO Use joint angles?
			pt = new RegStmtPoint(current.getPosition( getIdx() ), true);
		} else if (type == PositionType.GLOBAL) {
			// global Position register
			PositionRegister preg = (PositionRegister)RegisterFile.getPReg(getIdx());
			pt = new RegStmtPoint(preg.point, preg.isCartesian);
		} else {
			// Not a valid type
			return null;
		}

		if (posIdx == -1) {
			// Use the whole Point
			return pt;
		} else {
			// Use a specific value of the Point
			return pt.getValue(posIdx);
		}
	}

	public Operand clone() {
		return new PositionOp(this.robotRun, getIdx(), posIdx, type);
	}

	public String toString() {
		if (posIdx == -1) {

			if (type == PositionType.GLOBAL) {
				return String.format("PR[%d]", getIdx());
			} else {
				return String.format("P[%d]", getIdx());
			}
		} else {

			if (type == PositionType.GLOBAL) {
				return String.format("PR[%d]", getIdx());
			} else {
				return String.format("P[%d, %d]", getIdx(), posIdx);
			}
		}
	}
}