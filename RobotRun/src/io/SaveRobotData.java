package io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import global.Fields;
import programming.Macro;
import programming.Program;
import regs.DataRegister;
import regs.PositionRegister;
import robot.RoboticArm;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class SaveRobotData implements Runnable {

	private RoboticArm robotRef;
	private String tmpDirPath;
	private int dataFlag;
	
	/**
	 * TODO comment this
	 * 
	 * @param robotRef
	 * @param tmpDirPath
	 * @param dataFlag
	 */
	public SaveRobotData(RoboticArm robotRef, String tmpDirPath,
			int dataFlag) {
		
		this.robotRef = robotRef;
		this.dataFlag = dataFlag;
		this.tmpDirPath = tmpDirPath;
	}
	
	@Override
	public void run() {
		File destDir = new File(String.format("%srobot%d/", tmpDirPath,
				robotRef.RID));
		
		// Validate the robot save directory
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		
		final boolean savePrograms = (dataFlag & 0x1) != 0;
		Thread[] progThreads;
		
		if (savePrograms) {
			// Remove programs.bin file and validate the programs directory
			File oldSaveFile = new File(String.format("%s/programs.bin",
					destDir.getAbsolutePath()));
			File progDir = new File(String.format("%s/programs",
					destDir.getAbsolutePath()));
			
			if (oldSaveFile.exists()) {
				oldSaveFile.delete();
			}
			
			if (destDir.exists() && destDir.isFile()) {
				destDir.delete();
			}
			
			if (!destDir.exists()) {
				destDir.mkdir();
				System.err.printf("Successfully created %s.\n",
						destDir.getName());
			}
			
			/* Save each program in a separate file within the given robot's program
			 * directory */
			progThreads = new Thread[robotRef.numOfPrograms()];
			// Run a thread for saving each program
			for (int idx = 0; idx < progThreads.length; ++idx) {
				Program p = robotRef.getProgram(idx);
				File dest = new File( String.format("%s/%s.bin", progDir,
						p.getName()) );
				progThreads[idx] = new Thread(new SaveProgramFile(p, dest));
				progThreads[idx].start();
			}
			
		} else {
			progThreads = null;
		}
		
		if ((dataFlag & 0x2) != 0) {
			// Save the robot's frames
			saveFrameBytes(String.format("%s/frames.bin", destDir.getAbsolutePath()));
		}
		
		if ((dataFlag & 0x4) != 0) {
			// Save the robot's registers
			saveRegisterBytes(String.format("%s/registers.bin", destDir.getAbsolutePath()));
		}
		
		if ((dataFlag & 0x8) != 0) {
			// Save the robot's registers
			saveMacros(String.format("%s/macros.bin", destDir.getAbsolutePath()));
		}
		
		if (savePrograms) {
			// Wait for program threads to finish
			for (int idx = 0; idx < progThreads.length; ++idx) {
				Fields.waitForThread(progThreads[idx]);
			}
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param destPath
	 * @return
	 */
	private int saveFrameBytes(String destPath) {
		File dest = new File(destPath);

		try {
			// Create dest if it does not already exist
			if(!dest.exists()) {
				dest.createNewFile();
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			// Save Tool Frames
			dataOut.writeInt(Fields.FRAME_NUM);
			for (int idx = 0; idx < Fields.FRAME_NUM; ++idx) {
				DataManagement.saveToolFrame(robotRef.getToolFrame(idx),
						dataOut);
			}
			
			// Save User Frames
			dataOut.writeInt(Fields.FRAME_NUM);
			for (int idx = 0; idx < Fields.FRAME_NUM; ++idx) {
				DataManagement.saveUserFrame(robotRef.getUserFrame(idx),
						dataOut);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not find dest
			System.err.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// Error with writing to dest
			System.err.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param filePath
	 */
	private void saveMacros(String filePath) {
		File destDir = new File(filePath);
		
		try {		
			// Create dest if it does not already exist
			if(!destDir.exists()) {      
				try {
					destDir.createNewFile();
					System.err.printf("Successfully created %s.\n", destDir.getName());
					
				} catch (IOException IOEx) {
					System.err.printf("Could not create %s ...\n", destDir.getName());
					IOEx.printStackTrace();
					return;
				}
			}
						
			FileOutputStream out = new FileOutputStream(destDir);
			DataOutputStream dataOut = new DataOutputStream(out);
			
			dataOut.writeInt(robotRef.numOfMacros());
			
			for (int idx = 0; idx < robotRef.numOfMacros(); ++idx) {
				Macro m = robotRef.getMacro(idx);
				Program p = m.getProg();
				
				dataOut.writeBoolean(m.isManual());
				
				if (p == null) {
					dataOut.writeUTF("");
					
				} else {
					dataOut.writeUTF(p.getName());
				}
				
				dataOut.writeInt(m.getKeyNum());
			}
			
			dataOut.close();
			out.close();
		} 
		catch (Exception e) {
			System.err.println("Unable to save macros for robot " +
					robotRef.RID + "!");
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param destPath
	 * @return
	 */
	private int saveRegisterBytes(String destPath) {
		File dest = new File(destPath);
		
		try {
			// Create dest if it does not already exist
			if (!dest.exists()) {
				dest.createNewFile();
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			int numOfREntries = 0,
				numOfPREntries = 0;

			ArrayList<Integer> initializedR = new ArrayList<>(),
					initializedPR = new ArrayList<>();

			// Count the number of initialized entries and save their indices
			for(int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
				DataRegister dReg = robotRef.getDReg(idx);
				PositionRegister pReg = robotRef.getPReg(idx);
				
				if (dReg.value != null || dReg.comment != null) {
					initializedR.add(idx);
					++numOfREntries;
				}

				if (pReg.point != null || pReg.comment != null) {
					initializedPR.add(idx);
					++numOfPREntries;
				}
			}

			dataOut.writeInt(numOfREntries);
			// Save the Register entries
			for(Integer idx : initializedR) {
				DataRegister dReg = robotRef.getDReg(idx);
				dataOut.writeInt(idx);

				if (dReg.value == null) {
					// save for null Float value
					dataOut.writeFloat(Float.NaN);
					
				} else {
					dataOut.writeFloat(dReg.value);
				}

				if (dReg.comment == null) {
					dataOut.writeUTF("");
					
				} else {
					dataOut.writeUTF(dReg.comment);
				}
			}

			dataOut.writeInt(numOfPREntries);
			// Save the Position Register entries
			for(Integer idx : initializedPR) {
				PositionRegister pReg = robotRef.getPReg(idx);
				dataOut.writeInt(idx);
				DataManagement.savePoint(pReg.point, dataOut);

				if (pReg.comment == null) {
					dataOut.writeUTF("");
					
				} else {
					dataOut.writeUTF(pReg.comment);
				}

				dataOut.writeBoolean(pReg.isCartesian);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located dest
			System.err.printf("%s does not exist!\n", dest.getName());
			return 1;

		} catch (IOException IOEx) {
			// Error occured while reading from dest
			System.err.printf("%s is corrupt!\n", dest.getName());
			return 2;
		}
	}
}
