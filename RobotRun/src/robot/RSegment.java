package robot;

import java.util.ArrayList;

import enums.GLogic;
import geom.BoundingBox;
import geom.MyPShape;
import processing.core.PGraphics;

/**
 * Defines a segment of the robotic arm with its bounding boxes and model.
 * 
 * @author Joshua Hooker
 */
public class RSegment {
	
	/**
	 * The bounding boxes associated with this segment.
	 */
	protected final ArrayList<BoundingBox> OBBS;
	
	/**
	 * The model defining the shape of this segment.
	 */
	protected MyPShape model;
	
	/**
	 * The bounding boxes associated with this segment.
	 */
	private final ArrayList<DrawAction> DRAW_ACTIONS;
	
	/**
	 * Defines a segment with the given model, bounding boxes, and draw
	 * actions.
	 * 
	 * @param model			The model defining the shape of this segment
	 * @param obbs			The bounding boxes associated with this segment
	 * @param drawActions	The bounding boxes associated with this segment
	 */
	public RSegment(MyPShape model, ArrayList<BoundingBox> obbs,
			ArrayList<DrawAction> drawActions) {
		
		OBBS = obbs;
		DRAW_ACTIONS = drawActions;
		
		this.model = model;
	}
	
	/**
	 * Applies the given graphics action to the given graphics object for this
	 * segment. See classes implementing the GraphicsDraw interface for more
	 * details about specific actions.
	 * 
	 * @param g			The graphics object upon which to act
	 * @param action	The action to apply on g
	 */
	protected void executeAction(PGraphics g, DrawAction action) {
		
		if (action instanceof GLogic) {
			
			if (action == GLogic.DRAW_MODEL) {
				// Draw the segment's model
				g.shape(model);
				
			} else if (action == GLogic.RESET_MAT) {
				// Reset the top matrix
				g.resetMatrix();
				
			} else if (action == GLogic.PUSH_MAT) {
				// Push the top matrix
				g.pushMatrix();
				
			} else if (action == GLogic.POP_MAT) {
				// Pop the top matrix
				g.popMatrix();
			}
			
		} else if (action instanceof GDrawRShape) {
			// Draw the shape associated with the given action
			((GDrawRShape) action).shape.draw(g);
			
		} else if (action instanceof GTranslation) {
			GTranslation tAction = (GTranslation)action;
			// Apply the translation associated with the given action
			g.translate(tAction.transX, tAction.transY, tAction.transZ);
			
		} else if (action instanceof GRotateX) {
			/* Rotate around the x-axis the rotation associated with the given
			 * action */
			g.rotateX( ((GRotateX) action).rotation );
			
		} else if (action instanceof GRotateY) {
			/* Rotate around the y-axis the rotation associated with the given
			 * action */
			g.rotateY( ((GRotateY) action).rotation );
			
		} else if (action instanceof GRotateZ) {
			/* Rotate around the z-axis the rotation associated with the given
			 * action */
			g.rotateZ( ((GRotateZ) action).rotation );
			
		} else {
			// Invalid action
			System.err.printf("Invalid graphics action: %s!\n",
					action.getClass());
		}
		
	}
	
	/**
	 * Executes the list of draw actions associated with this segment.
	 * 
	 * @param g			The graphics object used to draw this segment
	 * @param drawOBBs	Whether to draw the segment's bounding boxes or not
	 */
	public void draw(PGraphics g, boolean drawOBBs) {
		/* TODO REMOVE AFTER REFACTOR */
		g.shape(model);
		
		/**
		for (DrawAction action : DRAW_ACTIONS) {
			executeAction(g, action);
		}
		/**/
	}
}
