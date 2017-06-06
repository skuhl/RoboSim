package robot;

import java.util.ArrayList;

import core.RobotRun;
import geom.Point;
import geom.RQuaternion;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * Defines the robot's target position and orientation motion, which uses
 * linear interpolation to move from the robot's current position and
 * orientation to the target position and orientation.
 * 
 * @author Joshua Hooker
 */
public class LinearInterpolation extends LinearMotion {
	
	/**
	 * Defines the set of points, through which to interpolate.
	 */
	private final ArrayList<Point> intermediatePositions;
	
	private float distBtwPts;
	private float speed;
	
	private int motionFrameCounter;
	private int interMotionIdx;
	
	/**
	 * Initializes intermediate positions.
	 */
	public LinearInterpolation() {
		super();
		intermediatePositions = new ArrayList<>();
		distBtwPts = 0f;
		speed = 0f;
		motionFrameCounter = 0;
		interMotionIdx = 0;
	}
	
	public void beginNewCircularMotion(Point start, Point inter, Point end, float distBtwPts, float speed) {
		reset(distBtwPts, speed);
		calculateArc(start, inter, end);
	}

	public void beginNewContinuousMotion(Point start, Point end, Point next, float p, float distBtwPts, float speed) {
		reset(distBtwPts, speed);
		calculateContinuousPositions(start, end, next, p);
	}

	public void beginNewLinearMotion(Point start, Point end, float distBtwPts, float speed) {
		reset(distBtwPts, speed);
		calculateIntermediatePositions(start, end);
	}
	
