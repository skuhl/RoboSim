package geom;

import java.util.ArrayList;

import camera.CamSelectArea;
import camera.CamSelectView;
import camera.RegisteredModels;
import core.RobotRun;
import global.RMath;
import processing.core.PGraphics;
import processing.core.PVector;
import window.WGUI;

public class CameraObject extends Part {
	
	public final int group_ID;
	public final float image_quality;
	public final float light_value;
	public final int model_ID;
	public final float reflective_IDX;
	
	private RobotRun appRef;
	private PGraphics preview;
	private ArrayList<CamSelectArea> selectAreas;
	
	public CameraObject(RobotRun appRef, Part p) {
		this(appRef, p, 1f, 1f);
	}
	
	public CameraObject(RobotRun appRef, Part p, float q, float l) {
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
		light_value = l;
		selectAreas = loadCamSelectAreas();
	}
	
	@Override
	public Part clone() {
		return new CameraObject(appRef, this, image_quality, light_value);
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
			PGraphics img = appRef.createGraphics(WGUI.imageWidth, WGUI.imageHeight, RobotRun.P3D);
			float[][] rMat = m.getDataF();
			
			PVector dim = this.getModel().getDims();
			
			RMatrix mat = this.getOrientation();
			PVector alignX = new PVector(Math.abs(mat.getEntryF(0, 0)*dim.x), 
										 Math.abs(mat.getEntryF(1, 0)*dim.y), 
										 Math.abs(mat.getEntryF(2, 0)*dim.z));
			PVector alignY = new PVector(Math.abs(mat.getEntryF(0, 1)*dim.x), 
										 Math.abs(mat.getEntryF(1, 1)*dim.y), 
										 Math.abs(mat.getEntryF(2, 1)*dim.z));
			PVector alignZ = new PVector(Math.abs(mat.getEntryF(0, 2)*dim.x), 
										 Math.abs(mat.getEntryF(1, 2)*dim.y), 
										 Math.abs(mat.getEntryF(2, 2)*dim.z));
			
			float dimX = alignX.x + alignX.y + alignX.z;
			float dimY = alignY.x + alignY.y + alignY.z;
			float dimZ = alignZ.x + alignZ.y + alignZ.z;
			
			//System.out.println("Object dimensions: " + dim.toString());
			//System.out.println("Apparent dimensions: " + dimX + ", " + dimY + ", " + dimZ);
			//System.out.println(mat.toString());
			
			img.beginDraw();
			img.ortho();
			img.background(255);

			img.translate(WGUI.imageWidth/2, WGUI.imageHeight/2, -dimZ - 20);
			
			img.applyMatrix(
					rMat[0][0], rMat[1][0], rMat[2][0], 0,
					rMat[0][1], rMat[1][1], rMat[2][1], 0,
					rMat[0][2], rMat[1][2], rMat[2][2], 0,
					0, 0, 0, 1
			);
			
			float light = 20 + 235 * light_value;
			img.directionalLight(light, light, light, 0, 0, 1);
			img.ambientLight(light, light, light);
			img.background(light);
			
			img.scale((float)Math.min((WGUI.imageWidth - 5)/dimX, (WGUI.imageHeight - 5)/dimY));
			this.getModel().draw(img);
			img.resetMatrix();
			
			PVector angles = RMath.matrixToEuler(mat);
			img.rotateZ(angles.z);
						
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
