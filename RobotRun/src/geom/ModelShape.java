package geom;
import processing.core.PShape;
import processing.core.PVector;
import robot.RobotRun;

/**
 * A complex shape formed from a .stl source file.
 */
public class ModelShape extends Shape {
	private PShape form;
	private PVector centerOffset, baseDims;
	private float scale;
	private String srcFilePath;

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color.
	 * 
	 * @throws NullPointerException  if the given filename is
	 *         not a valid .stl file in RobotRun/data/
	 */
	public ModelShape(String filename, int fill) throws NullPointerException {
		super(fill, null);
		srcFilePath = filename;
		scale = 1f;

		form = RobotRun.getInstance().loadSTLModel(filename, fill);
		iniDimensions();
	}

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color and scale value.
	 * 
	 * @throws NullPointerException  if the given filename is
	 *         not a valid .stl file in RobotRun/data/
	 */
	public ModelShape(String filename, int fill, float scale) throws NullPointerException {
		super(fill, null);
		srcFilePath = filename;
		this.scale = 1f;

		form = RobotRun.getInstance().loadSTLModel(filename, fill);
		iniDimensions();

		setDim(scale, DimType.SCALE);
	}

	/**
	 * Calculates the maximum length, height, and width of this shape as well as the center
	 * offset of the shape. The length, height, and width are based off of the maximum and
	 * minimum X, Y, Z values of the shape's vertices. The center offset is based off of
	 * the estimated center of the shape relative to the minimum X, Y, Z values as a position.
	 */
	private void iniDimensions() {
		PVector maximums = new PVector(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE),
				minimums = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		int vertexCount = form.getVertexCount();

		// Calculate the maximum and minimum values for each dimension
		for (int idx = 0; idx < vertexCount; ++idx) {
			PVector v = form.getVertex(idx);

			if (v.x > maximums.x) {
				maximums.x = v.x;

			} else if (v.x < minimums.x) {
				minimums.x = v.x;
			}

			if (v.y > maximums.y) {
				maximums.y = v.y;

			} else if (v.y < minimums.y) {
				minimums.y = v.y;
			}

			if (v.z > maximums.z) {
				maximums.z = v.z;

			} else if (v.z < minimums.z) {
				minimums.z = v.z;
			}
		}

		/* Calculate the base maximum span for each dimension as well as the base
		 * offset of the center of the shape, based on the dimensions, from the
		 * first vertex in the shape */
		baseDims = PVector.sub(maximums, minimums);
		centerOffset = PVector.add(minimums, PVector.mult(baseDims, 0.5f)).mult(-1);
	}

	public void draw() {
		RobotRun.getInstance().pushMatrix();
		// Draw shape, where its center is at (0, 0, 0)
		RobotRun.getInstance().translate(centerOffset.x, centerOffset.y, centerOffset.z);

		RobotRun.getInstance().shape(form);

		RobotRun.getInstance().popMatrix();
	}

	@Override
	public void setDim(Float newVal, DimType dim) {
		switch(dim) {
		case SCALE:
			// Update the model's scale
			centerOffset.mult(newVal / scale);
			form.scale(newVal / scale);
			scale = newVal;
			break;

		default:
		}
	}

	@Override
	public float getDim(DimType dim) {
		switch(dim) {
		// Determine dimension based on the scale
		case LENGTH: return scale * (baseDims.x);
		case HEIGHT: return scale * (baseDims.y);
		case WIDTH:  return scale * (baseDims.z);
		case SCALE:  return scale;
		default:     return -1f;
		}
	}

	public String getSourcePath() { return srcFilePath; }

	@Override
	public Object clone() {
		// Created from source file
		return new ModelShape(srcFilePath, getFillValue(), scale);
	}
}