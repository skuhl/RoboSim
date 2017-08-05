package geom;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import core.RobotRun;
import processing.core.PApplet;
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
	
	private static PApplet appRef;
	
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
	
	/**
	 * Build a PShape object from the contents of the given .stl source file
	 * stored in /RobotRun/data/.
	 * 
	 * @throws NullPointerException
	 *             if the given filename does not pertain to a valid .stl file
	 *             located in RobotRun/data/
	 * @throws ClassCastException
	 * 				if the application does not use processing's opengl
	 * 				graphics library
	 */
	public static MyPShape loadSTLModel(String filename, int fill) throws NullPointerException, ClassCastException {
		ArrayList<Triangle> triangles = new ArrayList<>();
		byte[] data = appRef.loadBytes(filename);
	
		int n = 84; // skip header and number of triangles
	
		while (n < data.length) {
			Triangle t = new Triangle();
			for (int m = 0; m < 4; m++) {
				byte[] bytesX = new byte[4];
				bytesX[0] = data[n + 3];
				bytesX[1] = data[n + 2];
				bytesX[2] = data[n + 1];
				bytesX[3] = data[n];
				n += 4;
				byte[] bytesY = new byte[4];
				bytesY[0] = data[n + 3];
				bytesY[1] = data[n + 2];
				bytesY[2] = data[n + 1];
				bytesY[3] = data[n];
				n += 4;
				byte[] bytesZ = new byte[4];
				bytesZ[0] = data[n + 3];
				bytesZ[1] = data[n + 2];
				bytesZ[2] = data[n + 1];
				bytesZ[3] = data[n];
				n += 4;
				t.components[m] = new PVector(ByteBuffer.wrap(bytesX).getFloat(), ByteBuffer.wrap(bytesY).getFloat(),
						ByteBuffer.wrap(bytesZ).getFloat());
			}
			triangles.add(t);
			n += 2; // skip meaningless "attribute byte count"
		}
		
		MyPShape mesh = new MyPShape((PGraphicsOpenGL)appRef.getGraphics(), PShape.GEOMETRY);
		mesh.beginShape(RobotRun.TRIANGLES);
		mesh.noStroke();
		mesh.fill(fill);
		
		for (Triangle t : triangles) {
			mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
			mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
			mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
			mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
		}
		
		mesh.endShape();
	
		return mesh;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param appRef
	 */
	public static void setAppRef(PApplet appRef) {
		MyPShape.appRef = appRef;
	}
}
