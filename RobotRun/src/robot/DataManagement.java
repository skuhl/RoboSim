package robot;

import java.io.File;
import java.util.ArrayList;

/**
 * Manages all the saving and loading of the program data to and from files.
 * All fields and methods are static, so no instance of the class is
 * necessary.
 * 
 * @author Joshua Hooker and Vincent Druckte
 * @version 1.0; 3 December 2016
 */
public abstract class DataManagement {
	
	private static final String parentDirectoryPath;
	private static final String[] subDirectoriesPaths;
	
	private static final File parentDirectory;
	private static final File[] robots;	
	
	static {
		parentDirectoryPath = "tmp/";
		/* Only programs, frames, and registers are associated with specific
		 * Robots, scenarios are shared amongst all Robots */
		subDirectoriesPaths = new String[] { "programs/",  "frames/" ,
				"registers/", "scenarios/" };
		
		parentDirectory = new File(parentDirectoryPath);
		
		if (parentDirectory.exists()) {
			// Read the robot directories
			robots = parentDirectory.listFiles();
			
		} else {
			robots = new File[0];
		}
	}
	
	public static ArrayList<ArmModel> loadState() {
		ArrayList<ArmModel> robotData = new ArrayList<ArmModel>();
		
		if (!parentDirectory.exists()) {
			// TODO return a list containing a new Robot
			return null;
		}
		
		// TODO load each Robot's programs, frames, and registers as well as the scenario data.
		
		return robotData;
	}
	
	public static void saveState(ArrayList<ArmModel> robotData) {
		// TODO save each robot's programs, frames, and registers as well as the scenario data
	}
	
	// TODO move saving and loading methods into this class from the RobotRun class
	// TODO modify the methods moved into this class from the RobotRun class to save and associated data with a specific ArmModel
}
