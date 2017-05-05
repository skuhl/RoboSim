package robot;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import geom.Fixture;
import geom.Part;
import geom.WorldObject;
import processing.core.PApplet;

/**
 * A storage class for a collection of objects with an associated name for the collection.
 */
public class Scenario implements Iterable<WorldObject>, Cloneable {
	private String name;
	/**
	 * A combine list of Parts and Fixtures
	 */
	private final ArrayList<WorldObject> objList;

	/**
	 * Create a new scenario of the given name.
	 */
	public Scenario(String n) {
		name = n;
		objList = new ArrayList<>();
	}

	/**
	 * Adds a number suffix to the given name, so that the name is unique amonst the names of all the other world
	 * objects in the given list. So, if the given name is 'block' and objects with names 'block', 'block1', and
	 * 'block2' exist in wldObjList, then the new name will be 'block3'.
	 * 
	 * @param originName  The origin name of the new world object
	 * @param eldObjList  The list of world objects, of wixh to check names
	 * @returning         A unique name amongst the names of the existing world objects in the given list, that
	 *                    contains the original name as a prefix
	 */
	private <T extends WorldObject> String addSuffixForDuplicateName(String originName, ArrayList<T> wldObjList) {
		int nameLen = originName.length();
		ArrayList<Integer> suffixes = new ArrayList<>();

		for (T wldObj : wldObjList) {
			String objName = wldObj.getName();
			int objNameLen = objName.length();

			if (objNameLen > nameLen) {
				String namePrefix = objName.substring(0, nameLen),
						nameSuffix = objName.substring(nameLen, objNameLen);
				// Find all strings that have the given name as a prefix and an integer value suffix
				if (namePrefix.equals(originName) && Pattern.matches("[0123456789]+", nameSuffix)) {
					int suffix = Integer.parseInt(nameSuffix),
							insertIdx = 0;
					// Store suffixes in increasing order
					while (insertIdx < suffixes.size() && suffix > suffixes.get(insertIdx)) {
						++insertIdx;
					}

					if (insertIdx == suffixes.size()) {
						suffixes.add(suffix);
					} else {
						suffixes.add(insertIdx, suffix);
					}
				}
			}
		}
		// Determine the minimum suffix value
		int suffix = 0;

		if (suffixes.size() == 1 && suffixes.get(0) == 0) {
			// If the only stirng with a suffix has a suffix of '0'
			suffix = 1;

		} else if (suffixes.size() >= 2) {
			int idx = 0;

			while ((idx + 1) < suffixes.size()) {
				// Find the first occurance of a gap between to adjacent suffix values (if any)
				if ((suffixes.get(idx + 1) - suffixes.get(idx)) > 1) {
					break;
				}

				++idx;
			}

			suffix = suffixes.get(idx) + 1;
		}
		// Concatenate the origin name with the new suffix
		return String.format("%s%d", originName, suffix);
	}

	/**
	 * Add the given world object to the scenario. Though, if the name of
	 * the given world object does not only contain letter and number
	 * characters, then the object is not added to either list.
	 * 
	 * @param newObject  The object to be added to either the Part or Fixture
	 *                   list
	 * @returning        Whether the object was added to a list or not
	 */
	public boolean addWorldObject(WorldObject newObject) {
		if (newObject == null || objList.contains(newObject)) {
			// Ignore nulls and duplicates
			if (newObject == null) {
				PApplet.println("New Object is null");
			} else {
				PApplet.println("New Object is: " + newObject.toString());
			}

			return false;
		}

		String originName = newObject.getName();

		if (originName.length() > 16) {
			// Base name length caps at 16 charcters
			newObject.setName( originName.substring(0, 16) );
			originName = newObject.getName();
		}

		if (Pattern.matches("[a-zA-Z0-9]+", originName)) {

			if (findObjectWithName(originName, objList) != null) {
				// Keep names unique
				newObject.setName( addSuffixForDuplicateName(originName, objList) );
			}

			// TODO add in alphabetical order
			objList.add(newObject);
			return true;
		}

		return false;
	}

	/**
	 * Only adds the given world objects that are non-null and do
	 * not already exist in the scenario.
	 * 
	 * @param newObjs  The world objects to add to the scenario
	 */
	public void addWorldObjects(WorldObject... newObjs) {

		for (WorldObject obj : newObjs) {
			addWorldObject(obj);
		}
	}

