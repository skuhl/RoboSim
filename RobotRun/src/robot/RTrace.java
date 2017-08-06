package robot;

import java.util.LinkedList;

import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Defines a trace: a set of line segments, which are composed of lines drawn
 * between many points.
 * 
 * @author Joshua Hooker
 */
public class RTrace {
	
	/**
	 * The maximum number of points, which will be stored at one time for
	 * drawing the trace.
	 */
	private int maxSize;
	
	/**
	 * The points, which makeup the trace segments.
	 */
	private LinkedList<PVector> ptBuffer;
	
	/**
	 * Defines an empty point buffer with a max size of 10000 points.
	 */
	public RTrace() {
		ptBuffer = new LinkedList<>();
		maxSize = 10000;
	}
	
	/**
	 * Adds the given point to the end of the point buffer. If the point buffer
	 * is already at its maximum size, then the  first point in the trace is
	 * removed to accommodate the new point. Null points are allowed.
	 * 
	 * @param pt	The point to add to the trace
	 */
	public void addPt(PVector pt) {
		if (ptBuffer.size() > maxSize) {
			// Remove beginning points when the buffer reaches maximum size
			ptBuffer.removeFirst();
		}
		
		ptBuffer.addLast(pt);
	}
	
	/**
	 * Removes all points from the point buffer.
	 */
	public void clear() {
		ptBuffer.clear();
	}
	
	/**
	 * Draws the trace with all the points stored in the point buffer.
	 * 
	 * @param g	The graphics object to use when drawing the trace
	 */
	public void draw(PGraphics g) {
		if (ptBuffer.size() > 1) {
			PVector lastPt = null;
			
			g.pushStyle();
			g.stroke(0);
			g.strokeWeight(3);
			
			for(PVector curPt : ptBuffer) {
				if (lastPt != null && curPt != null) {
					/* Draw lines between each non-null point stored in the
					 * point buffer */
					g.line(lastPt.x, lastPt.y, lastPt.z, curPt.x, curPt.y,
							curPt.z);
				}
				
				lastPt = curPt;
			}
			
			g.popStyle();
		}
	}
	
	/**
	 * The last point in the trace point buffer, or null if the trace is empty.
	 * 
	 * @return	The last point defined for the trace
	 */
	public PVector getLastPt() {
		if (ptBuffer.isEmpty()) {
			return null;
		}
		
		return ptBuffer.getLast();
	}
	
	/**
	 * @return	the current maximum size for the point buffer
	 */
	public int getMaxSize() {
		return maxSize;
	}
	
	/**
	 * @return	Does this trace have no points?
	 */
	public boolean isEmpty() {
		return ptBuffer.isEmpty();
	}
	
	/**
	 * @return	the current size of the point buffer
	 */
	public int numOfPts() {
		return ptBuffer.size();
	}
	
	/**
	 * Sets the maximum size for the point buffer. If the point buffer is
	 * larger than the given value, then points will be remove from the front
	 * of the list until the size is equal to the new max size.
	 * 
	 * @param newMax	A positive integer defining the maximum number of points
	 * 					to store for the trace
	 */
	public void setMaxSize(int newMax) {
		maxSize = Math.max(0, newMax);
		// Remove points from buffer to match the new max size
		while (ptBuffer.size() > newMax) {
			ptBuffer.removeFirst();
		}
	}
	
}
