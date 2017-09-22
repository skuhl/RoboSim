package robot;

import core.RobotRun;
import geom.Point;
import geom.RQuaternion;
import processing.core.PVector;

/**
 * TODO comment this
 * 
 * @author Joshua Hooker
 */
public class DynamicLinearInterpolation extends LinearMotion {
	
	private Point start;
	private Point dest;
	private final float totalDist;
	private float distTraveled;
	
	/**
	 * 
	 * @param start
	 * @param end
	 */
	public DynamicLinearInterpolation(Point start, Point dest) {
		this.start = start;
		this.dest = dest;
		PVector p1 = start.position;
		PVector p2 = dest.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = dest.orientation;
		totalDist = RobotRun.dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) +
				100f * q1.dist(q2);
		distTraveled = 0f;
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		if (!hasFault() && dest != null) {
			if (Math.abs(distTraveled - totalDist) >= 0.0005f) {
				distTraveled += RoboticArm.motorSpeed * robot.getLiveSpeed() / 6000f;
				
				if (distTraveled > totalDist) {
					distTraveled = totalDist;	
				}
				
				PVector p1 = start.position;
				PVector p2 = dest.position;
				RQuaternion q1 = start.orientation;
				RQuaternion q2 = dest.orientation;
				float mu = distTraveled / totalDist;
				
				// Calculate the next position based on the robot's current speed
				PVector nextPos = new PVector(p1.x * (1 - mu) + (p2.x * mu),
						p1.y * (1 - mu) + (p2.y * mu), p1.z * (1 - mu) + (p2.z * mu));
				RQuaternion nextOrien = RQuaternion.minSLERP(q1, q2, mu);
				int ret = robot.jumpTo(nextPos, nextOrien);
				
				if (ret == 0) {
					// Return the distance left
					return (int)(totalDist - distTraveled);
					
				} else {
					setFault(true);
				}
				
			} else {
				// The target position is close enough
				halt();
			}
		}
		
		return 0;
	}

	@Override
	public void halt() {
		dest = null;
	}

	@Override
	public boolean hasMotion() {
		return dest != null;
	}

}
