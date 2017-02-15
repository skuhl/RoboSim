package programming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


import geom.Point;
import global.Fields;
import robot.RobotRun;
import window.DisplayLine;

public class Program {
	String name;
	//TODO have program only reference the robot to which it is assigned rather
	//than the whole program instance
	private RobotRun robotRun;
	private int nextPosition;

	/**
	 * The positions associated with this program, which are
	 * stored in reference to the current User frame
	 */
	HashMap<Integer, Point> LPosReg;
	ArrayList<Instruction> instructions;

	public Program(String s, RobotRun r) {
		name = s;
		robotRun = r;
		nextPosition = 0;
		LPosReg = new HashMap<Integer, Point>();
		instructions = new ArrayList<Instruction>();
	}

	public void addInstruction(Instruction i) {
		instructions.add(i);
	}

	public void addInstruction(int idx, Instruction i) {
		instructions.add(idx, i);
	}

	/**
	 * Add a new position and the index of the next available position
	 * 
	 * @param pt  The new position to add to the program
	 */
	public void addPosition(Point pt) {
		LPosReg.put(nextPosition, pt);
		updateNextPosition();
	}

	/**
	 * Remove all the positinos from this program
	 */
	public void clearPositions() {
		LPosReg.clear();
		nextPosition = 0;
	}

	/**
	 * Return an independent replica of this program object.
	 */
	public Program clone() {
		Program copy = new Program(name, robotRun);

		// Copy positions
		Set<Integer> posNums = LPosReg.keySet();

		for (Integer posNum : posNums) {
			copy.setPosition(posNum, LPosReg.get(posNum));
		}

		// Copy instructions
		for (Instruction inst : instructions) {
			copy.addInstruction(inst.clone());
		}

		return copy;
	}

	/**
	 * Determines if a label with the given number exists in the program and returns its
	 * instruction index if it does.
	 * 
	 * @param lblNum  The target label index
	 * @returning     The instruction index of the target label, or -1 if it exists
	 */
	public int findLabelIdx(int lblNum) {

		for (int idx = 0; idx < instructions.size(); ++idx) {
			Instruction inst = instructions.get(idx);
			// Check the current instruction
			if (inst instanceof LabelInstruction && ((LabelInstruction)inst).getLabelNum() == lblNum) {
				// Return the label's instruction index
				return idx;
			}
		}
		// A label with the given number does not exist
		return -1;
	}

	public Instruction getInstruction(int i){
		return instructions.get(i);
	}

	public ArrayList<Instruction> getInstructions() {
		return instructions;
	}

	public LabelInstruction getLabel(int n){    
		for(Instruction i: instructions){
			if(i instanceof LabelInstruction){
				if(((LabelInstruction)i).getLabelNum() == n){
					return (LabelInstruction)i;
				}
			}
		}

		return null;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the index of the next position to normally insert a new position.
	 */
	public int getNextPosition() {
		return nextPosition;
	}

	/**
	 * Get the position assocaited with the given index.
	 * 
	 * @param idx  The indexx corresopnding to a position in the program
	 */
	public Point getPosition(int idx) {
		return LPosReg.get(idx);
	}

	/**
	 * Get all the indices of all initialized positions
	 */
	public Set<Integer> getPositionNums() {
		return LPosReg.keySet();
	}

	public int getRegistersLength() {
		return LPosReg.size();
	}

	public void overwriteInstruction(int idx, Instruction i) {
		instructions.set(idx, i);
	}
	
	public ArrayList<DisplayLine> printInstrList() {
		ArrayList<DisplayLine> instruct_list = new ArrayList<DisplayLine>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;

		Program p = this;
		int size = p.getInstructions().size();

		for(int i = 0; i < size; i+= 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstruction(i);
			int xPos = 10;

			// Add line number
			if (instr == null) {
				line.add( String.format("%d) ...", i+1) );
				continue;
			} else if(instr.isCommented()) {
				line.add("//"+Integer.toString(i+1) + ")");
			} else {
				line.add(Integer.toString(i+1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth*Fields.CHAR_WDTH + tokenOffset;

			if(instr instanceof MotionInstruction) {
				// Show '@' at the an instrution, if the Robot's position is close to that position stored in the instruction's register
				MotionInstruction a = (MotionInstruction)instr;
				Point ee_point = RobotRun.nativeRobotEEPoint(robotRun.getActiveRobot(), robotRun.getActiveRobot().getJointAngles());
				Point instPt = a.getVector(p);

				if(instPt != null && ee_point.position.dist(instPt.position) < (robotRun.getLiveSpeed() / 100f)) {
					line.add("@");
				}
				else {
					line.add("\0");
				}

				xPos += Fields.CHAR_WDTH + tokenOffset;
			}

			String[] fields = instr.toStringArray();

			for (int j = 0; j < fields.length; j += 1) {
				String field = fields[j];
				xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;

				if(field.equals("\n") && j != fields.length - 1) {
					instruct_list.add(line);
					if(instr instanceof SelectStatement) {
						xPos = 11*Fields.CHAR_WDTH + 3*tokenOffset;
					} else {
						xPos = 3*Fields.CHAR_WDTH + 3*tokenOffset;
					}

					line = new DisplayLine(i, xPos);
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				} else if(xPos > Fields.DISPLAY_WIDTH - 10) {
					instruct_list.add(line);
					xPos = 2*Fields.CHAR_WDTH + tokenOffset;

					line = new DisplayLine(i, xPos);
					field = ": " + field;
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				}

				if(!field.equals("\n")) {
					line.add(field);
				}
			}

			instruct_list.add(line);
		}

		return instruct_list;
	}

	public void setName(String n) { name = n; }

	/**
	 * Add the given point at the position defined by the given index, overriding
	 * the previous entry if the position isinitialized.
	 * 
	 * @param idx  The index where to store the new position
	 * @param  pt  The new position value
	 * @return     The previous position value
	 */
	public Point setPosition(int idx, Point pt) {
		if (idx >= 0 && idx < 1000) {
			Point prevPt = LPosReg.get(idx);
			LPosReg.put(idx, pt);

			if (idx == nextPosition) {
				// update the next position index if necessary
				updateNextPosition();
			}

			return prevPt;
		}

		return null;
	}

	public int size() {
		return instructions.size();
	}

	/**
	 * Updates the index of the lowest uninitialized position in the program.
	 * In te case that a program has the mazimum number of positions (1000),
	 * then the nextPosition will point to some initialized position.
	 */
	private void updateNextPosition() {
		if (LPosReg.size() >= 1000) {
			// Move to the next position if the position set is full
			++nextPosition;

		} else {
			// Find the next empty position
			while (LPosReg.get(nextPosition) != null) {
				++nextPosition;
			}
		}
	}
} // end Program class