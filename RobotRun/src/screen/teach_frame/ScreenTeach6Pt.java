package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import frame.ToolFrame;
import geom.Point;
import global.DataManagement;
import global.Fields;
import processing.core.PGraphics;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenTeach6Pt extends ST_ScreenTeachPoints {
	
	/**
	 * TODO comment this
	 * 
	 * @param r
	 * @param idx
	 * @return
	 */
	private static String loadHeader(RoboticArm r, int idx) {
		return String.format("TOOL: %s 6PT METHOD", r.toolLabel(idx));
	}
	
	public ScreenTeach6Pt(RobotRun r, int tFrameIdx) {
		super(ScreenMode.TEACH_6PT, loadHeader(r.getActiveRobot(), tFrameIdx),
				r, 4, 10, 20, 6, 10, 80, tFrameIdx);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		
		Point RP = r.getFacePlatePoint();
		boolean success = teachFrame.teach6Pt(RP);
		
		if (success) {
			// Set the updated frame
			r.setActiveToolFrame(frameIdx);
			DataManagement.saveRobotData(r, 2);
			robotRun.lastScreen();
			
		} else {
			Fields.setMessage("Invalid teach points");
		}
	}

	@Override
	public void drawTeachPts(PGraphics g) {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		g.pushStyle();
		g.noFill();
		
		for (int idx = 0; idx < 6; ++idx){
			Point pt = teachFrame.getTeactPt(idx);
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
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		
		return teachFrame.getTeactPt(idx);
	}

	@Override
	public boolean readyToTeach() {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		
		return teachFrame.is6PtComplete();
	}

	@Override
	public void setTeachPoint(Point pt, int idx) {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		
		teachFrame.setTeachPt(pt, idx);
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r.getToolFrame(frameIdx)));
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		ToolFrame teachFrame = r.getToolFrame(frameIdx);
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		String out = (teachFrame.getTeactPt(0) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(0, 0, "First Approach Point: " + out));
		
		out = (teachFrame.getTeactPt(1) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(1, 0, "Second Approach Point: " + out));
		
		out = (teachFrame.getTeactPt(2) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(2, 0, "Third Approach Point: " + out));
		
		out = (teachFrame.getTeactPt(3) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(3, 0, "Orient Origin Point: " + out));
		
		out = (teachFrame.getTeactPt(4) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(4, 0, "X Axis Point: " + out));
		
		out = (teachFrame.getTeactPt(5) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(5, 0, "Y Axis Point: " + out));
		
		options.setLines(lines);
	}
}
