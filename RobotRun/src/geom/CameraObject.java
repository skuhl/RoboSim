package geom;

import java.util.ArrayList;

import camera.CamSelectArea;
import camera.CamSelectView;
import camera.RegisteredModels;
import core.RobotRun;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class CameraObject extends Part {
	
	public final int model_ID;
	public final int group_ID;
	public final float reflective_IDX;
	public final float image_quality;
	
	private ArrayList<CamSelectArea> selectAreas;
	private PGraphics preview;
	private RobotRun appRef;
	
	public CameraObject(RobotRun appRef, Part p) {
		this(appRef, p, 1f);
	}
	
	public CameraObject(RobotRun appRef, Part p, float q) {
		super(p.getName(), p.getModel().clone(), p.getOBBDims().copy(), 
				p.localOrientation.clone(), p.defaultOrientation.clone(), p.getFixtureRef());
		
		this.appRef = appRef;
		RShape mdl = p.getModel();
		if(mdl instanceof ComplexShape) {
			String fileName = ((ComplexShape)mdl).getSourcePath();
			model_ID = RegisteredModels.modelIDList.getOrDefault(fileName, RegisteredModels.ID_GENERIC);
			group_ID = RegisteredModels.modelFamilyList.getOrDefault(model_ID, RegisteredModels.ID_GENERIC);
			reflective_IDX = RegisteredModels.modelReflectivity.getOrDefault(model_ID, 1f);
		} else if(mdl instanceof RBox) {
			model_ID = RegisteredModels.ID_CUBE;
			group_ID = RegisteredModels.ID_CUBE;
			reflective_IDX = 1f;
		} else if(mdl instanceof RCylinder) {
			model_ID = RegisteredModels.ID_CYLINDER;
			group_ID = RegisteredModels.ID_CYLINDER;
			reflective_IDX = 1f;
		} else {
			model_ID = RegisteredModels.ID_GENERIC;
			group_ID = RegisteredModels.ID_GENERIC;
			reflective_IDX = 1f;
		}
		
		image_quality = q;
		selectAreas = loadCamSelectAreas();
	}
	
	@Override
	public Part clone() {
		return new CameraObject(appRef, this, image_quality);
	}
	
	public CamSelectArea getCamSelectArea(int i) {
		return selectAreas.get(i);
	}
	
	public int getModelGroupID() {
		return group_ID;
	}
	
	public int getModelID() {
		return model_ID;
	}
	
	public PGraphics getModelPreview(RMatrix m) {
		if(preview == null) {
			PGraphics img = appRef.createGraphics(150, 200, RobotRun.P3D);
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
			
			//img.scale(0.5f);
			
			this.getModel().draw(img);
			img.filter(PApplet.BLUR, 5*(1 - image_quality*image_quality));
			System.out.println(image_quality);
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
	
	public int getNumSelectAreas() {
		return selectAreas.size();
	}
	
	public float getReflectiveIndex() {
		return reflective_IDX;
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

	public PGraphics updateModelPreview(RMatrix m) {
		preview = null;
		return getModelPreview(m);
	}

	private ArrayList<CamSelectArea> loadCamSelectAreas() {
		ArrayList<CamSelectArea> selectAreas = new ArrayList<CamSelectArea>();
		
		if(RegisteredModels.modelAreasOfInterest.get(model_ID) != null) {
			for(CamSelectArea c: RegisteredModels.modelAreasOfInterest.get(model_ID)) {
				selectAreas.add(c.copy());
			}
		}
		
		return selectAreas;
	}
}
