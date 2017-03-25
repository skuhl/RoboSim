package regs;
import geom.Point;

/* A simple class for a Position Register of the Robot Arm, which holds a point associated with a comment. */
public class PositionRegister extends Register {
	/**
	 * The point associated with this Position Register, which is saved in
	 * the current User frame with the active Tool frame TCP offset, though
	 * is independent of Frames
	 */
	public Point point;
	public boolean isCartesian;

	public PositionRegister() {
		super();
		comment = null;
		point = null;
		isCartesian = false;
	}

	public PositionRegister(int i) {
		super(i, null);
		point = null;
		isCartesian = false;
	}

	public PositionRegister(int i, String c, Point pt, boolean isCart) {
		super(i, c);
		point = pt;
		isCartesian = isCart;
	}

	/**
	 * Returns the value of the point stored in this register which corresponds
	 * to the register mode (joint or cartesian) and the given index 'idx.'
	 * Note that 'idx' should be in the range of 0 to 5 inclusive, as this value
	 * is meant to represent either 1 of 6 joint angles for a joint type point,
	 * or 1 of 6 cartesian points (x, y, z, w, p, r) for a cartesian type point.
	 */
	public Float getPointValue(int idx) {
		if(point == null) {
			return null;
		}

		if(!isCartesian) {
			return point.getValue(idx);
		}
		else {
			return point.getValue(idx + 6);
		}
	}

	public void setPointValue(int idx, float value) {
		if(point == null) {
			point = new Point();
		}

		if(!isCartesian) {
			point.setValue(idx, value);
		}
		else {
			point.setValue(idx + 6, value);
		}
	}
	
	@Override
	protected String regPrefix() {
		return "PR";
	}
	
	/**
	 * A variation of the position's string form, that includes an index, which
	 * references one of the position's fields (i.e. X, Y, Z, W, P, or R)
	 * 
	 * @param pdx	A integer between 0 and 4, inclusive
	 * @return		The string form of a specific field of a position register
	 */
	public String toString(int pdx) {
		if (pdx >= 0 && pdx < 5) {
			String idxStr = (idx < 0) ? "..." : Integer.toString(idx + 1);
			
			return String.format("P[%s, %d]", idxStr, pdx + 1);
		}
		// Invalid index
		return null;
	}
}