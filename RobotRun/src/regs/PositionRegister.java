package regs;
import geom.Point;

/**
 * An extension of the register class, which houses a point along with an
 * associated comment.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public class PositionRegister extends Register {
	
	/**
	 * What part of the point should be used as the current reference of the
	 * register: the Cartesian values or the angles values.
	 */
	public boolean isCartesian;
	
	/**
	 * The robot point associated with this register. The position and
	 * orientation of the point are frame independent.
	 * 
	 * NOTE: this value can be null!
	 */
	public Point point;
	
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
	
	@Override
	public String regPrefix() {
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
			
			return String.format("%s[%s, %d]", regPrefix(), idxStr, pdx + 1);
		}
		// Invalid index
		return null;
	}
}