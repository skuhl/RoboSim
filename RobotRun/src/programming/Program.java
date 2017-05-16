package programming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import geom.Point;
import global.Fields;
import robot.RobotRun;
import robot.RoboticArm;
import screen.DisplayLine;

public class Program implements Iterable<Instruction> {
	/**
	 * The positions associated with this program, which are
	 * stored in reference to the current User frame
	 */
	private final HashMap<Integer, Point> LPosReg;
	private final ArrayList<Instruction> instructions;
	
	private String name;
	private RoboticArm robot;
	private int nextPosition;

	public Program(String s, RoboticArm r) {
		name = s;
		robot = r;
		nextPosition = 0;
		LPosReg = new HashMap<>();
		instructions = new ArrayList<>();
	}

	public void addInstAtEnd(Instruction i) {
		instructions.add(i);
	}

	public void addInstAt(int idx, Instruction i) {
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
	@Override
	public Program clone() {
		Program copy = new Program(name, robot);

		// Copy positions
		Set<Integer> posNums = LPosReg.keySet();

		for (Integer posNum : posNums) {
			copy.setPosition(posNum, LPosReg.get(posNum));
		}

		// Copy instructions
		for (Instruction inst : instructions) {
			copy.addInstAtEnd(inst.clone());
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

	public Instruction getInstAt(int i){
		return instructions.get(i);
	}
	
	public int getNumOfInst() {
		return instructions.size();
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
	 * Get the position associated with the given index.
	 * 
	 * @param idx  The index corresponding to a position in the program
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

	public int getNumOfLReg() {
		return LPosReg.size();
	}
	
	public RoboticArm getRobot() {
		return robot;
	}
	
	@Override
	public Iterator<Instruction> iterator() {
		return instructions.iterator();
	}

	public Instruction replaceInstAt(int idx, Instruction i) {
		return instructions.set(idx, i);
	}
	
	public ArrayList<DisplayLine> printInstrList() {
		ArrayList<DisplayLine> instruct_list = new ArrayList<>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;

		Program p = this;
		int size = p.getNumOfInst();

		for(int i = 0; i < size; i+= 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstAt(i);
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
				Point ee_point = RobotRun.nativeRobotEEPoint(robot, robot.getJointAngles());
				Point instPt = a.getVector(p);

				if(instPt != null && ee_point.position.dist(instPt.position) < (robot.getLiveSpeed() / 100f)) {
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
				} else if(xPos > Fields.PENDANT_SCREEN_WIDTH - 10) {
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
	
	public Instruction rmInstAt(int idx) {
		return instructions.remove(idx);
	}

	public void setName(String n) { name = n; }

	/**
	 * Add the given point at the position defined by the given index, overriding
	 * the previous entry if the position is initialized.
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
	 * Updates the position associated with the motion instruction's secondary
	 * position index. The old point associated with the position is returned.
	 * 
	 * @param instIdx	The index of a motion instruction in this program
	 * @param newPt		The new point to store at the motion instruction's
	 * 					associated position
	 * @return			The previous point stored at the position associated
	 * 					with the instruction
	 * @throws ClassCastException	If the instruction indexed at instIdx is
	 * 								not a motion instruction
	 * @throws NullPointerException	If the given point is null or the instruction
	 * 								indexed at instIdx is not a motion type
	 * 								instruction
	 */
	public Point updateMCInstPosition(int instIdx, Point newPt) throws
		ClassCastException, NullPointerException {
		
		MotionInstruction mInst = (MotionInstruction) getInstAt(instIdx);
		MotionInstruction sndMInst = mInst.getSecondaryPoint();
		
		if (mInst.getMotionType() != Fields.MTYPE_CIRCULAR || sndMInst == null) {
			throw new NullPointerException(
					String.format("Instruction at %d is not a circular motion instruction!",
					instIdx)
				);	
		}
		
		if (newPt != null) {
			int posNum = sndMInst.getPositionNum();
			
			if (posNum == -1) {
				// In the case of an uninitialized position
				posNum = nextPosition;
				sndMInst.setPositionNum(posNum);
			}
			
			return setPosition(posNum, newPt);
		}
		
		throw new NullPointerException("arg, newPt, cannot be null for updateMInstPosition()!");
	}
	
	/**
	 * Updates the position associated with the motion instruction at the given
	 * instruction index to the given point. The old point associated with the
	 * position is returned.
	 * 
	 * @param instIdx	The index of a motion instruction in this program
	 * @param newPt		The new point to store at the motion instruction's
	 * 					associated position
	 * @return			The previous point stored at the position associated
	 * 					with the instruction
	 * @throws ClassCastException	If the instruction indexed at instIdx is
	 * 								not a motion instruction
	 * @throws NullPointerException	If the given point is null
	 */
	public Point updateMInstPosition(int instIdx, Point newPt) throws
		ClassCastException, NullPointerException {
		
		if (newPt != null) {
			MotionInstruction mInst = (MotionInstruction)getInstAt(instIdx);
			int posNum = mInst.getPositionNum();
			
			if (posNum == -1) {
				// In the case the instruction's position is unintialized
				posNum = nextPosition;
				mInst.setPositionNum(posNum);
			}
			
			return setPosition(posNum, newPt);
		}
		
		throw new NullPointerException("arg, newPt, cannot be null for updateMInstPosition()!");
	}

	/**
	 * Updates the index of the lowest uninitialized position in the program.
	 * In te case that a program has the mazimum number of positions (1000),
	 * then the nextPosition will point to some initialized position.
	 */
	private void updateNextPosition() {
		if (LPosReg.size() >= 1000) {
			// Move to the next position if the position set is full
			nextPosition = (nextPosition + 1) % 1000;

		} else {
			// Find the next empty position
			while (LPosReg.get(nextPosition) != null) {
				nextPosition = (nextPosition + 1) % 1000;
			}
		}
	}
}