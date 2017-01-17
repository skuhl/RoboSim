package geom;
/**
 * A class used as temporary storage of a Part when it is first loaded from the scenarios file.
 */
public class LoadedPart {
	public Part part;
	public String referenceName;

	public LoadedPart(Part p) {
		part = p;
		referenceName = null;
	}

	public LoadedPart(Part p, String refName) {
		part = p;
		referenceName = refName;
	}
}