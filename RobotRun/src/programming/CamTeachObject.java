package programming;

import core.RobotCamera;
import core.RobotRun;

public class CamTeachObject extends Instruction {
	
	public CamTeachObject() {
		
	}
	
	public int execute() {
		RobotCamera c = RobotRun.getInstance().getRobotCamera();
		if(c != null) {
			c.teachObjectToCamera(RobotRun.getInstanceScenario());
			return 0;
		}
		
		return 1;
	}
}
