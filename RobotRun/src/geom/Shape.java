package geom;
import robot.RobotRun;

/**
 * A simple class that defines the stroke and fill color for a shape
 * along with some methods necessarry for a shape.
 */
public abstract class Shape implements Cloneable {
	private Integer fillCVal;
	private Integer strokeCVal;

	public Shape() {
		fillCVal = RobotRun.getInstance().color(0);
		strokeCVal = RobotRun.getInstance().color(225);
	}

	public Shape(Integer fill, Integer strokeVal) {
		fillCVal = fill;
		strokeCVal = strokeVal;
	}

	/**
	 * Apply stroke and fill colors.
	 */
	protected void applyColors() {
		if (strokeCVal == null) {
			RobotRun.getInstance().noStroke();

		} else {
			RobotRun.getInstance().stroke(strokeCVal);
		}

		if (fillCVal == null) {
			RobotRun.getInstance().noFill();

		} else {
			RobotRun.getInstance().fill(fillCVal);
		} 
	}

	@Override
	public abstract Shape clone();

	public abstract void draw();

	/**
	 * Returns the value of the given dimension associated with
	 * this shape. If no such dimension exists, then -1 should
	 * be returned.
	 * 
	 * @param dim  The dimension of which to get the value
	 * @returning  The value of that dimension, or -1, if no
	 *             such dimension exists
	 */
	public abstract float getDim(DimType dim);
	
	public abstract float[] getDimArray();

	/* Getters and Setters for shapes fill and stroke colors */

	public Integer getFillValue() { return fillCVal; }
	public Integer getStrokeValue() { return strokeCVal; }
	/**
	 * Sets the value of the given dimension associated with
	 * this shape, if that dimension exists.
	 * 
	 * @param newVal  The value to which to set the dimension
	 * @param dim     The dimension of  which ro set the value
	 */
	public abstract void setDim(Float newVal, DimType dim);
	public void setFillValue(Integer newVal) { fillCVal = newVal; }

	public void setStrokeValue(Integer newVal) { strokeCVal = newVal; }
}