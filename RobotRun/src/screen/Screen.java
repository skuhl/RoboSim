package screen;

import java.util.ArrayList;

import core.RobotRun;
import enums.CoordFrame;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import global.Fields;
import processing.core.PVector;
import programming.Instruction;
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
	protected final RobotRun robotRun;
	
	protected final String header;
	protected MenuScroll contents;
	protected MenuScroll options;
	protected String[] labels;
	
	public Screen(ScreenMode m, String header, RobotRun r) {
		mode = m;
		robotRun = r;
		
		this.header = header;
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
	}
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		
		header = loadHeader();
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
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
	
	//Used for displaying screen text
	public String getHeader() { return header; }
	public MenuScroll getContents() { return contents; }
	public MenuScroll getOptions() { return options; }
	public String[] getLabels() { return labels; }
	
	public int getContentIdx() { return contents.getLineIdx(); }
	public int getContentColIdx() { return contents.getColumnIdx(); }
	public int getContentStart() { return contents.getRenderStart(); }

	public int getOptionIdx() { return options.getLineIdx(); }
	public int getOptionStart() { return options.getRenderStart(); }
	
	public void setContentIdx(int i) { contents.setLineIdx(i); }
	
	public ScreenState getScreenState() {
		ScreenState s = new ScreenState(mode, contents.getLineIdx(), contents.getColumnIdx(),
				contents.getRenderStart(), options.getLineIdx(), options.getRenderStart());
		
		return s;
	}
		
	//Loads given set of screen state variables 
	public void setScreenIndices(int contLine, int col, int contRS, int optLine, int optRS) {
		contents.setLineIdx(contLine);
		contents.setColumnIdx(col);
		contents.setRenderStart(contRS);
		
		options.setLineIdx(optLine);
		options.setRenderStart(optRS);
	}
	
	public void printScreenInfo() {
		System.out.println("Current screen: ");
		System.out.println("\tMode: " + mode.name());
		System.out.println("\tRow: " + contents.getLineIdx() + ", col: " + contents.getColumnIdx() +
				", RS: " + contents.getRenderStart());
		System.out.println("\tOpt row: " + options.getLineIdx() + ", opt RS: " + options.getRenderStart());
	}
	
	//Sets text for each screen
	protected abstract String loadHeader();
	protected abstract void loadContents();
	protected abstract void loadOptions();
	protected abstract void loadLabels();
	protected abstract void loadVars(ScreenState s);
		
	//Button actions
	public abstract void actionKeyPress(char key);
	public abstract void actionUp();
	public abstract void actionDn();
	public abstract void actionLt();
	public abstract void actionRt();
	public abstract void actionEntr();
	public abstract void actionBkspc();
	public abstract void actionF1();
	public abstract void actionF2();
	public abstract void actionF3();
	public abstract void actionF4();
	public abstract void actionF5();
	
	public ArrayList<DisplayLine> loadMacros() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		RoboticArm r = robotRun.getActiveRobot();
		
		for (int i = 0; i < r.getMacroList().size(); i += 1) {
			String[] strArray = r.getMacroList().get(i).toStringArray();
			disp.add(new DisplayLine(i, Integer.toString(i + 1), strArray[0], strArray[1], strArray[2]));
		}
		
		return disp;
	}

	public ArrayList<DisplayLine> loadManualFunct() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		RoboticArm r = robotRun.getActiveRobot();
		int macroNum = 0;

		for (int i = 0; i < r.getMacroList().size(); i += 1) {
			if (r.getMacroList().get(i).isManual()) {
				String manFunct = r.getMacroList().get(i).toString();
				disp.add(new DisplayLine(macroNum, (macroNum + 1) + " " + manFunct));
				macroNum += 1;
			}
		}
		
		return disp;
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
	 * TODO comment this
	 * 
	 * @param robot
	 * @return
	 */
	public ArrayList<DisplayLine> loadEEToolTipDefaults(RoboticArm robot) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 0; idx < robotRun.getActiveRobot().numOfEndEffectors(); ++idx) {
			IORegister ioReg = robotRun.getActiveRobot().getIOReg(idx + 1);
			PVector defToolTip = robotRun.getActiveRobot().getToolTipDefault(idx);
			String lineStr = String.format("%s = (%4.3f, %4.3f, %4.3f)",
					ioReg.comment, defToolTip.x, defToolTip.y, defToolTip.z); 
			
			lines.add(new DisplayLine(idx, 0, lineStr));
		}
		
		return lines;
	}
	
	/**
	 * TODO
	 * 
	 * @param r
	 * @param coordFrame
	 * @param fdx
	 * @return
	 */
	public ArrayList<DisplayLine> loadFrameDetail(RoboticArm r, CoordFrame coordFrame, int fdx) {
		
		ArrayList<DisplayLine> lines = new ArrayList<>();
		Frame f = null;
		
		if (coordFrame == CoordFrame.TOOL) {
			f = r.getToolFrame(fdx);
			
		} else if (coordFrame == CoordFrame.USER) {
			f = r.getUserFrame(fdx);
		}
		
		if (f != null) {
			String[] fields = f.toLineStringArray();
			
			for (String field : fields) {
				lines.add(new DisplayLine(-1, 0, field));
			}
			
		} else {
			// Invalid coordFrame or frame index
			lines.add(new DisplayLine(-1, 0, String.format("CoordFrame=%s", coordFrame) ));
			lines.add(new DisplayLine(-1, 0, String.format("Frame Index=%d", fdx) ));
		}
		
		return lines;
	}
	
	/**
	 * TODO
	 * 
	 * @param r
	 * @param coordFrame
	 * @return
	 */
	public ArrayList<DisplayLine> loadFrames(RoboticArm r, CoordFrame coordFrame) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = r.getToolFrame(idx).toLineStringArray();
				String line = String.format("%-4s %s", String.format("%d)",
						idx + 1), strArray[0]);
				
				lines.add(new DisplayLine(idx, 0, line));
				lines.add(new DisplayLine(idx, 38, String.format("%s",
						strArray[1])));
			}

		} else {
			// Display User frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = r.getUserFrame(idx).toLineStringArray();
				String line = String.format("%-4s %s", String.format("%d)",
						idx + 1), strArray[0]);
				
				lines.add(new DisplayLine(idx, 0, line));
				lines.add(new DisplayLine(idx, 38, String.format("%s", strArray[1])));
			}
		}
		
		return lines;
	}

	// prepare for displaying motion instructions on screen
	public ArrayList<DisplayLine> loadInstructions(Program p, boolean includeEND) {
		ArrayList<DisplayLine> instruct_list = new ArrayList<>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;
		
		int size = p.getNumOfInst();
		
		for (int i = 0; i < size; i += 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.get(i);
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
		
		if (includeEND) {
			DisplayLine endl = new DisplayLine(size);
			endl.add("[End]");
	
			instruct_list.add(endl);
		}
		
		return instruct_list;
	}

	/**
	 * TODO
	 * 
	 * @param r
	 * @return
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
	 * TODO
	 * 
	 * @param r
	 * @return
	 */
	public ArrayList<DisplayLine> loadIORegNav(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 1; idx <= r.numOfEndEffectors(); ++idx) {
			IORegister ioReg = r.getIOReg(idx);
			String col0 = String.format("IO[%2d:%-10s] = ", idx,
					ioReg.comment);
			lines.add(new DisplayLine(idx, 0, col0, (ioReg.getState() == 0) ?
					"OFF" : "ON") );
		}
		
		return lines;
	}

	/**
	 * TODO
	 * 
	 * @param f
	 * @param teachMethod
	 * @return
	 */
	public ArrayList<DisplayLine> loadPointList(Frame f, int teachMethod) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		boolean validMethod = teachMethod == 0 || teachMethod == 1;
		
		
		if (f instanceof ToolFrame && validMethod) {
			
			String out = (f.getPoint(0) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(0, 0, "First Approach Point: " + out));
			
			out = (f.getPoint(1) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(1, 0, "Second Approach Point: " + out));
			
			out = (f.getPoint(2) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(2, 0, "Third Approach Point: " + out));
			
			if (teachMethod == 1) {
				out = (f.getPoint(3) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(3, 0, "Orient Origin Point: " + out));
				
				out = (f.getPoint(4) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(4, 0, "X Axis Point: " + out));
				
				out = (f.getPoint(5) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(5, 0, "Y Axis Point: " + out));
			}
			
		} else if (f instanceof UserFrame && validMethod) {
			
			String out = (f.getPoint(0) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(0, 0, "Orient Origin Point: " + out));
			
			out = (f.getPoint(1) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(1, 0, "X Axis Point: " + out));
			
			out = (f.getPoint(2) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(2, 0, "Y Axis Point: " + out));
			
			if (teachMethod == 1) {
				out = (f.getPoint(3) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(3, 0, "Origin: " + out));
			}
			
		} else {
			lines.add(new DisplayLine(-1, 0,
					(f == null) ? "Null frame" : f.getClass().toString())
					);
			lines.add(new DisplayLine(-1, 0, String.format("Method: %d",
					teachMethod)));
		}
		
		return lines;
	}
	
	/**
	 * TODO
	 * 
	 * @param r
	 * @return
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
	 * TODO comment
	 * 
	 * @param rid
	 * @return
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
}
