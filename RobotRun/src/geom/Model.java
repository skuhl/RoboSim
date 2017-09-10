package geom;

import processing.core.PConstants;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

/**
 * The definition of a model, which is loaded from a .STL model.
 * 
 * @author Joshua Hooker
 */
public class Model extends PShapeOpenGL {
	
	/**
	 * Processing, I hate you
	 */
	protected PGraphicsOpenGL g;
	
	/**
	 * The name of the file, from which this model was created.
	 */
	private final String filename;
	
	/**
	 * Creates a PShape with the given graphics object and type.
	 * 
	 * @param g			The graphics object of the application
	 * @param type		One three PShape type constants (GROUP, PATH, GEOMETRY)
	 * @param filename	
	 */
	public Model(PGraphicsOpenGL g, int type, String filename) {
		super(g, type);
		
		if (g.is3D()) {
			set3D(true);
		}
		
		this.g = g;
		this.filename = filename;
	}
	
	/**
	 * Inspired by the processing thread:
	 * https://forum.processing.org/two/discussion/752/how-to-copy-a-pshape-object
	 */
	@Override
	public Model clone() {
		Model copy = new Model(g, GEOMETRY, filename);
		
		// Copy fill color and all vertices
		copy.beginShape(PConstants.TRIANGLES);
		copy.noStroke();
		copy.fill(fillColor);
		
		for (int vdx = 0; vdx < getVertexCount(); ++vdx) {
			PVector n = getNormal(vdx);
			PVector v = getVertex(vdx);
			
			copy.normal(n.x, n.y, n.z);
			copy.vertex(v.x, v.y, v.z);
		}
		
		copy.endShape();
		
		return copy;
	}
	
	/**
	 * Returns the name of the file, from which this model was derived.
	 * 
	 * @return	This model's file name
	 */
	public String getFilename() {
		return filename;
	}
}
