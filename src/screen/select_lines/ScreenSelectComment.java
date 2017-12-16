package screen.select_lines;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectComment extends ST_ScreenLineSelect {

	public ScreenSelectComment(RobotRun r) {
		super(ScreenMode.SELECT_COMMENT, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx()).toggleCommented();
		robotRun.updatePendantScreen();
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "[Done]";
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Select lines to comment/uncomment.");
	}
}