	@Override
	public Object clone() {
		Scenario copy = new Scenario(name);
		ArrayList<Fixture> fixtures = new ArrayList<>();
		ArrayList<Part> parts = new ArrayList<>();


		for (WorldObject obj : this) {
			try {
				// Add copies of all the objects in this scenario
				WorldObject newObj = (WorldObject)obj.clone();
				copy.addWorldObject(newObj);
				
				// Keep track of all fixtures and parts with non-null references
				if (newObj instanceof Fixture) {
					fixtures.add( (Fixture)newObj );
	
				} else if (newObj instanceof Part) {
					Part p = (Part)newObj;
	
					if (p.getFixtureRef() != null) {
						parts.add( (Part)newObj );
					}
				}
			
			} catch (NullPointerException NPEx) {/* Invalid source for model shape */}
		}

		// Update fixture references of new parts
		for (Part p : parts) {
			String refName = p.getFixtureRef().getName();
			p.setFixtureRef(null);

			for (Fixture f : fixtures) {
				if (f.getName().equals( refName )) {
					p.setFixtureRef(f);
				}
			}
		}

		return copy;
	}

	/**
	 * Attempts to find the world object, in the given list, with the given name. If no such object exists,
	 * then null is returned, otherwise the object with the given name is returned.
	 * 
	 * @param tgtName     The name of the world object to find
	 * @param wldObjList  The list of world objects to check
	 * @returning         The object with the given name, if it exists in the given list, or null.
	 */
	private <T extends WorldObject> WorldObject findObjectWithName(String tgtName, ArrayList<T> wldObjList) {

		if (tgtName != null && wldObjList != null) {

			for (T obj : wldObjList) {
				// Determine if the object exists
				if (obj != null && obj.getName().equals(tgtName)) {
					return obj;
				}
			}
		}

		return null;
	}

	public String getName() { return name; }

	/**
	 * Return the world object that corresponds to the given index in
	 * the list of world objects contained in this scenario, or null
	 * if the index is invalid.
	 * 
	 * @param idx  A valid index
	 * @returning  The world object, at the given index in the list,
	 *             or null
	 */
	public WorldObject getWorldObject(int idx) {
		if (idx >= 0 && idx < size()) {
			return objList.get(idx);
		}

		return null;
	}
	
	public ArrayList<WorldObject> getObjectList() {
		return objList;
	}

	@Override
	public Iterator<WorldObject> iterator() {
		return objList.iterator();
	}

	/**
	 * Delete the given world object from the correct object
	 * list, if it exists in the list.
	 * 
	 * @returning  0 if the object was removed succesfully,
	 *             1 if the object did not exist in the scenario,
	 *             2 if the object was a Fixture that was removed
	 *                from the scenario and was referenced by at
	 *                least one Part in the scenario
	 */
	public int removeWorldObject(WorldObject toRemove) {
		if (toRemove == null) {
			return 1;
		}

		int ret;
		// Remove a fixture from the list
		boolean removed = objList.remove(toRemove);

		ret = (removed) ? 0 : 1;

		if (removed && toRemove instanceof Fixture) {
			// Remove the reference from all Part objects associated with this fixture
			for (WorldObject obj : objList) {

				if (obj instanceof Part) {
					Part part = (Part)obj;

					if (part.getFixtureRef() == toRemove) {
						part.setFixtureRef(null);
						ret = 2;
					}
				}
			}
		}

		return ret;
	}

	/**
	 * Attempt to remove the given set of world objects from the scenario.
	 * 
	 * @param tgtObjs  The objects to remove from the scenario
	 * @returning      The number of the given objects that were successfully
	 *                 removed from the scenario
	 */
	public int removeWorldObjects(WorldObject... tgtObjs) {
		int objsRemoved = 0;

		for (WorldObject tgt : tgtObjs) {
			int ret = removeWorldObject(tgt);
			// Keep track of the number of given targets that were successfully removed
			if (ret == 0 || ret == 2) {
				++objsRemoved;
			}
		}

		return objsRemoved;
	}
	
