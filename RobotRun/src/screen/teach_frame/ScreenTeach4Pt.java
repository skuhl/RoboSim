package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import frame.UserFrame;
import geom.Point;
import processing.core.PGraphics;
import processing.core.PVector;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenTeach4Pt extends ST_ScreenTeachPoints {
	
	private UserFrame teachFrame;
	
	public ScreenTeach4Pt(RobotRun r, UserFrame uFrame) {
		super(ScreenMode.TEACH_4PT, r);
		teachFrame = uFrame;
	}

	@Override
	protected String loadHeader() {
		return "USER 4PT METHOD";
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadFrameDetail(teachFrame));
	}

	@Override
	protected void loadOptions() {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		String out = (teachFrame.getPoint(0) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(3, 0, "Orient Origin Point: " + out));
		
		out = (teachFrame.getPoint(1) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(4, 0, "X Axis Point: " + out));
		
		out = (teachFrame.getPoint(2) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(5, 0, "Y Axis Point: " + out));
		
		out = (teachFrame.getPoint(2) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(5, 0, "Origin: " + out));
		
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(teachFrame, 1);
		robotRun.lastScreen();
	}

	@Override
	public void drawTeachPts(PGraphics g) {
		g.pushStyle();
		g.noFill();
		
		for (int idx = 0; idx < 4; ++idx){
			Point pt = teachFrame.getPoint(idx);
			// Draw each initialized teach point
			if (pt != null) {
				PVector pos = pt.position;
				
				g.stroke(getPtColorForUser(idx));
				g.pushMatrix();
				g.translate(pos.x, pos.y, pos.z);
				g.sphere(3);
				g.popMatrix();
			}
		}
		
		g.popStyle();
	}

	@Override
	public Point getTeachPoint(int idx) {
		return teachFrame.getPoint(idx);
	}

	@Override
	public boolean readyToTeach() {
		return teachFrame.isComplete(1);
	}

	@Override
	public void setTeachPoint(Point pt, int idx) {
		teachFrame.setPoint(pt, idx);
	}
}