	private void calculateArc(Point start, Point inter, Point end) {
		PVector a = start.position;
		PVector b = inter.position;
		PVector c = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		// Calculate arc center point
		PVector[] plane = new PVector[3];
		plane = createPlaneFrom3Points(a, b, c);
		PVector center = circleCenter(vectorConvertTo(a, plane[0], plane[1], plane[2]),
				vectorConvertTo(b, plane[0], plane[1], plane[2]), vectorConvertTo(c, plane[0], plane[1], plane[2]));
		center = vectorConvertFrom(center, plane[0], plane[1], plane[2]);
		// Now get the radius (easy)
		float r = RobotRun.dist(center.x, center.y, center.z, a.x, a.y, a.z);
		// Calculate a vector from the center to point a
		PVector u = new PVector(a.x - center.x, a.y - center.y, a.z - center.z);
		u.normalize();
		// get the normal of the plane created by the 3 input points
		PVector tmp1 = new PVector(a.x - b.x, a.y - b.y, a.z - b.z);
		PVector tmp2 = new PVector(a.x - c.x, a.y - c.y, a.z - c.z);
		PVector n = tmp1.cross(tmp2);
		n.normalize();
		// calculate the angle between the start and end points
		PVector vec1 = new PVector(a.x - center.x, a.y - center.y, a.z - center.z);
		PVector vec2 = new PVector(c.x - center.x, c.y - center.y, c.z - center.z);
		float theta = RobotRun.atan2(vec1.cross(vec2).dot(n), vec1.dot(vec2));
		if (theta < 0)
			theta += PConstants.TWO_PI;
		// finally, draw an arc through all 3 points by rotating the u
		// vector around our normal vector
		float angle = 0, mu = 0;
		int numPoints = (int) (r * theta / distBtwPts);
		float inc = 1 / (float) numPoints;
		float angleInc = (theta) / numPoints;
		for (int i = 0; i < numPoints; i += 1) {
			PVector pos = RQuaternion.rotateVectorAroundAxis(u, n, angle).mult(r).add(center);
			if (i == numPoints - 1)
				pos = end.position;
			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(pos, qi));
			angle += angleInc;
			mu += inc;
		}
	}

	private void calculateContinuousPositions(Point start, Point end, Point next, float percentage) {
		// percentage /= 2;
		percentage /= 1.5f;
		percentage = 1 - percentage;
		percentage = RobotRun.constrain(percentage, 0, 1);

		PVector p1 = start.position.copy();
		PVector p2 = end.position;
		PVector p3 = next.position;
		RQuaternion q1 = start.orientation.clone();
		RQuaternion q2 = end.orientation;
		RQuaternion q3 = next.orientation;
		RQuaternion qi = new RQuaternion();

		ArrayList<Point> secondaryTargets = new ArrayList<>();
		float d1 = RobotRun.dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		float d2 = RobotRun.dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
		int numberOfPoints = 0;
		if (d1 > d2) {
			numberOfPoints = (int) (d1 / distBtwPts);
		} else {
			numberOfPoints = (int) (d2 / distBtwPts);
		}

		float mu = 0;
		float increment = 1.0f / numberOfPoints;
		for (int n = 0; n < numberOfPoints; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q2, q3, mu);
			secondaryTargets.add(new Point(new PVector(p2.x * (1 - mu) + (p3.x * mu), p2.y * (1 - mu) + (p3.y * mu),
					p2.z * (1 - mu) + (p3.z * mu)), qi));
		}

		mu = 0;
		int transitionPoint = (int) (numberOfPoints * percentage);
		for (int n = 0; n < transitionPoint; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu), p1.z * (1 - mu) + (p2.z * mu)), qi));
		}

		int secondaryIdx = 0; // accessor for secondary targets

		mu = 0;
		increment /= 2.0f;

		Point currentPoint;
		if (intermediatePositions.size() > 0) {
			currentPoint = intermediatePositions.get(intermediatePositions.size() - 1);
		} else {
			// NOTE orientation is in Native Coordinates!
			currentPoint = start.clone();
		}

		for (int n = transitionPoint; n < numberOfPoints; n++) {
			mu += increment;
			Point tgt = secondaryTargets.get(secondaryIdx);
			qi = RQuaternion.SLERP(currentPoint.orientation, tgt.orientation, mu);
			intermediatePositions.add(new Point(new PVector(currentPoint.position.x * (1 - mu) + (tgt.position.x * mu),
					currentPoint.position.y * (1 - mu) + (tgt.position.y * mu),
					currentPoint.position.z * (1 - mu) + (tgt.position.z * mu)), qi));
			currentPoint = intermediatePositions.get(intermediatePositions.size() - 1);
			secondaryIdx++;
		}
		interMotionIdx = 0;
	}

	// TODO: Add error check for colinear case (denominator is zero)
	private float calculateH(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = (x2 * x2 + y2 * y2) * y3 - (x3 * x3 + y3 * y3) * y2
				- ((x1 * x1 + y1 * y1) * y3 - (x3 * x3 + y3 * y3) * y1) + (x1 * x1 + y1 * y1) * y2
				- (x2 * x2 + y2 * y2) * y1;
		float denominator = (x2 * y3 - x3 * y2) - (x1 * y3 - x3 * y1) + (x1 * y2 - x2 * y1);
		denominator *= 2;
		return numerator / denominator;
	}

	private void calculateIntermediatePositions(Point start, Point end) {
		PVector p1 = start.position;
		PVector p2 = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		float mu = 0;
		float dist = RobotRun.dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) + 100f * q1.dist(q2);
		int numberOfPoints = (int) (dist / distBtwPts);

		float increment = 1.0f / numberOfPoints;
		for (int n = 0; n < numberOfPoints; n++) {
			mu += increment;

			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu), p1.z * (1 - mu) + (p2.z * mu)), qi));
		}

		interMotionIdx = 0;
	} // end calculate intermediate positions

	private float calculateK(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = x2 * (x3 * x3 + y3 * y3) - x3 * (x2 * x2 + y2 * y2)
				- (x1 * (x3 * x3 + y3 * y3) - x3 * (x1 * x1 + y1 * y1)) + x1 * (x2 * x2 + y2 * y2)
				- x2 * (x1 * x1 + y1 * y1);
		float denominator = (x2 * y3 - x3 * y2) - (x1 * y3 - x3 * y1) + (x1 * y2 - x2 * y1);
		denominator *= 2;
		return numerator / denominator;
	}
	
	private PVector circleCenter(PVector a, PVector b, PVector c) {
		float h = calculateH(a.x, a.y, b.x, b.y, c.x, c.y);
		float k = calculateK(a.x, a.y, b.x, b.y, c.x, c.y);
		return new PVector(h, k, a.z);
	}
	
	private PVector[] createPlaneFrom3Points(PVector a, PVector b, PVector c) {
		PVector n1 = new PVector(a.x - b.x, a.y - b.y, a.z - b.z);
		n1.normalize();
		PVector n2 = new PVector(a.x - c.x, a.y - c.y, a.z - c.z);
		n2.normalize();
		PVector x = n1.copy();
		PVector z = n1.cross(n2);
		PVector y = x.cross(z);
		y.normalize();
		z.normalize();
		PVector[] coordinateSystem = new PVector[3];
		coordinateSystem[0] = x;
		coordinateSystem[1] = y;
		coordinateSystem[2] = z;
		return coordinateSystem;
	}
	
	@Override
	public int executeMotion(RoboticArm robot) {
		motionFrameCounter++;
		// speed is in pixels per frame, multiply that by the current speed
		// setting
		// which is contained in the motion instruction
		float currentSpeed = robot.getMotorSpeed() * speed;
		if (currentSpeed * motionFrameCounter > robot.calculateDistanceBetweenPoints()) {
			interMotionIdx++;
			motionFrameCounter = 0;
			if (interMotionIdx >= intermediatePositions.size()) {
				reset(distBtwPts, speed);
				return 0;
			}

			int ret = 0;
			if (intermediatePositions.size() > 0) {
				Point tgtPoint = intermediatePositions.get(interMotionIdx);
				ret = robot.jumpTo(tgtPoint.position, tgtPoint.orientation);
			}

			if (ret == 1) {
				setFault(true);
				return 1;
			}
		}

		return 2;
	}

	@Override
	public void halt() {
		reset(0f, 0f);
	}

	@Override
	public boolean hasMotion() {
		// TODO greater than 1 or 0?
		return !hasFault() && intermediatePositions.size() > 0;
	}
	
	private void reset(float distBtwPts, float speed) {
		motionFault = false;
		intermediatePositions.clear();
		this.distBtwPts = distBtwPts;
		this.speed = speed;
		interMotionIdx = 0;
		motionFrameCounter = 0;
	}
	
	private PVector vectorConvertFrom(PVector point, PVector xAxis, PVector yAxis, PVector zAxis) {
		PMatrix3D matrix = new PMatrix3D(xAxis.x, yAxis.x, zAxis.x, 0, xAxis.y, yAxis.y, zAxis.y, 0, xAxis.z, yAxis.z,
				zAxis.z, 0, 0, 0, 0, 1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}

	private PVector vectorConvertTo(PVector point, PVector xAxis, PVector yAxis, PVector zAxis) {
		PMatrix3D matrix = new PMatrix3D(xAxis.x, xAxis.y, xAxis.z, 0, yAxis.x, yAxis.y, yAxis.z, 0, zAxis.x, zAxis.y,
				zAxis.z, 0, 0, 0, 0, 1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}
}
