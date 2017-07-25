package geom;

import camera.RegisteredModels;
import core.RobotRun;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Defines the length, width, height values to draw a box.
 */
public class RBox extends RShape {
	
	/**
	 * X -> length
	 * Y -> Height
	 * Z -> Width
	 */
	private PVector dimensions;

	/**
	 * Create a cube, with an edge length of 10.
	 */
	public RBox() {
		super();
		dimensions = new PVector(10f, 10f, 10f);
	}

	/**
	 * Create an empty cube with the given color and dimension.
	 */
	public RBox(int strokeVal, float edgeLen) {
		super(null, strokeVal);
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	/**
	 * Create an empty box with the given color and dimensions.
	 */
	public RBox(int strokeVal, float len, float hgt, float wdh) {
		super(null, strokeVal);
		dimensions = new PVector(len, hgt, wdh);
	}

	/**
	 * Create a cube with the given colors and dimension.
	 */
	public RBox(int fill, int strokeVal, float edgeLen) {
		super(fill, strokeVal);
		dimensions = new PVector(edgeLen, edgeLen, edgeLen);
	}

	/**
	 * Create a box with the given colors and dimensions.
	 */
	public RBox(int fill, int strokeVal, float len, float hgt, float wdh) {
		super(fill, strokeVal);
		dimensions = new PVector(len, hgt, wdh);
	}

	@Override
	public RBox clone() {
		return new RBox(getFillValue(), getStrokeValue(), dimensions.x, dimensions.y, dimensions.z);
	}
	
	@Override
	public void draw(PGraphics g) {
		g.pushStyle();
		applyStyle(g);
		
		g.box(dimensions.x, dimensions.y, dimensions.z);
		
		g.popStyle();
	}

	@Override
	public float getDim(DimType dim) {    
		switch (dim) {
		case LENGTH:  return dimensions.x;
		case HEIGHT:  return dimensions.y;
		case WIDTH:   return dimensions.z;
		// Invalid dimension
		default:      return -1f;
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

	public int getFamilyID() {
		return RegisteredModels.ID_CUBE;
	}

	@Override
	public int getModelID() {
		return RegisteredModels.ID_CUBE;
	}
	
	@Override
	public PGraphics getModelPreview(RMatrix m) {
		if(preview == null) {
			PGraphics img = RobotRun.getInstance().createGraphics(150, 200, RobotRun.P3D);
			float[][] rMat = m.getDataF();
			img.beginDraw();
			img.ortho();
			img.lights();
			img.background(255);
			img.stroke(0);
			img.translate(75, 100, 0);
			img.applyMatrix(
					rMat[0][0], rMat[1][0], rMat[2][0], 0,
					rMat[0][1], rMat[1][1], rMat[2][1], 0,
					rMat[0][2], rMat[1][2], rMat[2][2], 0,
					0, 0, 0, 1
			);
			draw(img);
			img.resetMatrix();
			img.translate(-75, -100);
			img.endDraw();
			
			preview = img;
		}
		
		return preview;
	}

	@Override
	public Float getReflectiveIndex() {
		return 1f;
	}
	
	@Override
	public void setDim(Float newVal, DimType dim) {

		switch (dim) {
		case LENGTH:
			// Update length
			dimensions.x = newVal;
			break;
		case HEIGHT:
			// Update height
			dimensions.y = newVal;
			break;

		case WIDTH:
			// Update width
			dimensions.z = newVal;
			break;
			// Invalid dimension
		default:
		}
	}
}