package io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import geom.Scenario;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class LoadScenarioFile implements Runnable {

	private Scenario[] scenarioList;
	private int idx;
	private File src;

	/**
	 * TODO comment this
	 * 
	 * @param scenarioList
	 * @param idx
	 * @param src
	 */
	public LoadScenarioFile(Scenario[] scenarioList, int idx, File src) {

		this.scenarioList = scenarioList;
		this.idx = idx;
		this.src = src;
	}

	@Override
	public void run() {
		if (src.exists()) {
			try {
				FileInputStream in = new FileInputStream(src);
				DataInputStream dataIn = new DataInputStream(in);
				/* Store the loaded scenario at the defined index in the
				 * list of scenarios */
				Scenario s = DataManagement.loadScenario(dataIn);
				scenarioList[idx] = s;

				dataIn.close();
				in.close();

			} catch (FileNotFoundException FNFEx) {
				System.err.printf("File %s does not exist in \\tmp\\scenarios.\n", src.getName());

			} catch (IOException IOEx) {
				System.err.printf("File, %s, in \\tmp\\scenarios is corrupt!\n", src.getName());
			}

		}
	}

}
