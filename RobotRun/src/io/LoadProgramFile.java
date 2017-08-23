package io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import programming.Program;
import robot.RoboticArm;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class LoadProgramFile implements Runnable {
	private RoboticArm robotRef;
	private Program[] listRef;
	private int idx;
	private File src;
	
	/**
	 * TODO comment this
	 * 
	 * @param robotRef
	 * @param listRef
	 * @param idx
	 * @param src
	 */
	public LoadProgramFile(RoboticArm robotRef, Program[] listRef, int idx,
			File src) {
		
		this.robotRef = robotRef;
		this.listRef = listRef;
		this.idx = idx;
		this.src = src;
	}
	
	@Override
	public void run() {
		
		if (src.exists()) {
			try {
				FileInputStream in = new FileInputStream(src);
				DataInputStream dataIn = new DataInputStream(in);
				// Load the program from the file
				Program p = DataManagement.loadProgram(robotRef, dataIn);
				// Insert the program in the program list at the specified index
				listRef[idx] = p;
				
				in.close();
				dataIn.close();
				
			} catch (FileNotFoundException FNFEx) {
				// Could not locate src
				System.err.printf("%s does not exist!\n", src.getName());

			} catch (EOFException EOFEx) {
				// Reached the end of src unexpectedly
				System.err.printf("End of file, %s, was reached unexpectedly!\n",
						src.getName());

			} catch (IOException IOEx) {
				// An error occurred with reading from src
				System.err.printf("%s is corrupt!\n", src.getName());
				
			} catch (ClassCastException CCEx) {
				/* An error occurred with casting between objects while loading a
				 * program's instructions */
				System.err.printf("%s is corrupt!\n", src.getName());
				
			} catch (NegativeArraySizeException NASEx) {
				// Issue with loading program points
				System.err.printf("%s is corrupt!\n", src.getName());
			}
			
		}
	}

}
