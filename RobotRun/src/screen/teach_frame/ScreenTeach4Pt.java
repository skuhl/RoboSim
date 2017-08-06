package screen.teach_frame;

import java.util.ArrayList;

import core.RobotRun;
import frame.UserFrame;
import geom.Point;
import global.DataManagement;
import global.Fields;
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
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		boolean success = teachFrame.teach4Pt();
		
		if (success) {
			// Set the updated frame
			r.setActiveUserFrame(frameIdx);
			DataManagement.saveRobotData(r, 2);
			robotRun.lastScreen();
			
		} else {
			Fields.setMessage("Invalid teach points");
		}
	}

	@Override
	public void drawTeachPts(PGraphics g) {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		g.pushStyle();
		g.noFill();
		
		for (int idx = 0; idx < 4; ++idx){
			Point pt = teachFrame.getTeachPt(idx);
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
		
		return teachFrame.getTeachPt(idx);
	}

	@Override
	public boolean readyToTeach() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		return teachFrame.is4PtComplete();
	}

	@Override
	public void setTeachPoint(Point pt, int idx) {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		
		teachFrame.setTeachPt(pt, idx);
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadFrameDetail(r.getUserFrame(frameIdx)));
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadOptions() {
		RoboticArm r = robotRun.getActiveRobot();
		UserFrame teachFrame = r.getUserFrame(frameIdx);
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		String out = (teachFrame.getTeachPt(0) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(0, 0, "Orient Origin Point: " + out));
		
		out = (teachFrame.getTeachPt(1) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(1, 0, "X Axis Point: " + out));
		
		out = (teachFrame.getTeachPt(2) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(2, 0, "Y Axis Point: " + out));
		
		out = (teachFrame.getTeachPt(3) == null) ? "UNINIT" : "RECORDED";
		lines.add(new DisplayLine(3, 0, "Origin: " + out));
		
		options.setLines(lines);
	}
}
