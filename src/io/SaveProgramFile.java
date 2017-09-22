package io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import programming.Program;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class SaveProgramFile implements Runnable {
	
	private Program progRef;
	private File dest;
	
	/**
	 * TODO comment this
	 * 
	 * @param progRef
	 * @param dest
	 */
	public SaveProgramFile(Program progRef, File dest) {
		this.progRef = progRef;
		this.dest = dest;
	}
	
	@Override
	public void run() {
		try {
			if (!dest.exists()) {
				// Create the file if it does not exist
				dest.createNewFile();
			}
			
			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);
			
			DataManagement.saveProgram(progRef, dataOut);

			dataOut.close();
			out.close();

		} catch (IOException IOEx) {
			// An error occurred with writing to dest
			System.err.printf("%s is corrupt!\n", dest.getName());;
		}
	}

}
