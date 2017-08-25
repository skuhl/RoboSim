package geom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import global.Fields;

/**
 * Defines a set of world objects that are rendered in the same scene.
 * 
 * @author Joshua Hooker
 */
public class Scenario implements Iterable<WorldObject>, Cloneable {
	
	/**
	 * The maximum number of objects allowed in a single scenario.
	 */
	public static final int MAX_SIZE = 20;
	
	private boolean gravity;
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
		objList = new ArrayList<WorldObject>();
		gravity = false;
	}

	/**
	 * Add the given world object to the scenario if the scenario is not null
	 * and the given reference is not null.
	 * 
	 * @param newObject  The object to add to this scenario
	 * @return			 0	the object was added successfully,
	 * 					 1	the object is a null reference,
	 * 					 2	the scenario is full
	 */
	public int addWorldObject(WorldObject newObject) {
		
		if (newObject == null) {
			return 1;
			
		} else if (isFull()) {
			return 2;
		}
		
		objList.add(newObject);
		return 0;
	}
	
	/**
	 * Attempts to add all the given objects to the scenario.
	 * 
	 * @param newObjs  The world objects to add to the scenario
	 */
	public void addWorldObjects(WorldObject... newObjs) {

		for (WorldObject obj : newObjs) {
			if (isFull()) {
				break;
			}
			
			addWorldObject(obj);
		}
	}
	
	@Override
	public Object clone() {
		Scenario copy = new Scenario(name);
		HashMap<String, Fixture> nameToFixtureMap = new HashMap<>();
		ArrayList<Part> parts = new ArrayList<>();
		ArrayList<String> parentNames = new ArrayList<>();
		
		for (WorldObject obj : this) {
			try {
				// Add copies of all the objects in this scenario
				WorldObject newObj = obj.clone();
				copy.addWorldObject(newObj);
				
				// Keep track of all fixtures and parts with non-null references
				if (newObj instanceof Fixture) {
					nameToFixtureMap.put(newObj.getName(), (Fixture)newObj);
	
				} else if (newObj instanceof Part) {
					Part p = (Part)newObj;
	
					if (p.getParent() != null) {
						parts.add( (Part)newObj );
						parentNames.add(p.getParent().getName());
					}
				}
			
			} catch (NullPointerException NPEx) {/* Invalid source for model shape */}
		}

		// Update fixture references of new parts
		for (int idx = 0; idx < parts.size(); ++idx) {
			Part p = parts.get(idx);
			String parentName = parentNames.get(idx);
			Fixture parent = nameToFixtureMap.get(parentName);
			
			if (parent != null) {
				parent.addDependent(p);
			}
		}

		return copy;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @return
	 */
	public WorldObject findWOWithName(String name) {
		for (WorldObject wo : objList) {
			if (wo.getName().equals(name)) {
				return wo;
			}
		}
		
		// No match found
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

	public boolean isGravity() {
		return gravity;
	}
	
	/**
	 * Has the scenario's number of parts and fixtures reached the maximum size
	 * defined for scenarios?
	 * 
	 * @return	The scenario has reached the maximum number of objects allowed
	 * 			for a scenario
	 */
	public boolean isFull() {
		return objList.size() >= MAX_SIZE;
	}

	@Override
	public Iterator<WorldObject> iterator() {
		return objList.iterator();
	}

	/**
	 * Delete the given world object from the correct object
	 * list, if it exists in the list.
	 * 
	 * @return  0 if the object was removed successfully,
	 *          1 if the object did not exist in the scenario,
	 *          2 if the object was a Fixture that was removed
	 *             from the scenario and was referenced by at
	 *             least one Part in the scenario
	 */
	public int removeWorldObject(WorldObject toRemove) {
		if (toRemove == null) {
			return 1;
		}

		int ret;
		// Remove a fixture from the list
		boolean removed = objList.remove(toRemove);
		ret = (removed) ? 0 : 1;

		if (removed) {
			if (toRemove instanceof Fixture) {
				/* Remove the reference from all Part objects associated with this
				 * fixture */
				((Fixture)toRemove).clearDependents();
				
			} else {
				// Remove parent reference
				Part p = (Part) toRemove;
				Fixture parent = p.getParent();
				
				if (parent != null) {
					parent.removeDependent(p);
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
	 * Return the color of all the object's bounding
	 * boxes to normal (green).
	 */
	public void resetObjectHitBoxColors() {
		for (WorldObject wldObj : objList) {
			if (wldObj instanceof Part) {
				// Reset all Part bounding-box colors
				((Part)wldObj).setBBColor(Fields.OBB_DEFAULT);
			}
		}
	}

	public void setName(String newName) { name = newName; }
	
	public int size() { return objList.size(); }

	public boolean toggleGravity() {
		gravity = !gravity;
		return gravity;
	}

	@Override
	public String toString() { return name; }
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @return
	 */
	public int validateWOName(String name) {
		// Names only consist of letters and numbers
		if (name == null || !Pattern.matches("[a-zA-Z0-9]+", name)) {
			// Invalid characters
			return 1;
		}
		
		if (name.length() > 16) {
			// Name is too long
			return 2;
		}

		for (WorldObject wo : objList) {
			if (wo.getName().equals(name)) {
				// Duplicate name
				return 3;
			}
		}

		return 0;
	}
	
	/**
	 * Adds a number suffix to the given name, so that the name is unique amonst the names of all the other world
	 * objects in the given list. So, if the given name is 'block' and objects with names 'block', 'block1', and
	 * 'block2' exist in wldObjList, then the new name will be 'block3'.
	 * 
	 * @param originName  The origin name of the new world object
	 * @param wldObjList  The list of world objects, of wixh to check names
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
			// If the only string with a suffix has a suffix of '0'
			suffix = 1;

		} else if (suffixes.size() >= 2 && suffixes.get(0) == 0) {
			int idx = 0;

			while ((idx + 1) < suffixes.size()) {
				// Find the first occurrence of a gap between to adjacent suffix values (if any)
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
}
