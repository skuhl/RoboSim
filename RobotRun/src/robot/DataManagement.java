package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import geom.Box;
import geom.Cylinder;
import geom.DimType;
import geom.LoadedPart;
import geom.ModelShape;
import geom.Part;
import geom.Shape;
import geom.WorldObject;
import processing.core.PVector;

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
	
	
	private static int saveScenarioBytes(ArrayList<Scenario> scenarios, File dest) {
		if (!dest.exists()) {
			dest.mkdir();
			
		} else if (dest.exists() && !dest.isDirectory()) {
			// File must be a directory
			return 1;
		}
		
		File scenarioFile = null;
		
		try {
			Scenario active = RobotRun.getInstance().activeScenario;
			
			if (active != null) {
				// Save the name of the active scenario
				scenarioFile = new File(dest.getAbsolutePath() + "activeScenario.txt");
				
				if (!scenarioFile.exists()) {
					scenarioFile.createNewFile();
				}
				
				FileOutputStream out = new FileOutputStream(scenarioFile);
				DataOutputStream dataOut = new DataOutputStream(out);
				
				dataOut.writeUTF(active.getName());
				
				dataOut.close();
				out.close();
			}
			
			for (Scenario s : scenarios) {
				// Save each scenario in a separate file
				scenarioFile = new File(dest.getAbsolutePath() + s.getName() + ".scenario");
				
				if (!scenarioFile.exists()) {
					scenarioFile.createNewFile();
				}
				
				FileOutputStream out = new FileOutputStream(scenarioFile);
				DataOutputStream dataOut = new DataOutputStream(out);
				// Save the scenario data
				saveScenario(s, dataOut);
				
				dataOut.close();
				out.close();
			}
			
			return 0;
			
		} catch (IOException IOEx) {
			// Issue with writing or opening a file
			if (scenarioFile != null) {
				System.err.printf("Error with file %s.\n", scenarioFile.getName());
			}
			
			IOEx.printStackTrace();
			return 2;
		}
	}
	
	private static int loadScenarioBytes(ArrayList<Scenario> emptyList, File src) {
		
		// TODO
		
		return 0;
	}
	
	private static void saveScenario(Scenario s, DataOutputStream out) throws IOException {

		if (s == null) {
			// Indicate the value saved is null
			out.writeByte(0);

		} else {
			// Indicate the value saved is non-null
			out.writeByte(1);
			// Write the name of the scenario
			out.writeUTF(s.getName());
			// Save the number of world objects in the scenario
			out.writeInt( s.size() );

			for (WorldObject wldObj : s) {
				// Save all the world objects associated with the scenario
				saveWorldObject(wldObj, out);  
			}
		}
	}

	private static Scenario loadScenario(DataInputStream in) throws IOException, NullPointerException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the name of the scenario
			String name = in.readUTF();
			Scenario s = new Scenario(name);
			// An extra set of only the loaded fixtures
			ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
			// A list of parts which have a fixture reference defined
			ArrayList<LoadedPart> partsWithReferences = new ArrayList<LoadedPart>();

			// Read the number of objects in the scenario
			int size = in.readInt();
			// Read all the world objects contained in the scenario
			while (size-- > 0) {
				Object loadedObject = loadWorldObject(in);

				if (loadedObject instanceof WorldObject) {
					// Add all normal world objects to the scenario
					s.addWorldObject( (WorldObject)loadedObject );

					if (loadedObject instanceof Fixture) {
						// Save an extra reference of each fixture
						fixtures.add( (Fixture)loadedObject );
					}

				} else if (loadedObject instanceof LoadedPart) {
					LoadedPart lPart = (LoadedPart)loadedObject;

					if (lPart.part != null) {
						// Save the part in the scenario
						s.addWorldObject(lPart.part);

						if (lPart.referenceName != null) {
							// Save any part with a defined reference
							partsWithReferences.add(lPart);
						}
					}
				}
			}

			// Set all the Part's references
			for (LoadedPart lPart : partsWithReferences) {
				for (Fixture f : fixtures) {
					if (lPart.referenceName.equals(f.getName())) {
						lPart.part.setFixtureRef(f);
					}
				}
			}

			return s;
		}
	}

	private static void saveWorldObject(WorldObject wldObj, DataOutputStream out) throws IOException {

		if (wldObj == null) {   //<>// //<>// //<>// //<>// //<>// //<>//
			// Indicate that the value saved is null
			out.writeByte(0);

		} else {
			if (wldObj instanceof Part) {
				// Indicate that the value saved is a Part
				out.writeByte(1);
			} else if (wldObj instanceof Fixture) {
				// Indicate that the value saved is a Fixture
				out.writeByte(2);
			}

			// Save the name and form of the object
			out.writeUTF(wldObj.getName());
			saveShape(wldObj.getForm(), out);
			// Save the local orientation of the object
			savePVector(wldObj.getLocalCenter(), out);
			saveFloatArray2D(wldObj.getLocalOrientationAxes(), out);

			if (wldObj instanceof Part) {
				Part part = (Part)wldObj;
				String refName = "";

				savePVector(part.getOBBDims(), out);

				if (part.getFixtureRef() != null) {
					// Save the name of the part's fixture reference
					refName = part.getFixtureRef().getName();
				}

				out.writeUTF(refName);
			}
		}
	}

	private static Object loadWorldObject(DataInputStream in) throws IOException, NullPointerException {
		// Load the flag byte
		byte flag = in.readByte();
		Object wldObjFields = null;

		if (flag != 0) {
			// Load the name and shape of the object
			String name = in.readUTF();
			Shape form = loadShape(in);
			// Load the object's local orientation
			PVector center = loadPVector(in);
			float[][] orientationAxes = loadFloatArray2D(in);
			CoordinateSystem localOrientation = new CoordinateSystem();
			localOrientation.setOrigin(center);
			localOrientation.setAxes(orientationAxes);

			if (flag == 1) {
				// Load the part's bounding-box and fixture reference name
				PVector OBBDims = loadPVector(in);
				String refName = in.readUTF();

				if (refName.equals("")) {
					// A part object
					wldObjFields = new Part(name, form, OBBDims, localOrientation, null);
				} else {
					// A part object with its reference's name
					wldObjFields = new LoadedPart( new Part(name, form, OBBDims, localOrientation, null), refName );
				}

			} else if (flag == 2) {
				// A fixture object
				wldObjFields = new Fixture(name, form, localOrientation);
			} 
		}

		return wldObjFields;
	}

	private static void saveShape(Shape shape, DataOutputStream out) throws IOException {
		if (shape == null) {
			// Indicate the saved value is null
			out.writeByte(0);

		} else {
			if (shape instanceof Box) {
				// Indicate the saved value is a box
				out.writeByte(1);
			} else if (shape instanceof Cylinder) {
				// Indicate the value saved is a cylinder
				out.writeByte(2);
			} else if (shape instanceof ModelShape) {
				// Indicate the value saved is a complex shape
				out.writeByte(3);
			}

			// Write fill color value
			saveInteger(shape.getFillValue(), out);

			if (shape instanceof Box) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save length, height, and width of the box
				out.writeFloat(shape.getDim(DimType.LENGTH));
				out.writeFloat(shape.getDim(DimType.HEIGHT));
				out.writeFloat(shape.getDim(DimType.WIDTH));

			} else if (shape instanceof Cylinder) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save the radius and height of the cylinder
				out.writeFloat(shape.getDim(DimType.RADIUS));
				out.writeFloat(shape.getDim(DimType.HEIGHT));

			} else if (shape instanceof ModelShape) {
				ModelShape m = (ModelShape)shape;

				out.writeFloat(m.getDim(DimType.SCALE));
				// Save the source path of the complex shape
				out.writeUTF(m.getSourcePath()); 
			}
		}
	}

	private static Shape loadShape(DataInputStream in) throws IOException, NullPointerException {
		// Read flag byte
		byte flag = in.readByte();
		Shape shape = null;

		if (flag != 0) {
			// Read fiil color
			Integer fill = loadInteger(in);

			if (flag == 1) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float x = in.readFloat(),
						y = in.readFloat(),
						z = in.readFloat();
				// Create a box
				shape = new Box(fill, strokeVal, x, y, z);

			} else if (flag == 2) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float radius = in.readFloat(),
						hgt = in.readFloat();
				// Create a cylinder
				shape = new Cylinder(fill, strokeVal, radius, hgt);

			} else if (flag == 3) {
				float scale = in.readFloat();
				String srcPath = in.readUTF();

				// Creates a complex shape from the srcPath located in RobotRun/data/
				shape = new ModelShape(srcPath, fill, scale);
			}
		}

		return shape;
	}
	
	private static void saveInteger(Integer i, DataOutputStream out) throws IOException {

		if (i == null) {
			// Write byte flag
			out.writeByte(0);

		} else {
			// Write byte flag
			out.writeByte(1);
			// Write integer value
			out.writeInt(i);
		}
	}

	private static Integer loadInteger(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read integer value
			return in.readInt();
		}
	}
	
	private static void savePVector(PVector p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);
			// Write vector data
			out.writeFloat(p.x);
			out.writeFloat(p.y);
			out.writeFloat(p.z);
		}
	}

	private static PVector loadPVector(DataInputStream in) throws IOException {
		// Read flag byte
		int val = in.readByte();

		if (val == 0) {
			return null;

		} else {
			// Read vector data
			PVector v = new PVector();
			v.x = in.readFloat();
			v.y = in.readFloat();
			v.z = in.readFloat();
			return v;
		}
	}

	private static void saveRQuaternion(RQuaternion q, DataOutputStream out) throws IOException {
		if (q == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);

			for (int idx = 0; idx < 4; ++idx) {
				// Write each quaternion value
				out.writeFloat(q.getValue(idx));
			}
		}
	}

	private static RQuaternion loadRQuaternion(DataInputStream in) throws IOException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read values of the quaternion
			float w = in.readFloat(),
					x = in.readFloat(),
					y = in.readFloat(),
					z = in.readFloat();

			return new RQuaternion(w, x, y, z);
		}
	}

	private static void saveFloatArray(float[] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write list length
			out.writeInt(list.length);
			// Write each value in the list
			for (int idx = 0; idx < list.length; ++idx) {
				out.writeFloat(list[idx]);
			}
		}
	}

	private static float[] loadFloatArray(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the length of the list
			int len = in.readInt();
			float[] list = new float[len];
			// Read each value of the list
			for (int idx = 0; idx < list.length; ++idx) {
				list[idx] = in.readFloat();
			}

			return list;
		}
	}

	private static void saveFloatArray2D(float[][] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write the dimensions of the list
			out.writeInt(list.length);
			out.writeInt(list[0].length);
			// Write each value in the list
			for (int row = 0; row < list.length; ++row) {
				for (int col = 0; col < list[0].length; ++col) {
					out.writeFloat(list[row][col]);
				}
			}
		}
	}
	
	private static float[][] loadFloatArray2D(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the length of the list
			int numOfRows = in.readInt(),
					numOfCols = in.readInt();
			float[][] list = new float[numOfRows][numOfCols];
			// Read each value of the list
			for (int row = 0; row < list.length; ++row) {
				for (int col = 0; col < list[0].length; ++col) {
					list[row][col] = in.readFloat();
				}
			}

			return list;
		}
	}
}
