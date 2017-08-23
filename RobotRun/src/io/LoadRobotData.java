package io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import geom.Point;
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
public class LoadRobotData implements Runnable {
	
	private RoboticArm robotRef;
	private String tmpDirPath;
	
	/**
	 * TODO comment this
	 * 
	 * @param robotRef
	 * @param tmpDirPath
	 */
	public LoadRobotData(RoboticArm robotRef, String tmpDirPath) {
		this.robotRef = robotRef;
		this.tmpDirPath = tmpDirPath;
	}
	
	@Override
	public void run() {
		File srcDir = new File(String.format("%srobot%d/",
				tmpDirPath, robotRef.RID));
		
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			// No such directory exists
			return;
		}
		
		// Check for the programs.bin and programs directory
		File progDir = new File(tmpDirPath + String.format("robot%d/",
				robotRef.RID) + "programs");
		File progFile = new File(tmpDirPath + String.format(
				"robot%d/programs.bin", robotRef.RID));
		
		// If the programs directory does not exist
		if (!progDir.exists() || !progDir.isDirectory()) {
			// Load the Robot's programs, frames, and registers from their respective files
			if (progFile.exists() && progFile.isFile()) {
				loadProgramBytesOldest(progFile.getPath());	
			}
			
			loadFrameBytes(String.format("%s/frames.bin",
					srcDir.getAbsolutePath()));
			loadRegisterBytes(String.format("%s/registers.bin",
					srcDir.getAbsolutePath()));
			loadMacros(String.format("%s/macros.bin",
					srcDir.getAbsolutePath()));
			return;
		}
		
		File[] progFiles = progDir.listFiles();
		Program[] programs = new Program[progFiles.length];
		Thread[] loadThreads = new Thread[progFiles.length];
		// Initialize a thread for each program file
		for (int idx = 0; idx < progFiles.length; ++idx) {
			LoadProgramFile loader = new LoadProgramFile(robotRef, programs, idx,
					progFiles[idx]);
			loadThreads[idx] = new Thread(loader);
			loadThreads[idx].start();
		}
		
		// Load the Robot's frames, and registers from their respective files
		loadFrameBytes(String.format("%s/frames.bin",
				srcDir.getAbsolutePath()));
		loadRegisterBytes(String.format("%s/registers.bin",
				srcDir.getAbsolutePath()));
		
		/* Wait for each thread to finish and add the program once the thread
		 * has completed */
		for (int idx = 0; idx < loadThreads.length; ++idx) {
			Fields.waitForThread(loadThreads[idx]);
			robotRef.addProgram(programs[idx]);
		}
		
