package geom;
import processing.core.PVector;
import robot.RobotRun;

public class Ray {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	private PVector origin;
	private PVector direction;

	public Ray(RobotRun robotRun) {
		this.robotRun = robotRun;
		origin = new PVector(0f, 0f, 0f);
		direction = new PVector(1f, 1f, 1f);
	}

	public Ray(RobotRun robotRun, PVector origin, PVector pointOnRay) {
		this.robotRun = robotRun;
		this.origin = origin.copy();
		direction = pointOnRay.sub(origin);
		direction.normalize();
	}

	public void draw() {
		this.robotRun.stroke(0);
		this.robotRun.noFill();
		PVector endpoint = PVector.add(origin, PVector.mult(direction, 5000f));
		this.robotRun.line(origin.x, origin.y, origin.z, endpoint.x, endpoint.y, endpoint.z);
	}
}