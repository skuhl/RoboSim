package expression;

import camera.RobotCamera;
import core.Pointer;
import geom.CameraObject;
import geom.Scenario;
import geom.WorldObject;

public class OperandCamObj extends Operand<CameraObject> implements BoolMath {
	private static RobotCamera camRef;
	private static Pointer<Scenario> scenarioRef;
	
	public OperandCamObj() {
		super(null, Operand.CAM_MATCH);
	}
	
	public OperandCamObj(CameraObject v) {
		super(v, Operand.CAM_MATCH);
	}

	@Override
	public Boolean getBoolValue() {
		
		if (value != null) {
			return camRef.isMatchVisible(value, scenarioRef.get());
			
		} else {
			return false;
		}
	}

	@Override
	public Operand<CameraObject> clone() {
		return new OperandCamObj(value);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param ref
	 */
	public static void setCamRef(RobotCamera ref) {
		camRef = ref;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param ref
	 */
	public static void setScenarioRef(Pointer<Scenario> ref) {
		scenarioRef = ref;
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
