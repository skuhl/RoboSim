package screen.select_lines;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectComment extends ST_ScreenLineSelect {

	public ScreenSelectComment(RobotRun r) {
		super(ScreenMode.SELECT_COMMENT, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select lines to comment/uncomment.");
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Done]";
		labels[4] = "";
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		r.getInstToEdit(robotRun.getActiveProg(), robotRun.getActiveInstIdx()).toggleCommented();
		robotRun.updatePendantScreen();
	}
	
	@Override
	public void actionF4() {
		robotRun.getScreenStack().pop();
		robotRun.updateInstructions();
	}
}
