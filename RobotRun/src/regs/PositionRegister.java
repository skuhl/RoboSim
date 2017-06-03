package regs;
import geom.Point;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public class PositionRegister extends Register {
	
	/**
	 * The robot point associated with this register. The position and
	 * orientation of the point are frame independent.
	 * 
	 * NOTE: this value can be null!
	 */
	public Point point;
	
	/**
	 * What part of the point should be used as the current reference of the
	 * register: the Cartesian values or the angles values.
	 */
	public boolean isCartesian;
	
	/**
	 * Initializes the register as empty.
	 */
	public PositionRegister() {
		super();
		point = null;
		isCartesian = false;
	}

	/**
	 * TODO comment this
	 * 
	 * @param i
	 */
	public PositionRegister(int i) {
		super(i, null);
		point = null;
		isCartesian = false;
	}

	/**
	 * TODO comment this
	 * 
	 * @param i
	 * @param c
	 * @param pt
	 * @param isCart
	 */
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
	public Float getWorldPtVal(int idx) {
		if(point == null) {
			return null;
		}

		if(!isCartesian) {
			return point.getWorldValue(idx);
		}
		else {
			return point.getWorldValue(idx + 6);
		}
	}
	
	/**
	 * TODO comment
	 * 
	 * @param idx
	 * @param value
	 */
	public void setWorldPtVal(int idx, float value) {
		if(point == null) {
			point = new Point();
		}

		if(!isCartesian) {
			point.setWorldValue(idx, value);
		}
		else {
			point.setWorldValue(idx + 6, value);
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