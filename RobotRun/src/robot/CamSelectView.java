package robot;

import processing.core.PVector;

public class CamSelectView {
	public final int viewAlignment;
	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;
	
	public CamSelectView(String align, float x1, float y1, float x2, float y2) {
		switch(align) {
		case "F": viewAlignment = 0; break; //Front/ -Z
		case "R": viewAlignment = 1; break; //Rear/ +Z
		case "T": viewAlignment = 2; break; //Top/ +Y
		case "B": viewAlignment = 3; break; //Bottom/ -Y
		case "Lt": viewAlignment = 4; break; //Left/ +X
		case "Rt": viewAlignment = 5; break; //Right/ -X
		default: viewAlignment = -1; break; //Invalid
		}
		
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	private CamSelectView(int align, float x1, float x2, float y1, float y2) {
		viewAlignment = align;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public PVector getTopLeftBound() {
		return new PVector(x1, y1);
	}
	
	public PVector getBottomRightBound() {
		return new PVector(x2, y2);
	}
	
	public PVector getCenter() {
		return new PVector(x1/2 + x2/2, y1/2 + y2/2);
	}
	
	public float getWidth() {
		return x2 - x1;
	}
	
	public float getHeight() {
		return y2 - y1;
	}

	public int getViewAlign() {
		return viewAlignment;
	}
	
	public CamSelectView copy() {
		return new CamSelectView(viewAlignment, x1, y1, x2, y2);
	}
}
