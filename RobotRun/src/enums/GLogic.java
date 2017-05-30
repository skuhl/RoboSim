package enums;

import robot.DrawAction;

/**
 * Defines the draw actions of a graphics object that require no data storage.
 * 
 * @author Joshua Hooker
 */
public enum GLogic implements DrawAction {
	RESET_MAT, PUSH_MAT, POP_MAT, DRAW_MODEL;
}
