package programming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import geom.Point;
import global.Fields;

public class Program implements Iterable<InstElement> {
	
	/**
	 * The maximum size for a program's instruction set.
	 */
	public static final int MAX_SIZE;
	
	/**
	 * The maximum number of past program modifications stored at one time.
	 */
	public static final int MAX_UNDO_SIZE;
	
	static {
		MAX_SIZE = 800;
		MAX_UNDO_SIZE = 50;
	}
	
	private final ArrayList<InstElement> instructions;
	
	/**
	 * The positions associated with this program, which are
	 * stored in reference to the current User frame
	 */
	private final HashMap<Integer, Point> LPosReg;
	
	private String name;
	
	private int nextID;
	private int nextPosition;

	public Program(String s) {
		name = s;
		
		LPosReg = new HashMap<>();
		instructions = new ArrayList<>();
		
		nextID = 0;
		nextPosition = 0;
	}
	
	public int addInstAt(int idx, Instruction inst) {
		if (instructions.size() < MAX_SIZE && idx >= 0 &&
				idx <= instructions.size()) {
			
			int nextID = getNextID();
			
			if (nextID >= 0) {
				InstElement e = new InstElement(nextID, inst);
				instructions.add(idx, e);
				
			} else {
				Fields.setMessage("Program is full!");
			}
			
			return nextID;
		}
		
		return -1;
	}
	
	public int addInstAtEnd(Instruction inst) {
		return addInstAt(instructions.size(), inst);
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
		Program copy = new Program(name);

		// Copy positions
		Set<Integer> posNums = LPosReg.keySet();

		for (Integer posNum : posNums) {
			copy.setPosition(posNum, LPosReg.get(posNum));
		}

		// Copy instructions
		for (InstElement e : instructions) {
			copy.addInstAtEnd(e.getInst().clone());
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
			Instruction inst = instructions.get(idx).getInst();
			// Check the current instruction
			if (inst instanceof LabelInstruction &&
					((LabelInstruction)inst).getLabelNum() == lblNum) {
				// Return the label's instruction index
				return idx;
			}
		}
		// A label with the given number does not exist
		return -1;
	}

	/**
	 * Returns the instruction element at the given index in the program's
	 * list of instructions.
	 * 
	 * @param idx	The index of the instruction element to get
	 * @return		The instruction element at the given index
	 */
	public InstElement get(int idx) {
		return instructions.get(idx);
	}
	
	/**
	 * Returns the instruction at the given index in the program's list of
	 * instructions.
	 * 
	 * @param idx	The index of the instruction to get
	 * @return		The instruction at the given index
	 */
	public Instruction getInstAt(int idx) {
		if (idx >= 0 && idx < instructions.size()) {
			return get(idx).getInst();
		}
		
		return null;
	}
	
	/**
	 * The label instruction with the given label number, if it exists in the
	 * program.
	 * 
	 * @param n	The number of the label to find
	 * @return	The label with the given number
	 */
	public LabelInstruction getLabel(int n) {
		for(InstElement e : instructions) {
			Instruction inst = e.getInst();
			
			if(inst instanceof LabelInstruction){
				if(((LabelInstruction)inst).getLabelNum() == n){
					return (LabelInstruction)inst;
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
	 * @return	The number of instructions in this program
	 */
	public int getNumOfInst() {
		return instructions.size();
	}

	/**
	 * @return	The number of positions associated with this program
	 */
	public int getNumOfLReg() {
		return LPosReg.size();
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

	@Override
	public Iterator<InstElement> iterator() {
		return instructions.iterator();
	}
	
	/**
	 * Replaces the instruction and ID of the instruction element at the given
	 * index in the program's list of instruction elements.
	 * 
	 * @param idx	The index at which to place the new instruction
	 * @param inst	The instruction to insert into the program
	 * @return		The instruction which was replaced
	 */
	public Instruction replaceInstAt(int idx, Instruction inst) {
		if (idx >= 0 && idx < instructions.size()) {
			InstElement e = get(idx);
			// Remove current instruction
			Instruction old = e.getInst();
			// Add the new instruction
			e.setElement(getNextID(), inst);
			
			return old;
		}
		
		return null;
	}
	
	/**
	 * Removes the instruction with the given ID, if such an instruction exists
	 * in this program.
	 * 
	 * @param id	The ID of the instruction to remove
	 * @return		The removed instruction element
	 */
	public InstElement rmInst(int id) {
		for (int idx = 0; idx < instructions.size(); ++idx) {
			InstElement e = instructions.get(idx);
			if (e.getID() == id) {
				// Remove the instruction's ID from the list ordering
				instructions.remove(idx);
				return e;
			}
		}
		
		// An instruction with the given ID does not exist
		return null;
	}
	
	/**
	 * Removes an instruction based on its order in the program execution.
	 * 
	 * @param idx	The list index of the instruction to remove
	 * @return		The instruction element at the given list index
	 */
	public InstElement rmInstAt(int idx) {
		InstElement removed = instructions.remove(idx);
		return removed;
	}
	
	
	/**
	 * Sets the name of this program.
	 * 
	 * @param n	The new name of this program
	 */
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

	/**
	 * Adds the given instruction element to the program's list of instruction
	 * elements at the given index.
	 * 
	 * @param idx	The index at which to add the given instruction element
	 * @param e		The element to add to this program
	 */
	protected void addAt(int idx, InstElement e) {
		if (idx >= 0 && idx < instructions.size()) {
			instructions.add(idx, e);
		}
	}
	
	/**
	 * Replaces the instruction element at the given index with the given
	 * instruction element.
	 * 
	 * @param idx	The index at which to put the given instruction element
	 * @param e		The element to put in this program
	 */
	protected void replace(int idx, InstElement e) {
		if (idx >= 0 && idx < instructions.size()) {
			instructions.set(idx, e);
		}
	}

	/**
	 * Returns the next unique ID for an instruction. If the ID is -1, then the
	 * program has reached its instruction capacity.
	 * 
	 * @return	The next unique ID for an instruction
	 */
	private int getNextID() {
		if (nextID < Integer.MAX_VALUE) {
			// Update the ID counter
			return nextID++;
		}
		
		// No next ID
		return -1;
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