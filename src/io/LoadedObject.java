package io;

import geom.Fixture;
import geom.Part;
import geom.WorldObject;

/**
 * A class used as temporary storage of a Part when it is first loaded from the scenarios file.
 */
public class LoadedObject {
	public WorldObject obj;
	public String referenceName;

	public LoadedObject(Fixture f) {
		obj = f;
		referenceName = null;
	}

	public LoadedObject(Part p, String refName) {
		obj = p;
		referenceName = refName;
	}
}