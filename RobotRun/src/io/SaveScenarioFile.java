package io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import geom.Scenario;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class SaveScenarioFile implements Runnable {
	
	private Scenario scenario;
	private File dest;
	
	/**
	 * TODO comment this
	 * 
	 * @param scenario
	 * @param dest
	 */
	public SaveScenarioFile(Scenario scenario, File dest) {
		this.scenario = scenario;
		this.dest = dest;
	}
	
	@Override
	public void run() {
		try {
			if (!dest.exists()) {
				dest.createNewFile();
			}
			
			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);
			// Save the scenario data
			DataManagement.saveScenario(scenario, dataOut);
			
			dataOut.close();
			out.close();
			
		} catch (IOException IOEx) {
			// Issue with writing or opening a file
			System.err.printf("Error with file %s.\n", dest.getName());
		}
	}

}
