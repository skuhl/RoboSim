package geom;

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
