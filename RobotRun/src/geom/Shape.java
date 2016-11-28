package geom;
import robot.RobotRun;

/**
 * A simple class that defines the stroke and fill color for a shape
 * along with some methods necessarry for a shape.
 */
public abstract class Shape implements Cloneable {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	private Integer fillCVal,
	strokeCVal;

	public Shape(RobotRun robotRun) {
		this.robotRun = robotRun;
		fillCVal = this.robotRun.color(0);
		strokeCVal = this.robotRun.color(225);
	}

	public Shape(RobotRun robotRun, Integer fill, Integer strokeVal) {
		this.robotRun = robotRun;
		fillCVal = fill;
		strokeCVal = strokeVal;
	}

	/**
	 * Sets the value of the given dimension associated with
	 * this shape, if that dimension exists.
	 * 
	 * @param newVal  The value to which to set the dimension
	 * @param dim     The dimension of  which ro set the value
	 */
	public abstract void setDim(Float newVal, DimType dim);

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

	/**
	 * Apply stroke and fill colors.
	 */
	protected void applyColors() {
		if (strokeCVal == null) {
			this.robotRun.noStroke();

		} else {
			this.robotRun.stroke(strokeCVal);
		}

		if (fillCVal == null) {
			this.robotRun.noFill();

		} else {
			this.robotRun.fill(fillCVal);
		} 
	}

	public abstract void draw();

	/* Getters and Setters for shapes fill and stroke colors */

	public Integer getStrokeValue() { return strokeCVal; }
	public void setStrokeValue(Integer newVal) { strokeCVal = newVal; }
	public Integer getFillValue() { return fillCVal; }
	public void setFillValue(Integer newVal) { fillCVal = newVal; }

	@Override
	public abstract Object clone();
}