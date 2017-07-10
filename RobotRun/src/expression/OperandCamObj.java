package expression;

import core.RobotCamera;
import core.RobotRun;
import geom.WorldObject;

public class OperandCamObj extends Operand<WorldObject> implements BoolMath {
	public static final RobotCamera CAM = RobotRun.getInstance().getRobotCamera();
	
	public OperandCamObj() {
		super(null, Operand.CAM_MATCH);
	}
	
	public OperandCamObj(WorldObject v) {
		super(v, Operand.CAM_MATCH);
	}

	@Override
	public Boolean getBoolValue() {
		if(value != null) {
			return CAM.isObjectInScene(value, RobotRun.getInstanceScenario());
		} else {
			return false;
		}
	}

	@Override
	public Operand<WorldObject> clone() {
		return new OperandCamObj(value);
	}
	
	@Override
	public String toString() {
		if(value != null) {
			String objName = ((WorldObject)value).getName();
			return "Match[" + objName + "]";
		}
		else {
			return null;
		}
	}
	
	@Override
	public String[] toStringArray() {
		if(value != null) {
			String objName = ((WorldObject)value).getName();
			return new String[] { "Match[" + objName + "]" };
		}
		else {
			return new String[] {"Match[...]"};
		}
	}
}
