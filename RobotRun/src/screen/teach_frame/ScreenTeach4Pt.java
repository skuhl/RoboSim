package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import frame.UserFrame;
import geom.Point;
import processing.core.PGraphics;
import processing.core.PVector;
import robot.RoboticArm;
import screen.ScreenMode;
import ui.DisplayLine;

public class ScreenTeach4Pt extends ST_ScreenTeachPoints {
	
	public ScreenTeach4Pt(RobotRun r, int uFrameIdx) {
		super(ScreenMode.TEACH_4PT, String.format("USER: %d 4PT METHOD",
				uFrameIdx + 1), r, uFrameIdx);
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r.getUserFrame(frameIdx)));
	}

	@Override
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
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
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		// TODO refactor this
		teachFrame.setFrame(1);
		
		robotRun.lastScreen();
	}

	@Override
	public void drawTeachPts(PGraphics g) {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
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
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		return teachFrame.getPoint(idx);
	}

	@Override
	public boolean readyToTeach() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		return teachFrame.isComplete(1);
	}

	@Override
	public void setTeachPoint(Point pt, int idx) {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		teachFrame.setPoint(pt, idx);
	}
}
