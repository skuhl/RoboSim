package screen;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import frame.ToolFrame;
import frame.UserFrame;
import global.Fields;
import processing.core.PVector;
import programming.Instruction;
import programming.Macro;
import programming.MotionInstruction;
import programming.Program;
import programming.SelectStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import robot.RoboticArm;
import ui.DisplayLine;
import ui.MenuScroll;

public abstract class Screen {
	public final ScreenMode mode;
	protected MenuScroll contents;
	
	protected final String header;
	protected String[] labels;
	protected MenuScroll options;
	protected final RobotRun robotRun;
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		
		header = loadHeader();
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 199);
		labels = new String[5];
	}
	
	public Screen(ScreenMode m, RobotRun r, int cMax, int cX, int cY, int oMax,
			int oX, int oY) {
		
		mode = m;
		robotRun = r;
		
		header = loadHeader();
		contents = new MenuScroll("cont", cMax, cX, cY);
		options = new MenuScroll("opt", oMax, oX, oY);
		labels = new String[5];
	}
	
	public Screen(ScreenMode m, String header, RobotRun r) {
		mode = m;
		robotRun = r;
		
		this.header = header;
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 199);
		labels = new String[5];
	}
	
	public Screen(ScreenMode m, String header, RobotRun r, int cMax, int cX,
			int cY, int oMax, int oX, int oY) {
		
		mode = m;
		robotRun = r;
		
		this.header = header;
		contents = new MenuScroll("cont", cMax, cX, cY);
		options = new MenuScroll("opt", oMax, oX, oY);
		labels = new String[5];
	}
	
	public abstract void actionArrowDn();
	public abstract void actionArrowLt();
	public abstract void actionArrowRt();
	public abstract void actionArrowUp();
	
	public abstract void actionBkspc();
	public abstract void actionEntr();
	
	public abstract void actionF1();
	public abstract void actionF2();
	public abstract void actionF3();
	public abstract void actionF4();
	public abstract void actionF5();
	
	//Button actions
	public abstract void actionKeyPress(char key);
	
	public int getContentColIdx() { return contents.getColumnIdx(); }
	public int getContentIdx() { return contents.getLineIdx(); }
	public MenuScroll getContents() { return contents; }
	public int getContentStart() { return contents.getRenderStart(); }
	//Used for displaying screen text
	public String getHeader() { return header; }
	public String[] getLabels() { return labels; }
	public int getOptionIdx() { return options.getLineIdx(); }
	public MenuScroll getOptions() { return options; }
	public int getOptionStart() { return options.getRenderStart(); }
	
	public ScreenState getScreenState() {
		ScreenState s = new ScreenState(mode, contents.getLineIdx(), contents.getColumnIdx(),
				contents.getRenderStart(), options.getLineIdx(), options.getRenderStart());
		
		return s;
	}
	
	/**
	 * Loads the data registers of the given robotic arm into a list of display
	 * lines, which can be rendered onto the pendant screen.
	 * 
	 * @param r	The robotic arm, from which to load the data registers
	 * @return	The list of display lines representing the given robot's data
	 * 			registers
	 */
	public ArrayList<DisplayLine> loadDataRegisters(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			DataRegister reg = r.getDReg(idx);

			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register value (* if uninitialized)
			String regEntry = "*";

			if (reg.value != null) {
				// Display Register value
				regEntry = String.format("%4.3f", reg.value);

			} else {
				regEntry = "*";
			}

			lines.add(new DisplayLine(idx, 0 , regLbl, regEntry));
		}
		
		return lines;
	}
	
	/**
	 * Complies a list of display lines that represent the default end effector
	 * offsets for the given robot.
	 * 
	 * @param robot	The robot, of which to use the default tool tip offsets
	 * @return		The list of display lines, which represent the values of
	 * 				the given robot's default tool tip offsets
	 */
	public ArrayList<DisplayLine> loadEEToolTipDefaults(RoboticArm robot) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 0; idx < robot.numOfEndEffectors(); ++idx) {
			IORegister ioReg = robot.getIOReg(idx + 1);
			PVector defToolTip = robot.getToolTipDefault(idx);
			String lineStr = String.format("%s = (%4.3f, %4.3f, %4.3f)",
					ioReg.comment, defToolTip.x, defToolTip.y, defToolTip.z); 
			
			lines.add(new DisplayLine(idx, 0, lineStr));
		}
		
		return lines;
	}
	
	/**
	 * Converts the frames TCP and orientation offset into a set of display
	 * lines to render on the pendant screen.
	 * 
	 * @param tFrame	The tool frame to render on the pendant screen
	 * @return			The display lines that represent the given frame's TCP
	 * 					and orientation offset
	 */
	public ArrayList<DisplayLine> loadFrameDetail(ToolFrame tFrame) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		String[] fields = tFrame.toLineStringArray();
		
		for (String field : fields) {
			lines.add(new DisplayLine(-1, 0, field));
		}
		
		return lines;
	}
	
	/**
	 * Converts the frame's origin and orientation offset into a set of display
	 * lines, so that the frame's values can be shown on the pendant screen.
	 * 
	 * @param uFrame	The user frame to render on the pendant screen
	 * @return			The display lines that represent the given frame's
	 * 					origin and orientation offset
	 */
	public ArrayList<DisplayLine> loadFrameDetail(UserFrame uFrame) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		String[] fields = uFrame.toLineStringArray();
		
		for (String field : fields) {
			lines.add(new DisplayLine(-1, 0, field));
		}
		
		return lines;
	}
	
	/**
	 * Compiles the list of all of the frames corresponding to the given
	 * coordinate frame type (Tool or User), in a textual format, so that they
	 * can be rendered on the pendant screen.
	 * 
	 * @param r				The robot of which to use the frames
	 * @param coordFrame	TOOL for tool frames, or USER for user frames
	 * @return				The list of display liens corresponding to the
	 * 						specified frame list
	 */
	public ArrayList<DisplayLine> loadFrames(RoboticArm r, CoordFrame coordFrame) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				DisplayLine line = new DisplayLine(idx, 0,
						String.format("TOOL %s", r.toolLabel(idx)));
				lines.add(line);
			}

		} else {
			// Display User frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				DisplayLine line = new DisplayLine(idx, 0,
						String.format("USER %s", r.userLabel(idx)));
				lines.add(line);
			}
		}
		
		return lines;
	}
	
	/**
	 * Complies a of list of display lines, which represents the instructions
	 * defined by the given program p.
	 * 
	 * @param p				The program of which to use the instructions
	 * @param includeEND	Whether to include an END line marker at the end of
	 * 						the program's list of instructions
	 * @return				The list of display lines representing the given
	 * 						program's list of instructions
	 */
	public ArrayList<DisplayLine> loadInstructions(Program p, boolean
			includeEND) {
		
		ArrayList<DisplayLine> instruct_list = new ArrayList<>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;
		
		int size = p.getNumOfInst();
		
		for (int i = 0; i < size; i += 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstAt(i);
			int xPos = 10;

			// Add line number
			if (instr == null) {
				line.add(String.format("%d) ...", i + 1));
				instruct_list.add(line);
				continue;
			} else if (instr.isCommented()) {
				line.add("//" + Integer.toString(i + 1) + ")");
			} else {
				line.add(Integer.toString(i + 1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth * Fields.CHAR_WDTH + tokenOffset;
			
			if (instr instanceof MotionInstruction) {
				Boolean isRobotAt = robotRun.isRobotAtPostn(i);
				
				if (isRobotAt != null && isRobotAt) {
					line.add("@");
					
				} else {
					// Add a placeholder for the '@' symbol
					line.add("\0");
				}
				
				xPos += Fields.CHAR_WDTH + tokenOffset;
			}
			
			String[] fields = instr.toStringArray();

			for (int j = 0; j < fields.length; j += 1) {
				String field = fields[j];
				xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;

				if (field.equals("\n") && j != fields.length - 1) {
					instruct_list.add(line);
					if (instr instanceof SelectStatement) {
						xPos = 11 * Fields.CHAR_WDTH + 3 * tokenOffset;
					} else {
						xPos = 3 * Fields.CHAR_WDTH + 3 * tokenOffset;
					}

					line = new DisplayLine(i, xPos);
					xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;
				} else if (xPos > Fields.PENDANT_SCREEN_WIDTH - 10) {
					instruct_list.add(line);
					xPos = 2 * Fields.CHAR_WDTH + tokenOffset;

					line = new DisplayLine(i, xPos);
					field = ": " + field;
					xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;
				}

				if (!field.equals("\n")) {
					line.add(field);
				}
			}

			instruct_list.add(line);
		}
		
		if (includeEND && p.getNumOfInst() < Program.MAX_SIZE) {
			DisplayLine endl = new DisplayLine(size);
			endl.add("[End]");
	
			instruct_list.add(endl);
		}
		
		return instruct_list;
	}
	
	/**
	 * Compiles a list of the given robot's I/O registers in the format for I/O
	 * Instruction creation pendant screen.
	 * 
	 * @param r	The robot, of which to use the I/O registers
	 * @return	The list of display lines representing the given robot's I/O
	 * 			registers and states
	 */
	public ArrayList<DisplayLine> loadIORegInst(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 1; idx <= r.numOfEndEffectors(); idx += 1) {
			IORegister ioReg = r.getIOReg(idx);
			
			String col0 = String.format("IO[%2d:%-10s] = ", idx,
					ioReg.comment);
			lines.add(new DisplayLine(idx, 0, col0, "ON", "OFF"));
		}
		
		return lines;
	}
	
	/**
	 * Compiles a list of the given robot's I/O registers in the format for the
	 * I/O register navigation pendant screen.
	 * 
	 * @param r	The robot, of which to use the I/O registers
	 * @return	The list of display lines representing current state of the
	 * 			given robot's I/O registers
	 */
	public ArrayList<DisplayLine> loadIORegNav(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 1; idx <= r.numOfEndEffectors(); ++idx) {
			IORegister ioReg = r.getIOReg(idx);
			String col0 = String.format("IO[%2d:%-10s] = ", idx,
					ioReg.comment);
			lines.add(new DisplayLine(idx, 0, col0, ioReg.getState() ?
					"ON" : "OFF"));
		}
		
		return lines;
	}
	
	public ArrayList<DisplayLine> loadMacros() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		RoboticArm r = robotRun.getActiveRobot();
		
		for (int i = 0; i < r.numOfMacros(); i += 1) {
			String[] strArray = r.getMacro(i).toStringArray();
			disp.add(new DisplayLine(i, Integer.toString(i + 1), strArray[0], strArray[1], strArray[2]));
		}
		
		return disp;
	}
	
	public ArrayList<DisplayLine> loadManualFunct() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		RoboticArm r = robotRun.getActiveRobot();

		for (int i = 0; i < r.numOfMacros(); i += 1) {
			Macro m = r.getMacro(i);
			if (m.isManual()) {
				String manFunct = m.toString();
				disp.add(new DisplayLine(i, (i + 1) + " " + manFunct));
			}
		}
		
		return disp;
	}
	
	/**
	 * Compiles a list of display lines that represent the position registers
	 * of the given robot.
	 * 
	 * @param r	The robot of which to use the position registers
	 * @return	The list of display lines representing the position registers
	 * 			associated with the given robot
	 */
	public ArrayList<DisplayLine> loadPositionRegisters(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			PositionRegister reg = r.getPReg(idx);
			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register edit prompt (* if uninitialized)
			String regEntry = (reg.point == null) ? "*" : "...Edit...";
			
			lines.add( new DisplayLine(idx, 0, regLbl, regEntry) );
		}
		
		return lines;
	}

	/**
	 * Compiles a list of display lines, which represent the list of programs
	 * associated with the given robot.
	 * 
	 * @param r	The robot of which to use the programs
	 * @return	The list of display lines representing the programs associated
	 * 			with the given robot
	 */
	public ArrayList<DisplayLine> loadPrograms(RoboticArm r) {
		ArrayList<DisplayLine> progList = null;
		
		if (r != null) {
			progList = new ArrayList<>();
			// Get a list of program names for the given robot
			for (int idx = 0; idx < r.numOfPrograms(); ++idx) {
				DisplayLine line = new DisplayLine(idx, 0,
						r.getProgram(idx).getName());
				progList.add(line);
			}
			
		}
		
		return progList;
	}
	
	public void printScreenInfo() {
		Fields.debug("Current screen: ");
		Fields.debug("\tMode: " + mode.name());
		Fields.debug("\tRow: " + contents.getLineIdx() + ", col: " + contents.getColumnIdx() +
				", RS: " + contents.getRenderStart());
		Fields.debug("\tOpt row: " + options.getLineIdx() + ", opt RS: " + options.getRenderStart());
	}
	
	public void setContentIdx(int i) { contents.setLineIdx(i); }
	
	//Loads given set of screen state variables 
	public void setScreenIndices(int contLine, int col, int contRS, int optLine, int optRS) {
		contents.setLineIdx(contLine);
		contents.setColumnIdx(col);
		contents.setRenderStart(contRS);
		
		options.setLineIdx(optLine);
		options.setRenderStart(optRS);
	}
	
	public void updateScreen() {
		contents.clear();
		options.clear();
		
		loadContents();
		loadOptions();
		loadLabels();
	}
	
	public void updateScreen(ScreenState s) {
		updateScreen();
		loadVars(s);
		
		printScreenInfo();
	}
	
	@Override
	public String toString() {
		if (this.mode == null) {
			return "UNKNOWN";
		}
		
		return mode.toString();
	}

	protected abstract void loadContents();

	//Sets text for each screen
	protected abstract String loadHeader();
	protected abstract void loadLabels();
	protected abstract void loadOptions();
	protected abstract void loadVars(ScreenState s);
}
