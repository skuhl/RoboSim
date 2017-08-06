package geom;

import global.RMath;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

/**
 * A complex shape formed from a .stl source file.
 */
public class ComplexShape extends RShape {
	public final float MAX_SCALE;
	public final float MIN_SCALE;
	
	private PVector centerOffset, baseDims;
	private float mdlScale = 1f;
	private PShape mesh;	
	private String srcFilePath;

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color.
	 * 
	 * @throws IllegalArgumentException	If the given model's base dimensions
	 * 									are outside the range of a world
	 * 									object's dimensions
	 */
	public ComplexShape(String filename, int fill) {
		this(filename, fill, 1f);
	}

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color and scale value.
	 * 
	 * @throws IllegalArgumentException	If the given model's scaled dimensions
	 * 									are outside the range of a world
	 * 									object's dimensions
	 */
	public ComplexShape(String filename, int fill, float scale)
			throws IllegalArgumentException {
		
		super(fill, null);
		
		srcFilePath = filename;
		mesh = MyPShape.loadSTLModel(filename, fill);
		
		iniDimensions();
		MIN_SCALE = 10f / RMath.min(baseDims.x, baseDims.y, baseDims.z);
		MAX_SCALE = 1000f / RMath.max(baseDims.x, baseDims.y, baseDims.z);
		
		if ((MAX_SCALE - MIN_SCALE) < 0f) {
			/* The model cannot be scaled to fit within the bounds of a world
			 * object's dimensions */
			String msg = String.format("%s\n%f - %f = %f\n", filename, MAX_SCALE,
					MIN_SCALE, MAX_SCALE - MIN_SCALE);
			throw new IllegalArgumentException(msg);
			
		} else if (scale > MAX_SCALE || scale < MIN_SCALE) {
			// The given scale is out of bounds
			String msg = String.format("The model's scale must be between %4.5f and %4.5f",
					MIN_SCALE, MAX_SCALE);
			throw new IllegalArgumentException(msg);
			
		} else {
			setDim(scale, DimType.SCALE);
		}
	}
	
	@Override
	public ComplexShape clone() {
		return new ComplexShape(srcFilePath, getFillValue(), mdlScale);
	}
	
	@Override
	public void draw(PGraphics g) {
		g.pushMatrix();
		g.translate(centerOffset.x, centerOffset.y, centerOffset.z);
		g.shape(mesh);
		g.popMatrix();
	}
	
	/**
	 * @return	The center offset associated with this model
	 */
	public float[] getCenterOffset() {
		return new float[] {
				centerOffset.x, centerOffset.y, centerOffset.z
		};
	}

	@Override
	public float getDim(DimType dim) {
		switch(dim) {
		// Determine dimension based on the scale
		case LENGTH: return mdlScale * (baseDims.x);
		case HEIGHT: return mdlScale * (baseDims.y);
		case WIDTH:  return mdlScale * (baseDims.z);
		case SCALE:  return mdlScale;
		default:     return -1f;
		}
	}
	
	@Override
	public float[] getDimArray() {
		float[] dims = new float[3];
		dims[0] = getDim(DimType.LENGTH);
		dims[1] = getDim(DimType.HEIGHT);
		dims[2] = getDim(DimType.WIDTH);
		return dims;
	}
	
	@Override
	public float getDimLBound(DimType dim) {
		if (dim == DimType.SCALE) {
			// Include the scale dimension
			return MIN_SCALE;
			
		} else {
			return super.getDimLBound(dim);
		}
	}
	
	@Override
	public float getDimUBound(DimType dim) {
		if (dim == DimType.SCALE) {
			// Include the scale dimension
			return MAX_SCALE;
			
		} else {
			return super.getDimLBound(dim);
		}
	}
	
	public PShape getMesh() {
		return mesh;
	}

	public String getSourcePath() { return srcFilePath; }
	
	@Override
	public void setDim(Float newVal, DimType dim) {
		switch(dim) {
		case SCALE:
			// Update the model's scale
			centerOffset.mult(newVal / mdlScale);
			mesh.scale(newVal / mdlScale);
			mdlScale = newVal;
			break;

		default:
		}
	}

	@Override
	public void setFillValue(Integer newVal) {
		if (newVal != null) {
			super.setFillValue(newVal);
			mesh.setFill((int)newVal);
		}
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

		int vertexCount = mesh.getVertexCount();

		// Calculate the maximum and minimum values for each dimension
		for (int idx = 0; idx < vertexCount; ++idx) {
			PVector v = mesh.getVertex(idx);

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
}