	/**
	 * Replaces the world object, in the scenario, which has the same name as
	 * the given world object. If no world object with the given world
	 * object's name exists in the scenario, then the object is added to the
	 * scenario.
	 * 
	 * @param newObj	The new world object
	 * @return			The object that was replaced, or null if the newObj is
	 * 					null or was added to the scenario
	 */
	public WorldObject put(WorldObject newObj) {
		if (newObj != null) {
			WorldObject replaced = null;
			
			for (int idx = 0; idx < objList.size(); ++idx) {
				// Find the world object with the same name as newObj
				if (objList.get(idx).getName().equals(newObj.getName())) {
					replaced = objList.set(idx, newObj);
				}
			}
			
			// Update all part references to the replaced fixture
			if (replaced instanceof Fixture) {
				for (WorldObject w : objList) {
					if (w instanceof Part && ((Part) w).getFixtureRef() == replaced) {
						((Part)w).setFixtureRef((Fixture)newObj);
					}
				}
			}
			
			if (replaced != null) {
				return replaced;
			}
			
			addWorldObject(newObj);
		}
		
		return null;
	}

	/**
	 * Return the color of all the object's bounding
	 * boxes to normal (green).
	 */
	public void resetObjectHitBoxColors() {
		for (WorldObject wldObj : objList) {
			if (wldObj instanceof Part) {
				// Reset all Part bounding-box colors
				((Part)wldObj).setBBColor(RobotRun.getInstance().color(0, 255, 0));
			}
		}
	}

	public void setName(String newName) { name = newName; }
	public int size() { return objList.size(); }

	@Override
	public String toString() { return name; }

	/**
	 * Updates the collision detection of all the Parts in the scenario,
	 * using the given ArmModel to detect collisions between world objects
	 * and the armModel, and draws every object.
	 */
	public void updateAndDrawObjects(RoboticArm model) {
		int numOfObjects = objList.size();

		for (int idx = 0; idx < numOfObjects; ++idx) {
			WorldObject wldObj = objList.get(idx);

			if (wldObj instanceof Part) {
				Part p = (Part)wldObj;

				/* Update the transformation matrix of an object held by the Robotic Arm */
				if(model != null && p == model.held && model.modelInMotion()) {
					RobotRun.getInstance().pushMatrix();
					RobotRun.getInstance().resetMatrix();

					/***********************************************
					     Moving a part with the Robot:
					
					     P' = R^-1 x E' x E^-1 x P
					
					     where:
					     P' - new part local orientation
					     R  - part fixture reference orientation
					     E' - current Robot end effector orientation
					     E  - previous Robot end effector orientation
					     P  - current part loval orientation
					 ***********************************************/

					Fixture refFixture = p.getFixtureRef();

					if (refFixture != null) {
						refFixture.removeCoordinateSystem();
					}

					RobotRun.applyModelRotation(model, model.getJointAngles());

					float[][] invEETMatrix = RobotRun.invertHCMatrix(RobotRun.getActiveRobot().getOldOrientation());
					RobotRun.getInstance().applyMatrix(invEETMatrix[0][0], invEETMatrix[1][0], invEETMatrix[2][0], invEETMatrix[0][3],
							invEETMatrix[0][1], invEETMatrix[1][1], invEETMatrix[2][1], invEETMatrix[1][3],
							invEETMatrix[0][2], invEETMatrix[1][2], invEETMatrix[2][2], invEETMatrix[2][3],
							0,                 0,                   0,                  1);

					p.applyCoordinateSystem();
					// Update the world object's position and orientation
					p.setLocalCoordinateSystem();
					p.updateAbsoluteOrientation();
					RobotRun.getInstance().popMatrix();
				}

				/* Collision Detection */
				if(RobotRun.getInstance().areOBBsDisplayed()) {
					if( model != null && model.checkObjectCollision(p) ) {
						p.setBBColor(RobotRun.getInstance().color(255, 0, 0));
					}

					// Detect collision with other objects
					for(int cdx = idx + 1; cdx < objList.size(); ++cdx) {

						if (objList.get(cdx) instanceof Part) {
							Part p2 = (Part)objList.get(cdx);

							if(p.collision(p2)) {
								// Change hit box color to indicate Object collision
								p.setBBColor(RobotRun.getInstance().color(255, 0, 0));
								p2.setBBColor(RobotRun.getInstance().color(255, 0, 0));
								break;
							}
						}
					}

					if (model != null && p != model.held && model.canPickup(p)) {
						// Change hit box color to indicate End Effector collision
						p.setBBColor(RobotRun.getInstance().color(0, 0, 255));
					}
				}

				if (p == RobotRun.getInstance().getManager().getSelectedWO()) {
					p.setBBColor(RobotRun.getInstance().color(255, 255, 0));
				}
			}
			// Draw the object
			wldObj.draw();
		}
	}
}