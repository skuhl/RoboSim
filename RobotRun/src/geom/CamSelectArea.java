package geom;

import processing.core.PVector;

public class CamSelectArea {
	public final int area_id;
	private int state;
	
	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;
		
	public CamSelectArea(int id, float x1, float y1, float x2, float y2) {
		area_id = id;
		state = 0;
		
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
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
	
	public PVector getTopLeftBound() {
		return new PVector(x1, y1);
	}
	
	public PVector getTopRightBound() {
		return new PVector(x2, y2);
	}
	
	public CamSelectArea copy() {
		return new CamSelectArea(area_id, x1, y1, x2, y2);
	}
}
