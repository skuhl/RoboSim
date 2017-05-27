package geom;

import processing.core.PVector;

public class CamSelectArea {
	public final int area_id;
	private CamSelectView[] selectViews;
	private int state;
	
		
	public CamSelectArea(int id, CamSelectView... views) {
		area_id = id;
		selectViews = new CamSelectView[6];
		state = 0;
		
		for(CamSelectView v: views) {
			if(v != null) {
				selectViews[v.getViewAlign()] = v.copy();
			}
		}
	}
	
	public CamSelectView getView(RMatrix m) {
		float[][] data = m.getFloatData();
		PVector look = new PVector(data[0][2], data[1][2], data[2][2]);
		
		float dotX = new PVector(1, 0, 0).dot(look);
		float dotY = new PVector(0, 1, 0).dot(look);
		float dotZ = new PVector(0, 0, 1).dot(look);
		
		if(dotZ < 0 && dotZ*dotZ > 0.9) { return selectViews[0]; }
		if(dotZ > 0 && dotZ*dotZ > 0.9) { return selectViews[1]; }
		if(dotY > 0 && dotY*dotY > 0.9) { return selectViews[2]; }
		if(dotY < 0 && dotY*dotY > 0.9) { return selectViews[3]; }
		if(dotX > 0 && dotX*dotX > 0.9) { return selectViews[4]; }
		if(dotX < 0 && dotX*dotX > 0.9) { return selectViews[5]; }
		
		return null;
		
	}
	
	public CamSelectArea emphasizeArea() {
		state = 1;
		return this;
	}
	
	public CamSelectArea ignoreArea() {
		state = -1;
		return this;
	}
	
	public boolean isEmphasized() {
		return (state == 1);
	}
	
	public boolean isIgnored() {
		return (state == -1);
	}
	
	public CamSelectArea copy() {
		return new CamSelectArea(area_id, selectViews);
	}
}