		// Load the robot's macros after its programs have been initialized
		loadMacros(String.format("%s/macros.bin", srcDir.getAbsolutePath()));
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param srcPath
	 * @return
	 */
	private int loadFrameBytes(String srcPath) {
		int idx = -1;
		File src = new File(srcPath);
		
		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			// Load Tool Frames
			int size = Math.max(0, Math.min(dataIn.readInt(), 10));
			
			for(idx = 0; idx < size; idx += 1) {
				DataManagement.loadTFrameData(robotRef.getToolFrame(idx),
						dataIn);
			}

			// Load User Frames
			size = Math.max(0, Math.min(dataIn.readInt(), 10));

			for(idx = 0; idx < size; idx += 1) {
				DataManagement.loadUFrameData(robotRef.getUserFrame(idx),
						dataIn);
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (Exception FNFEx) {
			System.err.printf("Current frame load method failed\n",
					src.getName());
			return loadFrameBytesOldest(srcPath);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param srcPath
	 * @return
	 */
	private int loadFrameBytesOldest(String srcPath) {
		int idx = -1;
		File src = new File(srcPath);
		
		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			// Load Tool Frames
			int size = Math.max(0, Math.min(dataIn.readInt(), 10));
			
			for(idx = 0; idx < size; idx += 1) {
				DataManagement.loadTFrameDataOldest(robotRef.getToolFrame(idx),
						dataIn);
			}

			// Load User Frames
			size = Math.max(0, Math.min(dataIn.readInt(), 10));

			for(idx = 0; idx < size; idx += 1) {
				DataManagement.loadUFrameDataOldest(robotRef.getUserFrame(idx),
						dataIn);
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not find src
			System.err.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Reached the end of src unexpectedly
			System.err.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// Error with reading from src
			System.err.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param filePath
	 */
	private void loadMacros(String filePath) {	
		try {
			File destDir = new File(filePath);
			FileInputStream in = new FileInputStream(destDir);
			DataInputStream dataIn = new DataInputStream(in);
			
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			
			int numMacros = dataIn.readInt();
			
			for (int i = 0; i < numMacros && !robotRef.atMacroCapacity();
					i += 1) {
				
				boolean isManual = dataIn.readBoolean();
				String progName = dataIn.readUTF();
				int keyNum = dataIn.readInt();
				
				Program p = robotRef.getProgram(progName);
				Macro m = robotRef.addMacro(p);
				m.setManual(isManual);
				robotRef.setKeyBinding(keyNum, m);
			}
			
			dataIn.close();
			in.close();
		}
		catch (Exception e) {
			System.err.println("Unable to load macros for robot " +
					robotRef.RID + "!");
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param srcPath
	 * @return
	 */
	private int loadProgramBytesOldest(String srcPath) {
		File src = new File(srcPath);

		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);
			// Read the number of programs stored in src
			int size = Math.max(0, Math.min(dataIn.readInt(),
					RoboticArm.PROG_NUM));
			
			while(size-- > 0) {
				// Read each program from src
				Program loadProg = DataManagement.loadProgram(robotRef,
						dataIn);
				robotRef.addProgram(loadProg);
			}

			dataIn.close();
			in.close();
			
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not locate src
			System.err.printf("%s does not exist!\n", src.getName());
			return 1;

		} catch (EOFException EOFEx) {
			// Reached the end of src unexpectedly
			System.err.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			return 2;

		} catch (IOException IOEx) {
			// An error occurred with reading from src
			System.err.printf("%s is corrupt!\n", src.getName());
			return 3;
			
		} catch (ClassCastException CCEx) {
			/* An error occurred with casting between objects while loading a
			 * program's instructions */
			System.err.printf("%s is corrupt!\n", src.getName());
			return 4;
			
		} catch (NegativeArraySizeException NASEx) {
			// Issue with loading program points
			System.err.printf("%s is corrupt!\n", src.getName());
			return 5;
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param srcPath
	 * @return
	 */
	private int loadRegisterBytes(String srcPath) {
		File src = new File(srcPath);
		
		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			int size = Math.max(0, Math.min(dataIn.readInt(), Fields.DPREG_NUM));

			// Load the Register entries
			while(size-- > 0) {
				// Each entry is saved after its respective index in REG
				int reg = dataIn.readInt();

				Float v = dataIn.readFloat();
				// Null values are saved as NaN
				if(Float.isNaN(v)) { v = null; }

				String c = dataIn.readUTF();
				// Null comments are saved as ""
				if(c.equals("")) { c = null; }
				
				DataRegister dReg = robotRef.getDReg(reg);
				
				if (dReg != null) {
					dReg.value = v;
					dReg.comment = c;
				}
			}

			size = Math.max(0, Math.min(dataIn.readInt(), Fields.DPREG_NUM));

			// Load the Position Register entries
			while(size-- > 0) {
				// Each entry is saved after its respective index in POS_REG
				int idx = dataIn.readInt();

				Point p = DataManagement.loadPoint(dataIn);
				String c = dataIn.readUTF();
				// Null comments are stored as ""
				if(c == "") { c = null; }
				boolean isCartesian = dataIn.readBoolean();
				
				PositionRegister pReg = robotRef.getPReg(idx);
				
				if (pReg != null) {
					pReg.point = p;
					pReg.comment = c;
					pReg.isCartesian = isCartesian;
				}
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located src
			System.err.printf("%s does not exist!\n", src.getName());
			return 1;

		} catch (EOFException EOFEx) {
			// Unexpectedly reached the end of src
			System.err.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			return 3;

		} catch (IOException IOEx) {
			// Error occrued while reading from src
			System.err.printf("%s is corrupt!\n", src.getName());
			return 2;
		}
	}
}
