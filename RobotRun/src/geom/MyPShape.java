package geom;

import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

/**
 * An extension of Processing's PShapeOpenGL class that has a copy method.
 * 
 * @author Joshua Hooker
 */
public class MyPShape extends PShapeOpenGL {
	
	/**
	 * Processing, I hate you
	 */
	protected PGraphicsOpenGL g;
	
	/**
	 * Creates a PShape with the given graphics object and type.
	 * 
	 * @param g		The graphics object of the application
	 * @param type	One three PShape type constants (GROUP, PATH, GEOMETRY)
	 */
	public MyPShape(PGraphicsOpenGL g, int type) {
		super(g, type);
		
		if (g.is3D()) {
			set3D(true);
		}
		
		this.g = g;
	}
	
	/**
	 * Inspired by the processing thread:
	 * https://forum.processing.org/two/discussion/752/how-to-copy-a-pshape-object
	 */
	@Override
	public MyPShape clone() {
		MyPShape copy = new MyPShape(g, GEOMETRY);
		
		// Copy fill color and all vertices
		copy.beginShape(PShape.TRIANGLES);
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
	
}
