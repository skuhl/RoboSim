package expression;

import core.RobotCamera;
import core.RobotRun;
import geom.WorldObject;

public class OperandCamObj extends Operand<WorldObject> implements BoolMath {
	public static final RobotCamera CAM = RobotRun.getInstance().getRobotCamera();
	
	public OperandCamObj(WorldObject v) {
		super(v, Operand.CAM_MATCH);
	}

	@Override
	public Boolean getBoolValue() {
		return CAM.isObjectInScene(value, RobotRun.getInstanceScenario());
	}

	@Override
	public Operand<WorldObject> clone() {
		return new OperandCamObj(value);
	}
	
}
