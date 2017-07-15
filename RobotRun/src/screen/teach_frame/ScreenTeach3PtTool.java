package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import frame.ToolFrame;
import geom.Point;
import processing.core.PGraphics;
import processing.core.PVector;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenTeach3PtTool extends ST_ScreenTeachPoints {
	
	private ToolFrame teachFrame;
	
	public ScreenTeach3PtTool(RobotRun r, ToolFrame frame) {
		super(ScreenMode.TEACH_3PT_TOOL, r);
		teachFrame = frame;
	}

	@Override
	protected String loadHeader() {
		return "TOOL 3PT METHOD";
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadFrameDetail(teachFrame));
	}

	@Override
	protected void loadOptions() {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		String out = (teachFrame.getPoint(0) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(0, 0, "First Approach Point: " + out));
		
		out = (teachFrame.getPoint(1) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(1, 0, "Second Approach Point: " + out));
		
		out = (teachFrame.getPoint(2) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(2, 0, "Third Approach Point: " + out));
		
		options.setLines(lines);
	}

	@Override
	public void actionEntr() {
		robotRun.createFrame(teachFrame, 0);
		robotRun.lastScreen();
	}
	
	@Override
	public void drawTeachPts(PGraphics g) {
		g.pushStyle();
		g.noFill();
		
		for (int idx = 0; idx < 3; ++idx){
			Point pt = teachFrame.getPoint(idx);
			// Draw each initialized teach point
			if (pt != null) {
				PVector pos = pt.position;
				
				g.stroke(getPtColorForTool(idx));
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
		return teachFrame.isComplete(0);
	}

	@Override
	public void setTeachPoint(Point pt, int idx) {
		teachFrame.setPoint(pt, idx);
	}
}
