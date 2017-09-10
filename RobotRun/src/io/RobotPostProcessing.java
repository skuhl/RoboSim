package io;

import java.util.ArrayList;

import core.RobotRun;
import geom.Scenario;
import programming.CallInstruction;
import programming.CamMoveToObject;
import programming.IfStatement;
import programming.Instruction;
import programming.Program;
import programming.SelectStatement;
import robot.RoboticArm;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class RobotPostProcessing implements Runnable {
	
	private RobotRun appRef;
	private RoboticArm robotRef;
	private int progIdx;
	
	/**
	 * TODO comment this
	 * 
	 * @param appRef
	 * @param robotRef
	 * @param progIdx
	 */
	public RobotPostProcessing(RobotRun appRef, RoboticArm robotRef,
			int progIdx) {
		
		this.appRef = appRef;
		this.robotRef = robotRef;
		this.progIdx = progIdx;
	}
	
	@Override
	public void run() {
		Program prog = robotRef.getProgram(progIdx);
		
		for (int idx = 0; idx < prog.getNumOfInst(); ++idx) {
			Instruction inst = prog.getInstAt(idx);
			
			if (inst instanceof CallInstruction) {
				// Update a top call instruction
				CallInstruction cInst = (CallInstruction)inst;
				RoboticArm tgtDevice = appRef.getRobot(cInst.getLoadedID());
				String tgtName = cInst.getLoadedName();
				
				cInst.setTgtDevice(tgtDevice);
				if (tgtDevice != null && tgtName != null) {
					Program tgt = tgtDevice.getProgram(tgtName);
					cInst.setProg(tgt);
				}
				
			} else if (inst instanceof SelectStatement) {
				// Update call instructions in a select statement
				SelectStatement stmt = (SelectStatement)inst;
				ArrayList<Instruction> instList = stmt.getInstrs();
				
				for (Instruction caseInst : instList) {
					
					if (caseInst instanceof CallInstruction) {
						CallInstruction cInst = (CallInstruction)caseInst;
						RoboticArm tgtDevice = appRef.getRobot(cInst.getLoadedID());
						String tgtName = cInst.getLoadedName();
						
						cInst.setTgtDevice(tgtDevice);
						if (tgtDevice != null && tgtName != null) {
							Program tgt = tgtDevice.getProgram(tgtName);
							cInst.setProg(tgt);
						}
					}
				}
				
			} else if (inst instanceof IfStatement) {
				// Update call instructions in a if statement
				IfStatement stmt = (IfStatement)inst;
				Instruction subInst = stmt.getInstr();
				
				if (subInst instanceof CallInstruction) {
					CallInstruction cInst = (CallInstruction)subInst;
					RoboticArm tgtDevice = appRef.getRobot(cInst.getLoadedID());
					String tgtName = cInst.getLoadedName();
					
					cInst.setTgtDevice(tgtDevice);
					if (tgtDevice != null && tgtName != null) {
						Program tgt = tgtDevice.getProgram(tgtName);
						cInst.setProg(tgt);
					}
				}
				
			} else if (inst instanceof CamMoveToObject) {
				// Update a camera motion instruction
				CamMoveToObject cMInst = (CamMoveToObject)inst;
				Scenario scene = appRef.getScenario(cMInst.getLoadedSceneName());
				cMInst.setScene(scene);
			}
		}
	}

}
