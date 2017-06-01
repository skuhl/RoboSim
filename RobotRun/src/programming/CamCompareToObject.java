package programming;

import java.util.ArrayList;

import core.RobotCamera;
import core.RobotRun;
import geom.WorldObject;

public class CamCompareToObject extends Instruction {
	private int tgtIdx;
	private ArrayList<WorldObject> matches;
	
	
	public CamCompareToObject(int tgt) {
		tgtIdx = tgt;
	}
	
	public ArrayList<WorldObject> getMatches() {
		return matches;
	}
	
	public int execute() {
		RobotCamera c = RobotRun.getInstance().getRobotCamera();
		matches = c.matchTaughtObject(tgtIdx, RobotRun.getInstanceScenario());
			
		return 0;
	}
}
