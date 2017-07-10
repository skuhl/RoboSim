package global;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;

import core.RobotRun;
import core.Scenario;
import expression.AtomicExpression;
import expression.BoolMath;
import expression.Expression;
import expression.ExpressionElement;
import expression.FloatMath;
import expression.Operand;
import expression.OperandBool;
import expression.OperandCamObj;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandGeneric;
import expression.OperandIOReg;
import expression.OperandPReg;
import expression.OperandPRegIdx;
import expression.OperandPoint;
import expression.OperandRegister;
import expression.Operator;
import expression.PointMath;
import expression.RobotPoint;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.ComplexShape;
import geom.CoordinateSystem;
import geom.DimType;
import geom.Fixture;
import geom.LoadedPart;
import geom.MyPShape;
import geom.Part;
import geom.Point;
import geom.RBox;
import geom.RCylinder;
import geom.RMatrix;
import geom.RQuaternion;
import geom.RShape;
import geom.WorldObject;
import processing.core.PVector;
import programming.CallInstruction;
import programming.CamMoveToObject;
import programming.FrameInstruction;
import programming.IOInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.LabelInstruction;
import programming.Macro;
import programming.PosMotionInst;
import programming.Program;
import programming.RegisterStatement;
import programming.SelectStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;

/**
 * Manages all the saving and loading of the program data to and from files.
 * All fields and methods are static, so no instance of the class is
 * necessary.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public abstract class DataManagement {
	
	private static String dataDirPath, errDirPath, tmpDirPath, scenarioDirPath;
	
	static {
		dataDirPath = null;
		errDirPath = null;
		tmpDirPath = null;
		scenarioDirPath = null;
	}
	
	/**
	 * Prints the given error's stack trace to a log file in the err sub
	 * directory. The file's name is the month-day-year-hour-minute the
	 * error occured.
	 * 
	 * @param Ex	the error for which to print the stack trace
	 */
	public static void errLog(Exception Ex) {
		try {
			File errDir = new File(errDirPath);
			// create the err subdirectory
			if (!errDir.exists()) {
				errDir.mkdir();
			}
			
			LocalDateTime now = LocalDateTime.now();
			// Use current month, day, year, hour, and minute as file name
			String time = String.format("%s%02d-%02d-%d-%02d-%02d.log", errDirPath,
					now.getMonthValue(), now.getDayOfMonth(), now.getYear(),
					now.getHour(), now.getMinute());
			
			File errLog = new File(time);
			
			if (!errLog.exists()) {
				errLog.createNewFile();
			}
			
			PrintWriter out = new PrintWriter(errLog);
			Ex.printStackTrace(out);
			out.close();
			
		} catch (IOException IOEx) {
			// Could not create error log file
			IOEx.printStackTrace();
		}
	}
	
	/**
	 * Returns a list of all the names of files in the data sub directory
	 * with the .stl file extension.
	 * 
	 * @return	A list of model files
	 */
	public static ArrayList<String> getDataFileNames() {
		File data = new File(dataDirPath);
		
		if (!data.exists() || data.isFile()) {
			// Missing data directory
			return null;
		}
		// Search for all .stl files
		File[] dataFiles = data.listFiles();
		ArrayList<String> fileNames = new ArrayList<>(dataFiles.length);
		
		for (File file : dataFiles) {
			String name = file.getName();
			// Check file extension and type
			if (file.isFile() && (name.endsWith(".stl") || name.endsWith(".STL"))) {
				fileNames.add(name);
			}
		}
		
		return fileNames;
	}
	
	// Must be called when RobotRun starts!!!!
	public static void initialize(RobotRun process) {
		dataDirPath = process.sketchPath("data\\");
		errDirPath = process.sketchPath("err\\");
		tmpDirPath = process.sketchPath("tmp\\");
		scenarioDirPath = process.sketchPath(tmpDirPath + "scenarios\\");
	}
	
	public static void loadState(RobotRun process) {
		loadScenarioBytes(process, scenarioDirPath);
		loadRobotData(process.getRobot(0));
		loadRobotData(process.getRobot(1));
		
		for (int rdx = 0; rdx < 2; ++rdx) {
			robotPostProcessing(process.getRobot(rdx), process);
		}
	}
	
	/**
	 * Removes the save file for the scenario with the given name.
	 * 
	 * @param name	The name of the scenario, of which to remove the back file
	 */
	public static void removeScenario(String name) {
		
		File f = new File(DataManagement.scenarioDirPath + name + ".bin");
		
		try {
			if (f.exists() && f.isFile()) {
				f.delete();
			}
			
		} catch (SecurityException SEx) {
			// Issue with file permissions
			SEx.printStackTrace();
		}
	}
	
	public static int saveRobotData(RoboticArm robot, int dataFlag) {
		validateTmpDir();
		File destDir = new File( String.format("%srobot%d/", tmpDirPath, robot.RID) );
		
		// Initialize and possibly create the robot directory
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		
		if ((dataFlag & 0x1) != 0) {
			// Save the robot's programs
			saveProgramBytes(robot, String.format("%s/programs.bin", destDir.getAbsolutePath()));
			exportProgsToTxt(robot, destDir.getAbsolutePath());
		}
		
		if ((dataFlag & 0x2) != 0) {
			// Save the robot's frames
			saveFrameBytes(robot, String.format("%s/frames.bin", destDir.getAbsolutePath()));
		}
		
		if ((dataFlag & 0x4) != 0) {
			// Save the robot's registers
			saveRegisterBytes(robot, String.format("%s/registers.bin", destDir.getAbsolutePath()));
		}
		
		if ((dataFlag & 0x8) != 0) {
			// Save the robot's registers
			saveMacros(robot, String.format("%s/macros.bin", destDir.getAbsolutePath()));
		}
		
		return 0;
	}
	
	public static void saveScenarios(RobotRun process) {
		validateTmpDir();
		
		Scenario as = process.getActiveScenario();
		saveScenarioBytes(process.getScenarios(), (as == null) ? null : as.getName(),
				scenarioDirPath);
	}
	
	public static void saveState(RobotRun process) {
		validateTmpDir();
		saveScenarioBytes(process.getScenarios(), (process.getActiveScenario() == null) ?
				null : process.getActiveScenario().getName(), scenarioDirPath);
		saveRobotData(process.getRobot(0), 15);
		saveRobotData(process.getRobot(1), 15);
	}
	
	public static void exportProgsToTxt(RoboticArm r, String directory) {
		for(int i = 0; i < r.numOfPrograms(); i += 1) {
			Program p = r.getProgram(i);
			File textfile = new File(directory + "/" + p.getName() + ".txt");
			
			try {
				PrintWriter out = new PrintWriter(textfile);
				out.write(p.getName() + ":");
				out.write(System.lineSeparator());
				out.write(System.lineSeparator());
				
				for(int j = 0; j < p.getNumOfInst(); j += 1) {
					Instruction instr = p.get(j);
					String[] text = instr.toStringArray();
					
					out.write((j + 1) + ") ");
					
					for(int k = 0; k < text.length; k += 1) {
						String str = text[k];
						
						if(str.compareTo("\n") != 0) {
							out.write(str + " ");
						}
						
						if(instr instanceof SelectStatement && k >= 5 && (k - 1) % 4 == 0 && k < text.length - 1) {
							out.write(System.lineSeparator());
							out.write("    ");
						}
					}
					
					out.write(System.lineSeparator());
				}
				
				out.write("[END]");
				out.close();
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static double[][] load2DDoubleArray(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read the length of the list
		int numOfRows = in.readInt(),
				numOfCols = in.readInt();
		double[][] list = new double[numOfRows][numOfCols];
		// Read each value of the list
		for (int row = 0; row < list.length; ++row) {
			for (int col = 0; col < list[0].length; ++col) {
				list[row][col] = in.readDouble();
			}
		}

		return list;
	}

	private static ExpressionElement loadExpressionElement(RoboticArm robot,
			DataInputStream in) throws IOException, ClassCastException {
		ExpressionElement ee = null;
		
		byte nullFlag = in.readByte();

		if (nullFlag == 1) {
			// Read in an operator
			int opFlag = in.readInt();
			ee = Operator.getOpFromID(opFlag);

		} else if (nullFlag == 2) {
			// Read in each expression element
			ArrayList<ExpressionElement> exprElements = new ArrayList<>();
			
			int len = in.readInt();
			
			for (int idx = 0; idx < len; ++idx) {
				// Read in each element of the expression
				ExpressionElement temp = loadExpressionElement(robot, in);
				exprElements.add(temp);
			}
	
			ee = new Expression(exprElements);
			
		} else if (nullFlag == 3) {
			// Read in an atomic expression operand
			Operand<?> a0 = (Operand<?>)loadExpressionElement(robot, in);
			Operand<?> a1 = (Operand<?>)loadExpressionElement(robot, in);
			Operator op = (Operator)loadExpressionElement(robot, in);

			ee = new AtomicExpression(a0, a1, op);

		} else if (nullFlag == 4) {
			// Read in a normal operand
			int opType = in.readInt();

			if (opType == Operand.FLOAT) {
				// Constant float
				Float val = in.readFloat();
				ee = new OperandFloat(val);

			} else if (opType == Operand.BOOL) {
				// Constant boolean
				Boolean val = in.readBoolean();
				ee = new OperandBool(val);

			} else if (opType == Operand.DREG ||
					opType == Operand.IOREG ||
					opType == Operand.PREG ||
					opType == Operand.PREG_IDX) {
				// Note: the register value of the operand is set to null!

				// Data, Position, or IO register
				Integer rdx = in.readInt();

				if (opType == Operand.DREG) {
					// Data register
					if(rdx == -1) {
						ee = new OperandDReg();
					} else {
						ee = new OperandDReg(robot.getDReg(rdx));
					}
				} else if (opType == Operand.PREG) {
					// Position register
					if(rdx == -1) {
						ee = new OperandPReg();
					} else {
						ee = new OperandPReg(robot.getPReg(rdx));
					}
				} else if (opType == Operand.PREG_IDX) {
					// Specific portion of a point
					Integer pdx = in.readInt();
					if(rdx == -1) {
						ee = new OperandPRegIdx();
					} else {
						ee = new OperandPRegIdx(robot.getPReg(rdx), pdx);
					}
				} else if (opType == Operand.IOREG) {
					// I/O register
					if(rdx == -1) {
						ee = new OperandIOReg();
					} else {
						ee = new OperandIOReg(robot.getIOReg(rdx)); 
					}
					
				} else {
					ee = new OperandGeneric();
				}

			} else if (opType == Operand.POSTN) {
				// Robot position
				Point pt = loadPoint(in);
				ee = new OperandPoint(pt);

			} else if (opType == Operand.ROBOT) {
				boolean isCart = in.readBoolean();
				// Robot point (LPos/JPos)
				ee = new RobotPoint(robot, isCart);
				
			} else {
				ee = new OperandGeneric();
			}
		}

		return ee;
	}
	
	private static float[] loadFloatArray(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read the length of the list
		int len = in.readInt();
		float[] list = new float[len];
		// Read each value of the list
		for (int idx = 0; idx < list.length; ++idx) {
			list[idx] = in.readFloat();
		}

		return list;
	}

	private static void loadFrame(Frame ref, DataInputStream in) throws IOException {
		byte type = in.readByte();

		if ((ref instanceof ToolFrame && type != 1) ||
			(ref instanceof UserFrame && type != 2)) {
			// Types do not match
			throw new IOException("Invalid Frame type!");
		}
		// Read the frame's name
		String name = in.readUTF();
		ref.setName(name);
		
		PVector v = loadPVector(in);
		int len;
		
		if (ref instanceof UserFrame) {
			// Read origin value
			((UserFrame)ref).setOrigin(v);
			len = 3;
			
		} else {
			// Read TCP offset values
			((ToolFrame)ref).setTCPOffset(v);
			len = 6;
		}

		// Read axes quaternion values
		ref.setOrientation( loadRQuaternion(in) );
		
		// Read in orientation points (and tooltip teach points for tool frames)
		for (int idx = 0; idx < len; ++idx) {
			ref.setPoint(loadPoint(in), idx);
		}

		// Read manual entry origin values
		ref.setDEOrigin(loadPVector(in));
		ref.setDEOrientationOffset(loadRQuaternion(in));

		if (ref instanceof UserFrame) {
			// Load point for the origin offset of the frame
			((UserFrame)ref).setOrientOrigin(loadPoint(in));
		}
	}
	
	private static int loadFrameBytes(RoboticArm robot, String srcPath) {
		int idx = -1;
		File src = new File(srcPath);
		
		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			// Load Tool Frames
			int size = Math.max(0, Math.min(dataIn.readInt(), 10));
			
			for(idx = 0; idx < size; idx += 1) {
				loadFrame(robot.getToolFrame(idx), dataIn);
			}

			// Load User Frames
			size = Math.max(0, Math.min(dataIn.readInt(), 10));

			for(idx = 0; idx < size; idx += 1) {
				loadFrame(robot.getUserFrame(idx), dataIn);
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
	
	private static Instruction loadInstruction(RoboticArm robot, DataInputStream in)
			throws IOException {
		
		Instruction inst = null;
		// Read flag byte
		byte instType = in.readByte();

		if(instType == 2) {
			// Read data for a MotionInstruction object
			boolean isCommented = in.readBoolean();
			int mType = in.readInt();
			int pType = in.readInt();
			int posIdx = in.readInt();
			int circPType = in.readInt();
			int circPosIdx = in.readInt();
			float spdMod = in.readFloat();
			int term = in.readInt();
			int tFrameIdx = in.readInt();
			int uFrameIdx = in.readInt();
			int offType = in.readInt();
			int offIdx = in.readInt();
			
			inst = new PosMotionInst(isCommented, mType, pType, posIdx,
					circPType, circPosIdx, spdMod, term, tFrameIdx, uFrameIdx,
					offType, offIdx);

		} else if(instType == 3) {
			// Read data for a FrameInstruction object
			boolean isCommented = in.readBoolean();
			inst = new FrameInstruction( in.readInt(), in.readInt() );
			inst.setIsCommented(isCommented);

		} else if(instType == 4) {
			// Read data for a ToolInstruction object
			boolean isCommented = in.readBoolean();
			int reg = in.readInt();
			int val = in.readInt();
			
			inst = new IOInstruction(reg, val == 0 ? Fields.OFF : Fields.ON);
			inst.setIsCommented(isCommented);

		} else if (instType == 5) {
			boolean isCommented = in.readBoolean();
			int labelNum = in.readInt();

			inst = new LabelInstruction(labelNum);
			inst.setIsCommented(isCommented);

		} else if (instType == 6) {
			boolean isCommented = in.readBoolean();
			int tgtLabelNum = in.readInt();

			inst = new JumpInstruction(tgtLabelNum);
			inst.setIsCommented(isCommented);

		} else if (instType == 7) {
			boolean isCommented = in.readBoolean();
			int tgtRID = in.readInt();
			String pName = in.readUTF();
			// TODO Refactor THIS
			RoboticArm tgt = RobotRun.getInstance().getRobot(tgtRID);
			
			inst = new CallInstruction(tgt, pName);
			inst.setIsCommented(isCommented);

		} else if (instType == 8) {
			boolean isCommented = in.readBoolean();
			int regType = in.readInt();
			int posIdx = -1;
			Register reg;
			
			if (regType == 3) {
				reg = robot.getIOReg( in.readInt() );
				
			} else if (regType == 2) {
				reg = robot.getPReg( in.readInt() );
				posIdx = in.readInt();
				
			} else if (regType == 1) {
				reg = robot.getDReg( in.readInt() );
				
			} else {
				reg = null;
			}
			
			Expression expr = (Expression)loadExpressionElement(robot, in);

			inst = new RegisterStatement(reg, posIdx, expr);
			inst.setIsCommented(isCommented);

		} else if (instType == 9) {
			// Load data associated with an if statement
			boolean isCommented = in.readBoolean();
			Instruction subInst = loadInstruction(robot, in);
			AtomicExpression expr = (AtomicExpression)loadExpressionElement(robot, in);
			
			inst = new IfStatement(expr, subInst);
			inst.setIsCommented(isCommented);
			
		} else if (instType == 10) {
			// Load data associated with a select statement
			boolean isCommented = in.readBoolean();
			Operand<?> arg = (Operand<?>)loadExpressionElement(robot, in);
			
			ArrayList<Operand<?>> cases = new ArrayList<>();
			int size = in.readInt();
			
			while (size-- > 0) {
				cases.add( (Operand<?>)loadExpressionElement(robot, in) );
			}
			
			ArrayList<Instruction> insts = new ArrayList<>();
			size = in.readInt();
			
			while (size-- > 0) {
				insts.add( loadInstruction(robot, in) );
			}
			
			inst = new SelectStatement(arg, cases, insts);
			inst.setIsCommented(isCommented);
			
		} else if (instType == 11) {
			
			boolean isCommented = in.readBoolean();
			int mType = in.readInt();
			int pdx = in.readInt();
			float spdMod = in.readFloat();
			int term = in.readInt();
			
			byte flag = in.readByte();
			String loadedName;
			
			if (flag == 0) {
				loadedName = null;
				
			} else {
				loadedName = in.readUTF();
			}
			
			inst = new CamMoveToObject(isCommented, mType, Fields.PTYPE_WO,
					pdx, spdMod, term, loadedName);
			
		}/* Add other instructions here! */
		else if (instType == 1) {
			inst = new Instruction();
			boolean isCommented = in.readBoolean();
			inst.setIsCommented(isCommented);

		} else {
			return null;
		}

		return inst;
	}
	
	private static Integer loadInteger(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read integer value
		return in.readInt();
	}

	private static void loadMacros(RoboticArm r, String filePath) {	
		try {
			File destDir = new File(filePath);
			FileInputStream in = new FileInputStream(destDir);
			DataInputStream dataIn = new DataInputStream(in);
			
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			
			int numMacros = dataIn.readInt();
			
			for(int i = 0; i < numMacros; i += 1) {
				boolean isManual = dataIn.readBoolean();
				int robotID = dataIn.readInt();
				int progIdx = dataIn.readInt();
				int keyNum = dataIn.readInt();
				Macro m = new Macro(isManual, robotID, progIdx, keyNum);
				
				r.getMacroList().add(m);
				
				if(!isManual && keyNum != -1) {
					r.getMacroKeyBinds()[keyNum] = m;
				}
			}
			
			dataIn.close();
			in.close();
		}
		catch (Exception e) {
			System.err.println("Unable to load macros for robot " + r.RID + "!");
		}
	}

	private static Point loadPoint(DataInputStream in) throws IOException {
		// Read flag byte
		byte val = in.readByte();

		if (val == 0) {
			return null;
		}
		
		// Read the point's position
		PVector position = loadPVector(in);
		// Read the point's orientation
		RQuaternion orientation = loadRQuaternion(in);
		// Read the joint angles for the joint's position
		float[] angles = loadFloatArray(in);

		return new Point(position, orientation, angles);
	}

	private static Program loadProgram(RoboticArm robot, DataInputStream in) throws IOException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read program name
		String name = in.readUTF();
		Program prog = new Program(name, robot);
		int nReg;

		// Read in all the positions saved for the program
		do {
			nReg = in.readInt();

			if (nReg == -1) {
				break;  
			}

			// Load the saved point
			Point pt = loadPoint(in);
			prog.setPosition(nReg, pt);

		} while (true);

		// Read the number of instructions stored for this program
		int numOfInst = Math.max(0, Math.min(in.readInt(), 500));

		while(numOfInst-- > 0) {
			// Read in each instruction
			Instruction inst = loadInstruction(robot, in);
			prog.addInstAtEnd(inst);
		}

		return prog;
	}

	private static int loadProgramBytes(RoboticArm robot, String srcPath) {
		File src = new File(srcPath);

		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);
			// Read the number of programs stored in src
			int size = Math.max(0, Math.min(dataIn.readInt(), 200));
			
			while(size-- > 0) {
				// Read each program from src
				robot.addProgram( loadProgram(robot, dataIn) );
			}

			dataIn.close();
			in.close();
			
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not locate src
			System.err.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Reached the end of src unexpectedly
			System.err.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 2;

		} catch (IOException IOEx) {
			// An error occurred with reading from src
			System.err.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 3;
			
		} catch (ClassCastException CCEx) {
			/* An error occurred with casting between objects while loading a
			 * program's instructions */
			System.err.printf("%s is corrupt!\n", src.getName());
			CCEx.printStackTrace();
			return 4;
			
		} catch (NegativeArraySizeException NASEx) {
			// Issue with loading program points
			System.err.printf("%s is corrupt!\n", src.getName());
			NASEx.printStackTrace();
			return 5;
		}
	}

	private static PVector loadPVector(DataInputStream in) throws IOException {
		// Read flag byte
		int val = in.readByte();

		if (val == 0) {
			return null;

		}
		
		// Read vector data
		PVector v = new PVector();
		v.x = in.readFloat();
		v.y = in.readFloat();
		v.z = in.readFloat();
		return v;
	}
	
	private static int loadRegisterBytes(RoboticArm robot, String srcPath) {
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
				
				DataRegister dReg = robot.getDReg(reg);
				
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

				Point p = loadPoint(dataIn);
				String c = dataIn.readUTF();
				// Null comments are stored as ""
				if(c == "") { c = null; }
				boolean isCartesian = dataIn.readBoolean();
				
				PositionRegister pReg = robot.getPReg(idx);
				
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
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Unexpectedly reached the end of src
			System.err.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// Error occrued while reading from src
			System.err.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	private static int loadRobotData(RoboticArm robot) {
		File srcDir = new File( String.format("%srobot%d/", tmpDirPath, robot.RID) );
		
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			// No such directory exists
			return 1;	
		}
		
		// Load the Robot's programs, frames, and registers from their respective files
		loadProgramBytes(robot, String.format("%s/programs.bin", srcDir.getAbsolutePath()));
		loadFrameBytes(robot, String.format("%s/frames.bin", srcDir.getAbsolutePath()));
		loadRegisterBytes(robot, String.format("%s/registers.bin", srcDir.getAbsolutePath()));
		loadMacros(robot, String.format("%s/macros.bin", srcDir.getAbsolutePath()));
		
		return 0;
	}

	private static RQuaternion loadRQuaternion(DataInputStream in) throws IOException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read values of the quaternion
		float w = in.readFloat(),
				x = in.readFloat(),
				y = in.readFloat(),
				z = in.readFloat();

		return new RQuaternion(w, x, y, z);
	}
	
	private static Scenario loadScenario(DataInputStream in, RobotRun app) throws IOException, NullPointerException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;
		}
		
		// Read the name of the scenario
		String name = in.readUTF();
		Scenario s = new Scenario(name);
		// An extra set of only the loaded fixtures
		ArrayList<Fixture> fixtures = new ArrayList<>();
		// A list of parts which have a fixture reference defined
		ArrayList<LoadedPart> partsWithReferences = new ArrayList<>();

		// Read the number of objects in the scenario
		int size = in.readInt();
		// Read all the world objects contained in the scenario
		while (size-- > 0) {
			try {
				Object loadedObject = loadWorldObject(in, app);

				if (loadedObject instanceof WorldObject) {
					// Add all normal world objects to the scenario
					s.addWorldObject( (WorldObject)loadedObject );

					if (loadedObject instanceof Fixture) {
						// Save an extra reference of each fixture
						fixtures.add( (Fixture)loadedObject );
					}

				} else if (loadedObject instanceof LoadedPart) {
					LoadedPart lPart = (LoadedPart)loadedObject;

					if (lPart.part != null) {
						// Save the part in the scenario
						s.addWorldObject(lPart.part);

						if (lPart.referenceName != null) {
							// Save any part with a defined reference
							partsWithReferences.add(lPart);
						}
					}
				}
				
			} catch (NullPointerException NPEx) {
				/* Invalid model source file name */
				System.err.println( NPEx.getMessage() );
				
			} catch (RuntimeException REx) {
				/* Invalid model source file name */
				System.err.println( REx.getMessage() );
			}
		}
		
		// Set all the Part's references
		for (LoadedPart lPart : partsWithReferences) {
			for (Fixture f : fixtures) {
				if (lPart.referenceName.equals(f.getName())) {
					lPart.part.setFixtureRef(f);
				}
			}
		}

		return s;
	}
	
	private static int loadScenarioBytes(RobotRun process, String srcPath) {
		File src = new File(srcPath);
		
		if (!src.exists() || !src.isDirectory()) {
			// No files to load
			return 1;
		}
		
		File[] scenarioFiles = src.listFiles();
		File activeFile = null;
		
		ArrayList<Scenario> scenarioList = process.getScenarios();
		
		// Load each scenario from their respective files
		for (File scenarioFile : scenarioFiles) {
			try {
				activeFile = scenarioFile;
				FileInputStream in = new FileInputStream(activeFile);
				DataInputStream dataIn = new DataInputStream(in);
				
				Scenario s = loadScenario(dataIn, process);
				
				if (s != null) {
					scenarioList.add(s);
				}
				
				dataIn.close();
				in.close();
			
			} catch (FileNotFoundException FNFEx) {
				System.err.printf("File %s does not exist in \\tmp\\scenarios.\n",
						activeFile.getName());
				FNFEx.printStackTrace();
				
			} catch (IOException IOEx) {
				System.err.printf("File, %s, in \\tmp\\scenarios is corrupt!\n",
						activeFile.getName());
				IOEx.printStackTrace();
			}
		}
		
		activeFile = new File(src.getAbsolutePath() + "/activeScenario.bin");
		
		try {
			
			if (activeFile.exists()) {
				FileInputStream in = new FileInputStream(activeFile);
				DataInputStream dataIn = new DataInputStream(in);
				
				// Read the name of the active scenario
				String activeName = dataIn.readUTF();
				process.setActiveScenario(activeName);
				
				dataIn.close();
				in.close();
			}
		
		} catch (IOException IOEx) {
			System.err.println("The active scenario file is corrupt!");
			// An error occurred with loading a scenario from a file
			IOEx.printStackTrace();
		}
		
		return 0;
	}

	private static RShape loadShape(DataInputStream in, RobotRun app) throws IOException,
			NullPointerException, RuntimeException {
		
		// Read flag byte
		byte flag = in.readByte();
		RShape shape = null;

		if (flag != 0) {
			// Read fiil color
			Integer fill = loadInteger(in);

			if (flag == 1) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float x = in.readFloat(),
						y = in.readFloat(),
						z = in.readFloat();
				// Create a box
				shape = new RBox(fill, strokeVal, x, y, z);

			} else if (flag == 2) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float radius = in.readFloat(),
						hgt = in.readFloat();
				// Create a cylinder
				shape = new RCylinder(fill, strokeVal, radius, hgt);

			} else if (flag == 3) {
				float scale = in.readFloat();
				String srcPath = in.readUTF();
				
				File src = new File(DataManagement.dataDirPath + srcPath);
				
				if (!src.exists() || src.isDirectory()) {
					String error = String.format("Source file, %s, does not exist in %s",
							srcPath, DataManagement.dataDirPath);
					throw new NullPointerException(error);
				}
				
				MyPShape form = app.loadSTLModel(srcPath, fill);
				
				// Creates a complex shape from the srcPath located in RobotRun/data/
				shape = new ComplexShape(srcPath, form, fill, scale);
			}
		}

		return shape;
	}

	private static Object loadWorldObject(DataInputStream in, RobotRun app) throws IOException, NullPointerException {
		// Load the flag byte
		byte flag = in.readByte();
		Object wldObjFields = null;

		if (flag != 0) {
			// Load the name and shape of the object
			String name = in.readUTF();
			RShape form = loadShape(in, app);
			// Load the object's local orientation
			PVector center = loadPVector(in);
			RMatrix orientationAxes = new RMatrix( load2DDoubleArray(in) );
			
			CoordinateSystem localOrientation = new CoordinateSystem(center, orientationAxes);

			if (flag == 1) {
				center = loadPVector(in);
				orientationAxes = new RMatrix( load2DDoubleArray(in) );
				
				CoordinateSystem defaultOrientation = new CoordinateSystem(center, orientationAxes);
				
				// Load the part's bounding-box and fixture reference name
				PVector OBBDims = loadPVector(in);
				String refName = in.readUTF();

				if (refName.equals("")) {
					// A part object
					wldObjFields = new Part(name, form, OBBDims, localOrientation, defaultOrientation, null);
				} else {
					// A part object with its reference's name
					wldObjFields = new LoadedPart( new Part(name, form, OBBDims, localOrientation, defaultOrientation, null), refName );
				}

			} else if (flag == 2) {
				// A fixture object
				wldObjFields = new Fixture(name, form, localOrientation);
			} 
		}

		return wldObjFields;
	}
	
	private static void robotPostProcessing(RoboticArm robot, RobotRun process) {
		ArrayList<Scenario> scenes = process.getScenarios();
		
		/**
		 * Loop through all programs and update call instructions, so that they
		 * reference the correct target program.
		 */
		for (int pdx = 0; pdx < robot.numOfPrograms(); ++pdx) {
			Program p = robot.getProgram(pdx);
			
			for (int idx = 0; idx < p.size(); ++idx) {
				Instruction inst = p.get(idx);
				
				if (inst instanceof CallInstruction) {
					// Update a top call instruction
					CallInstruction cInst = (CallInstruction)inst;
					
					if (cInst.getTgtDevice() != null && cInst.getLoadedName() != null) {
						Program tgt = cInst.getTgtDevice().getProgram(cInst.getLoadedName());
						cInst.setProg(tgt);
					}
					
				} else if (inst instanceof SelectStatement) {
					// Update call instructions in a select statement
					SelectStatement stmt = (SelectStatement)inst;
					ArrayList<Instruction> instList = stmt.getInstrs();
					
					for (Instruction caseInst : instList) {
						
						if (caseInst instanceof CallInstruction) {
							CallInstruction cInst = (CallInstruction)caseInst;
							
							if (cInst.getTgtDevice() != null && cInst.getLoadedName() != null) {
								Program tgt = cInst.getTgtDevice().getProgram(cInst.getLoadedName());
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
						
						if (cInst.getTgtDevice() != null && cInst.getLoadedName() != null) {
							Program tgt = cInst.getTgtDevice().getProgram(cInst.getLoadedName());
							cInst.setProg(tgt);
						}
					}
					
				} else if (inst instanceof CamMoveToObject) {
					// Update a camera motion instruction
					CamMoveToObject cMInst = (CamMoveToObject)inst;
					
					for (Scenario s : scenes) {
						if (s.getName().equals(cMInst.getLoadedSceneName())) {
							cMInst.setScene(s);
						}
					}
				}
			}
		}
	}

	private static void save2DDoubleArray(double[][] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write the dimensions of the list
			out.writeInt(list.length);
			out.writeInt(list[0].length);
			// Write each value in the list
			for (int row = 0; row < list.length; ++row) {
				for (int col = 0; col < list[0].length; ++col) {
					out.writeDouble(list[row][col]);
				}
			}
		}
	}
	
	private static void saveExpressionElement(ExpressionElement ee,
			DataOutputStream out) throws IOException {

		if (ee == null) {
			// Indicate the object saved is null
			out.writeByte(0);

		} else {
			
			if (ee instanceof Operator) {
				// Operator
				Operator op = (Operator)ee;

				out.writeByte(1);
				out.writeInt( op.getOpID() );

			} else if (ee instanceof Expression) {
				Expression expr = (Expression)ee;
				
				out.writeByte(2);
				
				int exprLen = expr.size();
				// Save the length of the expression
				out.writeInt(exprLen);
		
				// Save each expression element
				for (int idx = 0; idx < exprLen; ++idx) {
					saveExpressionElement(expr.get(idx), out);
				}
				
			} else if (ee instanceof AtomicExpression) {
				// Subexpression
				AtomicExpression ae = (AtomicExpression)ee;

				out.writeByte(3);
				saveExpressionElement(ae.getArg1(), out);
				saveExpressionElement(ae.getArg2(), out);
				saveExpressionElement(ae.getOp(), out);

			} else if (ee instanceof Operand<?>) {
				Operand<?> eo = (Operand<?>)ee;

				out.writeByte(4);
				// Indicate that the object is non-null
				out.writeInt(eo.getType());

				if (eo instanceof OperandRegister) {
					// Data, Position, or IO register
					out.writeInt( ((OperandRegister<?>)eo).getRegIdx() );

					if (eo instanceof OperandPRegIdx) {
						// Specific portion of a point
						out.writeInt( ((OperandPRegIdx)eo).getSubIdx() );
					}
				} else if(eo instanceof OperandCamObj) {
					// Object match operand
					//out.writeInt(0);
				} else if (eo instanceof FloatMath) {
					// Constant float
					out.writeFloat( ((FloatMath)eo).getArithValue() );
				} else if (eo instanceof BoolMath) {
					// Constant boolean
					out.writeBoolean( ((BoolMath)eo).getBoolValue() );					
				} else if (eo instanceof PointMath) {
					// Robot position
					if (eo instanceof RobotPoint) {
						out.writeBoolean(((RobotPoint) eo).isCartesian());
						
					} else {
						savePoint(((PointMath)eo).getPointValue(), out);
					}
					
				} // Otherwise it is uninitialized
			}
		}
	}

	private static void saveFloatArray(float[] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write list length
			out.writeInt(list.length);
			// Write each value in the list
			for (int idx = 0; idx < list.length; ++idx) {
				out.writeFloat(list[idx]);
			}
		}
	}
	
	private static void saveFrame(Frame f, DataOutputStream out) throws IOException {

		// Save a flag to indicate what kind of frame was saved
		if (f == null) {
			out.writeByte(0);
			return;

		} else if (f instanceof ToolFrame) {
			out.writeByte(1);

		} else if (f instanceof UserFrame) {
			out.writeByte(2);

		} else {
			throw new IOException("Invalid Frame!");
		}
		// Write the name of the string
		out.writeUTF(f.getName());
		
		int len;
		
		if (f instanceof UserFrame) {
			UserFrame uFrame = (UserFrame)f;
			// Write User frame origin
			savePVector(uFrame.getOrigin(), out);
			len = 3;
			
			// Write frame axes
			saveRQuaternion(uFrame.getOrientation(), out);

		} else {
			ToolFrame tf = (ToolFrame)f;
			
			// Write Tool frame TCP offset
			savePVector( tf.getTCPOffset(), out );
			len = 6;
			
			// Write frame axes
			saveRQuaternion(tf.getOrientationOffset(), out);
		}


		
		// Write frame orientation (and tooltip teach points for tool frames) points
		for (int idx = 0; idx < len; ++idx) {
			savePoint(f.getPoint(idx), out);
		}

		// Write frame manual entry origin value
		savePVector(f.getDEOrigin(), out);
		// Write frame manual entry origin value
		saveRQuaternion(f.getDEOrientationOffset(), out);

		if (f instanceof UserFrame) {
			// Save point for the origin offset of the frame
			savePoint( ((UserFrame)f).getOrientOrigin(), out );
		}
	}

	private static int saveFrameBytes(RoboticArm robot, String destPath) {
		File dest = new File(destPath);

		try {
			// Create dest if it does not already exist
			if(!dest.exists()) {
				try {
					dest.createNewFile();
					
				} catch (IOException IOEx) {
					IOEx.printStackTrace();
				}
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			// Save Tool Frames
			dataOut.writeInt(Fields.FRAME_NUM);
			for (int idx = 0; idx < Fields.FRAME_NUM; ++idx) {
				saveFrame(robot.getToolFrame(idx), dataOut);
			}
			
			// Save User Frames
			dataOut.writeInt(Fields.FRAME_NUM);
			for (int idx = 0; idx < Fields.FRAME_NUM; ++idx) {
				saveFrame(robot.getUserFrame(idx), dataOut);
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
	
	private static void saveInstruction(Instruction inst, DataOutputStream out)
			throws IOException {

		/* Each Instruction subclass MUST have its own saving code block
		 * associated with its unique data fields */
		if (inst instanceof PosMotionInst) {
			PosMotionInst m_inst = (PosMotionInst)inst;
			
			out.writeByte(2);
			// Write data associated with the MotionIntruction object
			out.writeBoolean(m_inst.isCommented());
			out.writeInt(m_inst.getMotionType());
			out.writeInt(m_inst.getPosType());
			out.writeInt(m_inst.getPosIdx());
			out.writeInt(m_inst.getCircPosType());
			out.writeInt(m_inst.getCircPosIdx());
			out.writeFloat(m_inst.getSpdMod());
			out.writeInt(m_inst.getTermination());
			out.writeInt(m_inst.getTFrameIdx());
			out.writeInt(m_inst.getUFrameIdx());
			out.writeInt(m_inst.getOffsetType());
			out.writeInt(m_inst.getOffsetIdx());

		} else if(inst instanceof FrameInstruction) {
			FrameInstruction f_inst = (FrameInstruction)inst;
			
			out.writeByte(3);
			// Write data associated with the FrameInstruction object
			out.writeBoolean(f_inst.isCommented());
			out.writeInt(f_inst.getFrameType());
			out.writeInt(f_inst.getFrameIdx());

		} else if(inst instanceof IOInstruction) {
			IOInstruction t_inst = (IOInstruction)inst;
			
			out.writeByte(4);
			// Write data associated with the ToolInstruction object
			out.writeBoolean(t_inst.isCommented());
			out.writeInt(t_inst.getReg());
			out.writeInt(t_inst.getState() ? 1 : 0);

		} else if(inst instanceof LabelInstruction) {
			LabelInstruction l_inst = (LabelInstruction)inst;

			out.writeByte(5);
			out.writeBoolean(l_inst.isCommented());
			out.writeInt(l_inst.getLabelNum());

		} else if(inst instanceof JumpInstruction) {
			JumpInstruction j_inst = (JumpInstruction)inst;

			out.writeByte(6);
			out.writeBoolean(j_inst.isCommented());
			out.writeInt(j_inst.getTgtLblNum());

		} else if (inst instanceof CallInstruction) {
			CallInstruction c_inst = (CallInstruction)inst;
			
			out.writeByte(7);
			out.writeBoolean(c_inst.isCommented());
			
			if (c_inst.getTgtDevice() == null) {
				out.writeInt(-1);
				
			} else {
				out.writeInt(c_inst.getTgtDevice().RID);
			}
			
			if (c_inst.getProg() == null) {
				out.writeUTF("N/A");
				
			} else {
				out.writeUTF( c_inst.getProg().getName() );
			}

		} else if (inst instanceof RegisterStatement) {
			RegisterStatement rs = (RegisterStatement)inst;
			Register r = rs.getReg();

			out.writeByte(8);
			out.writeBoolean(rs.isCommented());

			// In what type of register will the result of the statement be placed?
			int regType;
			
			if (r instanceof IORegister) {
				regType = 3;

			} else if (r instanceof PositionRegister) {
				
				regType = 2;

			} else if (r instanceof DataRegister) {
				regType = 1;
				
			} else {
				regType = 0;
			}

			out.writeInt(regType);
			
			if (regType > 0) {
				out.writeInt(r.idx);
				
				if (regType == 2) {
					out.writeInt(rs.getPosIdx());
				}
			}

			saveExpressionElement(rs.getExpr(), out);

		} else if (inst instanceof IfStatement) {
			IfStatement ifSt = (IfStatement)inst;
			
			out.writeByte(9);
			out.writeBoolean(ifSt.isCommented());
			// Save data associated with the if statement
			saveInstruction(ifSt.getInstr(), out);
			saveExpressionElement(ifSt.getExpr(), out);
			
		} else if (inst instanceof SelectStatement) {
			SelectStatement sStmt = (SelectStatement)inst;
			ArrayList<Operand<?>> cases = sStmt.getCases();
			ArrayList<Instruction> insts = sStmt.getInstrs();
			// Save data associated with the select statement instruction
			out.writeByte(10);
			out.writeBoolean(sStmt.isCommented());
			
			saveExpressionElement(sStmt.getArg(), out);
			// Save list of cases
			out.writeInt(cases.size());
			for (Operand<?> opr : cases) {
				saveExpressionElement(opr, out);
			}
			// Save list of instructions
			out.writeInt(insts.size());
			for (Instruction i : insts) {
				saveInstruction(i, out);
			}
			
		} else if (inst instanceof CamMoveToObject) {
			CamMoveToObject cMInst = (CamMoveToObject)inst;
			
			out.writeByte(11);
			out.writeBoolean(cMInst.isCommented());
			out.writeInt(cMInst.getMotionType());
			out.writeInt(cMInst.getPosIdx());
			out.writeFloat(cMInst.getSpdMod());
			out.writeInt(cMInst.getTermination());
			// Save the name of the reference scene, if it is not null
			Scenario scene = cMInst.getScene();
			
			if (scene == null) {
				out.writeByte(0);
				
			} else {
				out.writeByte(1);
				out.writeUTF(scene.getName());
			}
			
		}/* Add other instructions here! */
		else if (inst != null) {
			/// A blank instruction
			out.writeByte(1);
			out.writeBoolean(inst.isCommented());

		} else {
			// Indicate a null-value is saved
			out.writeByte(0);
		}


	}

	private static void saveInteger(Integer i, DataOutputStream out) throws IOException {

		if (i == null) {
			// Write byte flag
			out.writeByte(0);

		} else {
			// Write byte flag
			out.writeByte(1);
			// Write integer value
			out.writeInt(i);
		}
	}
	
	private static void saveMacros(RoboticArm r, String filePath) {
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
			
			dataOut.writeInt(r.getMacroList().size());
			
			for(Macro m: r.getMacroList()) {
				dataOut.writeBoolean(m.isManual());
				dataOut.writeInt(m.getRobotID());
				dataOut.writeInt(m.getProgIdx());
				dataOut.writeInt(m.getKeyNum());
			}
			
			dataOut.close();
			out.close();
		} 
		catch (Exception e) {
			System.err.println("Unable to save macros for robot " + r.RID + "!");
		}
	}

	private static void savePoint(Point p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Indicate a null value is saved
			out.writeByte(0);

		} else {
			// Indicate a non-null value is saved
			out.writeByte(1);
			// Write position of the point
			savePVector(p.position, out);
			// Write point's orientation
			saveRQuaternion(p.orientation, out);
			// Write the joint angles for the point's position
			saveFloatArray(p.angles, out);
		}
	}

	private static void saveProgram(Program p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Indicates a null value is saved
			out.writeByte(0);

		} else {
			// Indicates a non-null value is saved
			out.writeByte(1);

			out.writeUTF(p.getName());

			for (int pdx = 0; pdx < 1000; ++pdx) {
				if (p.getPosition(pdx) != null) {
					// Save the position with its respective index
					out.writeInt(pdx);
					savePoint(p.getPosition(pdx), out);
				}
			}

			// End of saved positions
			out.writeInt(-1);

			out.writeInt(p.getNumOfInst());
			// Save each instruction
			for(Instruction inst : p) {
				saveInstruction(inst, out);
			}
		}
	}

	private static int saveProgramBytes(RoboticArm robot, String destPath) {
		File dest = new File(destPath);
		
		try {
			// Create dest if it does not already exist
			if(!dest.exists()) {      
				try {
					dest.createNewFile();
					System.err.printf("Successfully created %s.\n", dest.getName());
				} catch (IOException IOEx) {
					System.err.printf("Could not create %s ...\n", dest.getName());
					IOEx.printStackTrace();
					return 1;
				}
			} 

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);
			// Save the number of programs
			dataOut.writeInt(robot.numOfPrograms());

			for(int idx = 0; idx < robot.numOfPrograms(); ++idx) {
				// Save each program
				saveProgram(robot.getProgram(idx), dataOut);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not locate dest
			System.err.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// An error occurred with writing to dest
			System.err.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	private static void savePVector(PVector p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);
			// Write vector data
			out.writeFloat(p.x);
			out.writeFloat(p.y);
			out.writeFloat(p.z);
		}
	}
	
	private static int saveRegisterBytes(RoboticArm robot, String destPath) {
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
				DataRegister dReg = robot.getDReg(idx);
				PositionRegister pReg = robot.getPReg(idx);
				
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
				DataRegister dReg = robot.getDReg(idx);
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
				PositionRegister pReg = robot.getPReg(idx);
				dataOut.writeInt(idx);
				savePoint(pReg.point, dataOut);

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
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// Error occured while reading from dest
			System.err.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	private static void saveRQuaternion(RQuaternion q, DataOutputStream out) throws IOException {
		if (q == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);

			for (int idx = 0; idx < 4; ++idx) {
				// Write each quaternion value
				out.writeFloat(q.getValue(idx));
			}
		}
	}
	
	private static void saveScenario(Scenario s, DataOutputStream out) throws IOException {

		if (s == null) {
			// Indicate the value saved is null
			out.writeByte(0);

		} else {
			// Indicate the value saved is non-null
			out.writeByte(1);
			// Write the name of the scenario
			out.writeUTF(s.getName());
			// Save the number of world objects in the scenario
			out.writeInt( s.size() );

			for (WorldObject wldObj : s) {
				// Save all the world objects associated with the scenario
				saveWorldObject(wldObj, out);
			}
		}
	}
	
	private static int saveScenarioBytes(ArrayList<Scenario> scenarios,
			String ASName, String destPath) {
		File dest = new File(destPath);
		
		if (!dest.exists()) {
			dest.mkdir();
			
		} else if (dest.exists() && !dest.isDirectory()) {
			// File must be a directory
			return 1;
		}
		
		File scenarioFile = null;
		
		if (ASName != null) {
			// Save the name of the active scenario
			scenarioFile = new File(dest.getAbsolutePath() + "/activeScenario.bin");
			
			try {
				if (!scenarioFile.exists()) {
					scenarioFile.createNewFile();
				}
				
				FileOutputStream out = new FileOutputStream(scenarioFile);
				DataOutputStream dataOut = new DataOutputStream(out);
				
				dataOut.writeUTF(ASName);
				
				dataOut.close();
				out.close();
				
			} catch (IOException IOEx) {
				// Issue with writing or opening a file
				System.err.printf("Error with file %s.\n", scenarioFile.getName());
				IOEx.printStackTrace();
				return 2;
			}
		}
		
		for (Scenario s : scenarios) {
			try {
				// Save each scenario in a separate file
				scenarioFile = new File(dest.getAbsolutePath() + "/" + s.getName() + ".bin");
				
				if (!scenarioFile.exists()) {
					scenarioFile.createNewFile();
				}
				
				FileOutputStream out = new FileOutputStream(scenarioFile);
				DataOutputStream dataOut = new DataOutputStream(out);
				// Save the scenario data
				saveScenario(s, dataOut);
				
				dataOut.close();
				out.close();
				
			} catch (IOException IOEx) {
				// Issue with writing or opening a file
				System.err.printf("Error with file %s.\n", scenarioFile.getName());
				IOEx.printStackTrace();
				return 2;
			}
		}
		
		return 0;
			
		
	}
	
	private static void saveShape(RShape shape, DataOutputStream out) throws IOException {
		if (shape == null) {
			// Indicate the saved value is null
			out.writeByte(0);

		} else {
			if (shape instanceof RBox) {
				// Indicate the saved value is a box
				out.writeByte(1);
			} else if (shape instanceof RCylinder) {
				// Indicate the value saved is a cylinder
				out.writeByte(2);
			} else if (shape instanceof ComplexShape) {
				// Indicate the value saved is a complex shape
				out.writeByte(3);
			}

			// Write fill color value
			saveInteger(shape.getFillValue(), out);

			if (shape instanceof RBox) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save length, height, and width of the box
				out.writeFloat(shape.getDim(DimType.LENGTH));
				out.writeFloat(shape.getDim(DimType.HEIGHT));
				out.writeFloat(shape.getDim(DimType.WIDTH));

			} else if (shape instanceof RCylinder) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save the radius and height of the cylinder
				out.writeFloat(shape.getDim(DimType.RADIUS));
				out.writeFloat(shape.getDim(DimType.HEIGHT));

			} else if (shape instanceof ComplexShape) {
				ComplexShape m = (ComplexShape)shape;

				out.writeFloat(m.getDim(DimType.SCALE));
				// Save the source path of the complex shape
				out.writeUTF(m.getSourcePath()); 
			}
		}
	}

	private static void saveWorldObject(WorldObject wldObj, DataOutputStream out) throws IOException {

		if (wldObj == null) {
			// Indicate that the value saved is null
			out.writeByte(0);

		} else {
			if (wldObj instanceof Part) {
				// Indicate that the value saved is a Part
				out.writeByte(1);
			} else if (wldObj instanceof Fixture) {
				// Indicate that the value saved is a Fixture
				out.writeByte(2);
			}

			// Save the name and form of the object
			out.writeUTF(wldObj.getName());
			saveShape(wldObj.getForm(), out);
			// Save the local orientation of the object
			savePVector(wldObj.getLocalCenter(), out);
			save2DDoubleArray(wldObj.getLocalOrientation().getData(), out);
			
			if (wldObj instanceof Part) {
				Part part = (Part)wldObj;
				String refName = "";
				
				// Save the default orientation of the part
				savePVector(part.getDefaultCenter(), out);
				save2DDoubleArray(part.getDefaultOrientation().getData(), out);
				
				savePVector(part.getOBBDims(), out);

				if (part.getFixtureRef() != null) {
					// Save the name of the part's fixture reference
					refName = part.getFixtureRef().getName();
				}

				out.writeUTF(refName);
			}
		}
	}
	
	private static void validateTmpDir() {
		File tmpDir = new File(tmpDirPath);
		
		if (!tmpDir.exists()) {
			// Create the directory if it does not exist
			tmpDir.mkdir();
		}
	}
}