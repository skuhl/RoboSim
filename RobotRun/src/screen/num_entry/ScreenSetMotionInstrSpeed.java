package screen.num_entry;

import core.RobotRun;
import global.Fields;
import global.RMath;
import programming.Instruction;
import programming.MotionInstruction;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetMotionInstrSpeed extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrSpeed(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SET_MINST_SPD, prevState, r);
	}

	@Override
	protected void loadOptions() {
		Instruction inst = robotRun.getActiveInstruction();

		if (inst instanceof MotionInstruction) {
			MotionInstruction mInst = (MotionInstruction)inst;
			String workingTextSuffix;
			
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				workingTextSuffix = "%";
			} else {
				workingTextSuffix = "mm/s";
			}
			
			options.addLine("Enter desired speed:");
			options.addLine(workingText + workingTextSuffix);
			
		} else {
			String line = String.format("Invalid instruction: %s", inst);
			options.addLine(line);
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		MotionInstruction m = (MotionInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		int motionType = m.getMotionType();
		
		try {
			float tempSpeed = Float.parseFloat(workingText.toString());
			
			if (motionType == Fields.MTYPE_LINEAR ||
					motionType == Fields.MTYPE_CIRCULAR) {
				
				tempSpeed /= RoboticArm.motorSpeed;
				
			} else {
				tempSpeed /= 100f;
			}
			
			m.setSpdMod(RMath.clamp(tempSpeed, 0.01f, 1f));
			
		} catch (NumberFormatException NFEx) {/*Invalid input*/}
		
		robotRun.lastScreen();
	}

}
