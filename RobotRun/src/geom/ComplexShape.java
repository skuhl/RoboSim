package geom;

import java.util.ArrayList;

import global.RegisteredModels;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import robot.CamSelectArea;
import robot.CamSelectView;
import robot.RobotRun;

/**
 * A complex shape formed from a .stl source file.
 */
public class ComplexShape extends RShape {
	
	private String srcFilePath;
	
	private MyPShape model;
	private PVector centerOffset, baseDims;
	
	private PGraphics preview;
	
	private float mdlScale;
	private final int model_id;
	private final int model_family_id;
	private ArrayList<CamSelectArea> selectAreas;

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color.
	 * 
	 * @throws NullPointerException  if the given filename is
	 *         not a valid .stl file in RobotRun/data/
	 */
	public ComplexShape(String filename, MyPShape mdl, int fill) {
		super(fill, null);
		model_id = RegisteredModels.modelIDList.get(filename);
		model_family_id = RegisteredModels.modelFamilyList.get(model_id);
		srcFilePath = filename;
		
		mdlScale = 1f;
		model = mdl;
<<<<<<< HEAD
		iniDimensions();
		
		preview = loadModelPreview();
		selectAreas = new ArrayList<CamSelectArea>();
		loadSelectAreas();
=======
		model.setFill(fill);
		selectAreas = new ArrayList<CamSelectArea>();
		
		loadCamSelectAreas();
		iniDimensions();
>>>>>>> 098a75a4ae0da1a1f612c0b6e879f52f3481e4b4
	}

	/**
	 * Create a complex model from the soruce .stl file of the
	 * given name, filename, stored in the '/RobotRun/data/'
	 * with the given fill color and scale value.
	 * 
	 * @throws NullPointerException  if the given filename is
	 *         not a valid .stl file in RobotRun/data/
	 */
	public ComplexShape(String filename, MyPShape mdl, int fill, float scale) {
		super(fill, null);
		model_id = RegisteredModels.modelIDList.get(filename);
		model_family_id = RegisteredModels.modelFamilyList.get(model_id);
		srcFilePath = filename;
<<<<<<< HEAD
		// The initial scale MUST be one in order for scaling to work properly!
		mdlScale = 1f;
		model = mdl;
		iniDimensions();
		// This sets the scale!
		setDim(scale, DimType.SCALE);
		
		preview = loadModelPreview();
		selectAreas = new ArrayList<CamSelectArea>();
		loadSelectAreas();
=======
		
		mdlScale = 1f;
		model = mdl;
		selectAreas = new ArrayList<CamSelectArea>();
		
		loadCamSelectAreas();
		iniDimensions();
		setDim(scale, DimType.SCALE);
>>>>>>> 098a75a4ae0da1a1f612c0b6e879f52f3481e4b4
	}
	
	private void loadCamSelectAreas() {
		if(RegisteredModels.modelAreasOfInterest.get(model_id) != null) {
			for(CamSelectArea c: RegisteredModels.modelAreasOfInterest.get(model_id)) {
				selectAreas.add(c.copy());
			}
		}
	}
	
	public CamSelectArea getCamSelectArea(int i) {
		return selectAreas.get(i);
	}
	
	public int getNumSelectAreas() {
		return selectAreas.size();
	}
	
	public CamSelectArea getSelectAreaClicked(int x, int y, RMatrix m) {
		for(CamSelectArea a: selectAreas) {
			CamSelectView v = a.getView(m);
			
			if(v != null) {
				PVector tl = v.getTopLeftBound();
				PVector br = v.getBottomRightBound();
				
				if(x >= tl.x && x <= br.x && y >= tl.y && y <= br.y) {
					return a;
				}
			}
		}
		
		return null;
	}

	@Override
	public ComplexShape clone() {
		return new ComplexShape(srcFilePath, model.clone(), getFillValue(),
				mdlScale);
	}
	
	@Override
	public void draw(PGraphics g) {
		g.pushMatrix();
		g.translate(centerOffset.x, centerOffset.y, centerOffset.z);
		
		g.shape(model);
		
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
	public int getID() {
		return model_id;
	}
	
	@Override
	public int getFamilyID() {
		return model_family_id;
	}
	
	public PShape getForm() {
		return model;
	}

	public String getSourcePath() { return srcFilePath; }

	/**
	 * Calculates the maximum length, height, and width of this shape as well as the center
	 * offset of the shape. The length, height, and width are based off of the maximum and
	 * minimum X, Y, Z values of the shape's vertices. The center offset is based off of
	 * the estimated center of the shape relative to the minimum X, Y, Z values as a position.
	 */
	private void iniDimensions() {
		PVector maximums = new PVector(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE),
				minimums = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

		int vertexCount = model.getVertexCount();

		// Calculate the maximum and minimum values for each dimension
		for (int idx = 0; idx < vertexCount; ++idx) {
			PVector v = model.getVertex(idx);

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
	
	public PGraphics getModelPreview(RMatrix m) {
		if(preview == null) {
			PGraphics img = RobotRun.getInstance().createGraphics(150, 200, RobotRun.P3D);
			float[][] rMat = m.getFloatData();
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
			img.scale(2/mdlScale);
			img.shape(model);
			img.resetMatrix();
			img.translate(-75, -100);
						
			for(CamSelectArea a: selectAreas) {
				CamSelectView v = a.getView(m);
				if(a.isEmphasized()) {
					img.stroke(0, 255, 0);
					img.fill(0, 255, 0, 126);
				}
				else if(a.isIgnored()) {
					img.stroke(255, 0, 0);
					img.fill(255, 0, 0, 126);
				}
				else {
					img.stroke(0);
					img.fill(0, 0, 0, 126);
				}
				
				if(v != null) {
					PVector c = v.getTopLeftBound();
					float w = v.getWidth();
					float h = v.getHeight();
					img.rect(c.x, c.y, w, h);
				}
			}
			
			img.endDraw();
			
			preview = img;
		}
		
		return preview;
	}
	
	public PGraphics updateModelPreview(RMatrix m) {
		preview = null;
		return getModelPreview(m);
	}

	@Override
	public void setDim(Float newVal, DimType dim) {
		switch(dim) {
		case SCALE:
			// Update the model's scale
			centerOffset.mult(newVal / mdlScale);
			model.scale(newVal / mdlScale);
			mdlScale = newVal;
			break;

		default:
		}
	}
}